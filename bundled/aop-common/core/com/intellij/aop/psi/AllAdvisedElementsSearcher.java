/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.jam.AopConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peter
*/
public class AllAdvisedElementsSearcher extends AopAdvisedElementsSearcher {
  private static final Logger LOG = Logger.getInstance("#com.intellij.aop.psi.AllAdvisedElementsSearcher");
  private GlobalSearchScope myScope;

  @TestOnly
  public AllAdvisedElementsSearcher(final PsiManager manager) {
    this(manager, GlobalSearchScope.allScope(manager.getProject()));

  }

  public AllAdvisedElementsSearcher(final PsiManager manager, final GlobalSearchScope scope) {
    super(manager);
    myScope = scope;
  }

  public boolean process(final Processor<PsiClass> processor) {
    final PsiPackage psiPackage = JavaPsiFacade.getInstance(getManager().getProject()).findPackage("");
    return psiPackage == null || processPackage(processor, psiPackage, new ArrayList<PsiPackage>());

  }

  @Override
  public boolean isAcceptable(final PsiClass psiClass) {
    return true;
  }

  private boolean processPackage(final Processor<PsiClass> processor, final PsiPackage psiPackage, final List<PsiPackage> visited) {
    if (visited.contains(psiPackage)) {
      LOG.error("Circular package structure:\n" + StringUtil.join(visited, new Function<PsiPackage, String>() {
        public String fun(final PsiPackage psiPackage) {
          return psiPackage.getQualifiedName() + " === " + StringUtil.join(psiPackage.getDirectories(), new Function<PsiDirectory, String>() {
            public String fun(final PsiDirectory psiDirectory) {
              return psiDirectory.getVirtualFile().getPath();
            }
          }, "; ");
        }
      }, "\n"));
    }

    visited.add(psiPackage);
    if (!ContainerUtil.process(psiPackage.getClasses(myScope), new Processor<PsiClass>() {
      public boolean process(final PsiClass psiClass) {
        return psiClass.getModifierList().findAnnotation(AopConstants.ASPECT_ANNO) != null || processor.process(psiClass);
      }
    })) {
      return false;
    }
    for (final PsiPackage aPackage : psiPackage.getSubPackages(myScope)) {
      if (!processPackage(processor, aPackage, new ArrayList<PsiPackage>(visited))) return false;
    }
    return true;
  }

}
