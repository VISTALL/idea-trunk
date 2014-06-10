/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.spring.SpringBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Dmitry Avdeev
*/
public class PatternFileReferenceSet extends FileReferenceSet {

  public PatternFileReferenceSet(final String str, final PsiElement element, final int offset) {
    super(str, element, offset, null, true);
  }

  public FileReference createFileReference(final TextRange range, final int index, final String text) {
    if (!isAntPattern(text)) return super.createFileReference(range, index, text);

    return new PatternFileReference(this, range, index, text);
  }

  // @see org.springframework.util.AntPathMatcher#isPattern
  static boolean isAntPattern(final String str) {
    return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
  }

  protected boolean isSoft() {
    return true;
  }

  /**
 * @author Dmitry Avdeev
  */
  public static class PatternFileReference extends FileReference {
    public PatternFileReference(final FileReferenceSet referenceSet, final TextRange range, final int index, final String text) {
      super(referenceSet, range, index, text);
    }

    protected void innerResolveInContext(@NotNull final String text,
                                         @NotNull final PsiFileSystemItem context,
                                         final Collection<ResolveResult> result, final boolean caseSensitive) {


      if (text.equals("**")) {
        addDirectoryResolves(context, result);
      }
      else {
        final String patternText = FileUtil.convertAntToRegexp(text);
        final Pattern pattern = Pattern.compile(patternText);

        final PsiElement[] psiElements = context.getChildren();
        for (PsiElement psiElement : psiElements) {
          if (psiElement instanceof PsiFileSystemItem) {
            if (pattern.matcher(((PsiFileSystemItem)psiElement).getName()).matches()) {
              result.add(new PsiElementResolveResult(psiElement));
            }
          }
        }
      }
    }

    private static void addDirectoryResolves(final PsiElement context, final Collection<ResolveResult> result) {
      if (context instanceof PsiFileSystemItem && ((PsiFileSystemItem)context).isDirectory()) {
        result.add(new PsiElementResolveResult(context));
        for (PsiElement psiElement : context.getChildren()) {
           addDirectoryResolves(psiElement, result);
        }
      }
    }

    public String getUnresolvedMessagePattern() {
      return SpringBundle.message("spring.resource.ant.style.reference.error.message");
    }
  }
}
