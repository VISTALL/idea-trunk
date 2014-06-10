/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.util.Function;
import com.intellij.util.LogicalRoot;
import com.intellij.util.LogicalRootsManager;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.StringTokenizer;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public class ResourceResolverUtils {
  @NonNls private static final String CLASSPATH_PREFIX = "classpath:";
  @NonNls private static final String CLASSPATH_PREFIX_ASTERISK = "classpath*:";
  @NonNls private static final String FILE_PREFIX = "file:";
  @NonNls private static final String HTTP_PREFIX = "http:";

  public static final Condition<PsiFileSystemItem> FILE_FILTER = new Condition<PsiFileSystemItem>() {
    public boolean value(final PsiFileSystemItem item) {
      return item instanceof PsiFile;
    }
  };
  public static final Condition<PsiFileSystemItem> DIRECTORY_FILTER = new Condition<PsiFileSystemItem>() {
    public boolean value(final PsiFileSystemItem item) {
      return item instanceof PsiDirectory;
    }
  };

  private static final FilePathReferenceProvider ourFilePathReferenceProvider = new FilePathReferenceProvider() {
    protected FileReference createFileReference(final FileReferenceSet referenceSet,
                                                final TextRange range,
                                                final int index,
                                                final String text) {
      if (!PatternFileReferenceSet.isAntPattern(text)) return new FileReference(referenceSet, range, index, text);

      return new PatternFileReferenceSet.PatternFileReference(referenceSet, range, index, text);
    }
  };

  private ResourceResolverUtils() {
  }

  public static boolean processSpringValues(final SpringProperty property, final PairProcessor<GenericDomValue, String> processor) {
    {
      final GenericAttributeValue<String> valueAttr = property.getValueAttr();
      final XmlAttribute valueAttrElement = valueAttr.getXmlAttribute();
      final String valueAttrString = valueAttr.getStringValue();
      if (valueAttrElement != null && valueAttrString != null && !processor.process(valueAttr, valueAttrString)) {
        return false;
      }
    }
    {
      final SpringValue value = property.getValue();
      final XmlTag valueElement = value.getXmlTag();
      final String valueString = value.getStringValue();
      if (valueElement != null && valueString != null && !processor.process(value, valueString)) {
        return false;
      }
    }
    {
      final ListOrSet listOrSet = property.getList();
      for (SpringValue springValue : listOrSet.getValues()) {
        final XmlTag element = springValue.getXmlTag();
        final String string = springValue.getStringValue();
        if (element != null && string != null && !processor.process(springValue, string)) {
          return false;
        }
      }
    }
    return true;
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T getResourceItems(final @NotNull SpringProperty property, final T result, final Condition<PsiFileSystemItem> filter) {
    processSpringValues(property, new PairProcessor<GenericDomValue, String>() {
      public boolean process(final GenericDomValue domValue, final String s) {
        final Object value = domValue.getValue();
        if (value instanceof Collection) {
          for (Object o : (Collection)value) {
            if (o instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)o)) {
              result.add((V)o);
            }
          }
        }
        return true;
      }
    });
    return result;
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(final @NotNull PsiElement element, final @NotNull String s,
                                                  final T result, final Condition<PsiFileSystemItem> filter) {
    final PsiReference[] references = getReferences(element, s, false, false);
    return addResourceItems(result, references, filter);
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(final @NotNull PsiElement element, final @NotNull String s, final String delimiter,
                                                  final T result, final Condition<PsiFileSystemItem> filter) {
    final ArrayList<PsiReference> references = new ArrayList<PsiReference>();
    final int startInElement = ElementManipulators.getOffsetInElement(element);

    processSeparatedString(s, delimiter, new PairProcessor<String, Integer>() {
      public boolean process(final String s, final Integer offset) {
        final PsiReference[] psiReferences = getReferences(element, s, false, false, offset.intValue() + startInElement, true);
        references.addAll(Arrays.asList(psiReferences));
        return true;
      }
    });

    return addResourceItems(result, references.toArray(new PsiReference[references.size()]) , filter);
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(final @NotNull GenericDomValue element, final @NotNull String s,
                                                  final T result,
                                                  final Condition<PsiFileSystemItem> filter) {
    final Converter converter = WrappingConverter.getDeepestConverter(element.getConverter(), element);
    if (converter instanceof CustomReferenceConverter) {
      final PsiReference[] references = ((CustomReferenceConverter)converter).createReferences(element, element.getXmlElement(),
                                                                                               AbstractConvertContext.createConvertContext(element));
      return addResourceItems(result, references, filter);
    }
    return result;
  }

  private static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceItems(final T result, final PsiReference[] references, final Condition<PsiFileSystemItem> filter) {
    for (PsiReference reference : references) {
      if (reference instanceof PsiPolyVariantReference) {
        final ResolveResult[] resolveResults = ((PsiPolyVariantReference)reference).multiResolve(false);
        for (ResolveResult resolveResult : resolveResults) {
          final PsiElement psiElement = resolveResult.getElement();
          if (psiElement instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)psiElement)) {
            result.add((V)psiElement);
          }
        }
      }
      else {
        final PsiElement psiElement = reference.resolve();
        if (psiElement instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)psiElement)) {
          result.add((V)psiElement);
        }
      }
    }
    return result;
  }

  public static PsiReference[] getReferences(final @NotNull PsiElement element,
                                             final @Nullable String s,
                                             final boolean fromRoot,
                                             final boolean fromCurrent) {
    final int offset = ElementManipulators.getOffsetInElement(element);
    return getReferences(element, s, fromRoot, fromCurrent, offset);
  }

  public static PsiReference[] getReferences(final @NotNull PsiElement element,
                                             final @Nullable String s,
                                             final boolean fromRoot,
                                             final boolean fromCurrent,
                                             final int offset) {

    return getReferences(element, s, fromRoot, fromCurrent, offset, true);
  }

  public static PsiReference[] getReferences(final @NotNull PsiElement element,
                                             final @Nullable String s,
                                             final boolean fromRoot,
                                             final boolean fromCurrent,
                                             final int offset,
                                             final boolean soft) {

    if (s == null || StringUtil.isEmptyOrSpaces(s) || s.startsWith(HTTP_PREFIX)) return PsiReference.EMPTY_ARRAY;

    final FileReferenceSet set;
    if (s.startsWith(CLASSPATH_PREFIX)) {
      return getClassPathReferences(element, s.substring(CLASSPATH_PREFIX.length()), CLASSPATH_PREFIX.length() + offset, soft);
    } else if (s.startsWith(CLASSPATH_PREFIX_ASTERISK)) {
      return getClassPathReferences(element, s.substring(CLASSPATH_PREFIX_ASTERISK.length()), CLASSPATH_PREFIX_ASTERISK.length() + offset,
                                    soft);
    }  else if (s.startsWith(FILE_PREFIX)) {
      final String str = s.substring(FILE_PREFIX.length());
      if (str.startsWith("/") || new File(str).isAbsolute()) {
        return PsiReference.EMPTY_ARRAY;
      }

      set = new PatternFileReferenceSet(str, element, FILE_PREFIX.length() + offset) {
        @Override
        protected boolean isSoft() {
          return soft;
        }
      };

      set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    else {
      set = new PatternFileReferenceSet(s, element, offset) {
        @Override
        protected boolean isSoft() {
          return soft;
        }
      };
      if (fromCurrent) {
        set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, new Function<PsiFile, Collection<PsiFileSystemItem>>() {
          public Collection<PsiFileSystemItem> fun(final PsiFile file) {
            return ContainerUtil.<PsiFileSystemItem>createMaybeSingletonList(file.getContainingDirectory());
          }
        });
      }
      else if (fromRoot) {
        set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
      }
    }
    return set.getAllReferences();
  }

  public static PsiReference[] getClassPathReferences(final PsiElement element, final String s, final int offset, final boolean soft) {
    return ourFilePathReferenceProvider.getReferencesByElement(element, s, offset, soft);
  }

  public static boolean processSeparatedString(final String str, final String delimiter, final PairProcessor<String, Integer> processor) {
    if (str == null || StringUtil.isEmptyOrSpaces(str)) return true;

    final StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
    while (tokenizer.hasMoreElements()) {
      @NonNls String s = tokenizer.nextElement().trim();
      if (s.length() == 0) continue;
      if (!processor.process(s, str.indexOf(s))) return false;
    }

    return true;
  }

  @Nullable
  public static String getResourceFileReferenceString(final PsiFile resourceFile) {
    final VirtualFile virtualFile = resourceFile == null ? null : resourceFile.getVirtualFile();
    if (virtualFile == null) return null;

    final LogicalRoot logicalRoot = LogicalRootsManager.getLogicalRootsManager(resourceFile.getProject()).findLogicalRoot(virtualFile);
    if (logicalRoot != null) {
      return CLASSPATH_PREFIX + VfsUtil.getRelativePath(virtualFile, logicalRoot.getVirtualFile(), '/');
    }

    final ProjectFileIndex index = ProjectRootManager.getInstance(resourceFile.getProject()).getFileIndex();
    final VirtualFile contentRoot = index.getContentRootForFile(virtualFile);
    if (contentRoot != null) {
      return FILE_PREFIX + VfsUtil.getRelativePath(virtualFile, contentRoot, '/');
    }
    else {
      return FILE_PREFIX + virtualFile.getPath();
    }
  }
}
