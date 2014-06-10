/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag;

import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.CommonProcessors;
import com.intellij.util.PairProcessor;
import com.intellij.util.Processor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import static org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil.NAMESPACE_FIELD;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.resolve.processors.PropertyResolverProcessor;

import java.util.Collection;
import java.util.List;

/**
 * @author peter
 */
public class GspNamespaceDescriptor implements XmlNSDescriptor, DumbAware {
  private final String myPrefix;
  private final List<PsiClass> myClasses;

  public GspNamespaceDescriptor(String prefix, List<PsiClass> classes) {
    myPrefix = prefix;
    myClasses = classes;
  }

  @Nullable
  public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
    CommonProcessors.FindFirstProcessor<XmlElementDescriptor> descriptorProcessor = new CommonProcessors.FindFirstProcessor<XmlElementDescriptor>();
    processElementDescriptors(tag.getLocalName(), tag, descriptorProcessor);
    return descriptorProcessor.getFoundValue();
  }

  public void processElementDescriptors(@Nullable final String tagName, @NotNull final PsiElement place, final Processor<XmlElementDescriptor> processor) {
    for (final PsiClass aClass : myClasses) {
      if (!processGrailsTaglibs(tagName, place, processor, aClass)) return;
    }

    if (GspTagLibUtil.DEFAULT_TAGLIB_PREFIX.equals(myPrefix)) {
      processBuiltInTags(tagName, place, processor);
    }
  }

  private boolean processGrailsTaglibs(final String tagName,
                                       final PsiElement place, Processor<XmlElementDescriptor> processor, PsiClass aClass) {
    final PropertyResolverProcessor resolverProcessor = new PropertyResolverProcessor(tagName, place) {
      @Override
      public boolean execute(PsiElement element, ResolveState state) {
        super.execute(element, state);
        return true;
      }
    };
    aClass.processDeclarations(resolverProcessor, ResolveState.initial(), null, place);
    for (final GroovyResolveResult result : resolverProcessor.getCandidates()) {
      final PsiElement element = result.getElement();
      if (element instanceof GrField) {
        final GrField field = (GrField)element;
        if (field.isProperty()) {
          final PsiMethod[] getters = field.getGetters();
          if (getters.length > 0 && isValidTagType(getters[0]) && !processor.process(new GspPropertyElementDescriptor(this, field, field.getName()))) {
            return false;
          }
        }
      }
      else if (element instanceof PsiMethod) {
        final PsiMethod method = (PsiMethod)element;
        if (PropertyUtil.isSimplePropertyGetter(method) && isValidTagType(method)) {
          if (!processor.process(new GspPropertyElementDescriptor(this, element, PropertyUtil.getPropertyNameByGetter(method)))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean processBuiltInTags(String tagName, PsiElement place, final Processor<XmlElementDescriptor> processor) {
    if (tagName != null) {
      final PsiClass psiClass = GspTagLibUtil.getBuiltInTagByName(tagName, place);
      if (psiClass != null && !processor.process(new GspBuiltInElementDescriptor(this, psiClass, tagName))) {
        return false;
      }
      return true;
    }
    GspTagLibUtil.processBuiltInTagClasses(place, new PairProcessor<String, PsiClass>() {
      public boolean process(final String tagName, final PsiClass psiClass) {
        return processor.process(new GspBuiltInElementDescriptor(GspNamespaceDescriptor.this, psiClass, tagName));
      }
    });
    return true;
  }

  private static boolean isValidTagType(PsiMethod method) {
    if (method.hasModifierProperty(PsiModifier.STATIC)) return false;

    final PsiType type = method.getReturnType();
    if (type == null) return false;

    return type.equalsToText(CommonClassNames.JAVA_LANG_OBJECT) || type.equalsToText("groovy.lang.Closure");
  }

  @NotNull
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable XmlDocument document) {
    if (document == null) {
      return XmlElementDescriptor.EMPTY_ARRAY;
    }

    final CommonProcessors.CollectProcessor<XmlElementDescriptor> processor = new CommonProcessors.CollectProcessor<XmlElementDescriptor>();
    processElementDescriptors(null, document, processor);
    final Collection<XmlElementDescriptor> results = processor.getResults();
    return results.toArray(new XmlElementDescriptor[results.size()]);
  }

  public XmlFile getDescriptorFile() {
    return null;
  }

  public boolean isHierarhyEnabled() {
    return false;
  }

  @Nullable
  public PsiElement getDeclaration() {
    for (final PsiClass psiClass : myClasses) {
      final PsiField field = psiClass.findFieldByName(NAMESPACE_FIELD, false);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

  public String getName(PsiElement context) {
    return myPrefix;
  }

  public String getName() {
    return myPrefix;
  }

  public void init(PsiElement element) {
    throw new UnsupportedOperationException("Method init is not yet implemented in " + getClass().getName());
  }

  public Object[] getDependences() {
    throw new UnsupportedOperationException("Method getDependences is not yet implemented in " + getClass().getName());
  }

  public String getPrefix() {
    return myPrefix;
  }

  public static GspNamespaceDescriptor getDefaultNsDescriptor(final XmlTag tag) {
    return new GspNamespaceDescriptor(GspTagLibUtil.DEFAULT_TAGLIB_PREFIX, GspTagLibUtil.getTagLibClasses(GspTagLibUtil.DEFAULT_TAGLIB_PREFIX, tag));
  }
}
