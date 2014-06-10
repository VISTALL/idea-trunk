/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.*;
import com.intellij.aop.psi.AllAdvisedElementsSearcher;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.spring.model.xml.aop.SpringAopAdvice;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author peter
 */
public class SpringAopProvider extends AopProvider {
  private static final Key<CachedValue<Set<? extends AopAspect>>> CACHED_SPRING_MODELS = Key.create("CachedSpringModels");
  private static final Key<CachedValue<AopAdvisedElementsSearcher>> CACHED_SEARCHER = Key.create("CACHED_SEARCHER");

  @NotNull
  public Set<? extends AopAspect> getAdditionalAspects(@NotNull final Module module) {
    if (SpringManager.getInstance(module.getProject()) == null) return Collections.emptySet();

    if (module.getUserData(CACHED_SPRING_MODELS) == null) {
      module.putUserData(CACHED_SPRING_MODELS, PsiManager.getInstance(module.getProject()).getCachedValuesManager().createCachedValue(new CachedValueProvider<Set<? extends AopAspect>>() {
        public Result<Set<? extends AopAspect>> compute() {
          final THashSet<AopAspect> set = new THashSet<AopAspect>();
          for (final SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
            for (final DomFileElement<Beans> element : model.getRoots()) {
              addAopAspects(set, element.getRootElement());
            }
          }
          return new Result<Set<? extends AopAspect>>(set, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      }, false));
    }

    return module.getUserData(CACHED_SPRING_MODELS).getValue();
  }

  protected static Set<AopAspect> addAopAspects(final Set<AopAspect> set, final DomElement element) {
    for (final DomElement child : DomUtil.getDefinedChildren(element, true, false)) {
      if (child instanceof AopAspect) {
        final AopAspect aspect = (AopAspect)child;
        set.add(aspect);
      } else if (child instanceof AopConfig) {
        final AopConfig config = (AopConfig)child;
        set.addAll(config.getAdvisors());
        set.addAll(config.getAspects());
      }
    }
    return set;
  }

  public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@NotNull final PsiClass aClass) {
    return getSearcher(aClass);
  }

  public static AopAdvisedElementsSearcher getSearcher(final PsiClass aClass) {
    CachedValue<AopAdvisedElementsSearcher> value = aClass.getUserData(CACHED_SEARCHER);
    if (value == null) {
      aClass.putUserData(CACHED_SEARCHER, value = aClass.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<AopAdvisedElementsSearcher>() {
        public Result<AopAdvisedElementsSearcher> compute() {
          final Module module = ModuleUtil.findModuleForPsiElement(aClass);
          if (module == null || hasNoSpringFacetAtAll(module)) {
            final GlobalSearchScope scope = module == null ? GlobalSearchScope.EMPTY_SCOPE: GlobalSearchScope.moduleWithDependenciesScope(module);
            final AopAdvisedElementsSearcher searcher = new AllAdvisedElementsSearcher(aClass.getManager(), scope) {
              public boolean shouldSuppressErrors() {
                return true;
              }
            };
            return Result.create(searcher, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, ProjectRootManager.getInstance(aClass.getProject()));
          }

          final AopAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(aClass.getManager(), SpringUtils.getNonEmptySpringModels(module));
          return Result.create(searcher, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, ProjectRootManager.getInstance(aClass.getProject()));
        }
      }, false));
    }
    return value.getValue();
  }

  private static boolean hasNoSpringFacetAtAll(final Module module) {
    return ModuleUtil.visitMeAndDependentModules(module, new ModuleUtil.ModuleVisitor() {
      public boolean visit(final Module module) {
        return SpringFacet.getInstance(module) == null;
      }
    });
  }

  @Nullable
  public Pair<? extends ArgNamesManipulator, PsiMethod> getCustomArgNamesManipulator(@NotNull final PsiElement element) {
    if (element instanceof XmlAttributeValue &&
        element.getParent() instanceof XmlAttribute &&
        "pointcut-ref".equals(((XmlAttribute)element.getParent()).getLocalName())) {
      final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (tag != null) {
        final DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement(tag);
        if (domElement instanceof BasicAdvice) {
          final BasicAdvice advice = (BasicAdvice)domElement;
          final PsiMethod method = advice.getMethod().getValue();
          if (advice.getPointcut().getStringValue() == null && method != null) {
            return Pair.create(new SpringArgNamesManipulator(tag), method);
          }
        }
      }
    }

    return super.getCustomArgNamesManipulator(element);
  }

  @Override
  public Integer getAdviceOrder(final AopAdvice advice) {
    if (advice instanceof SpringAopAdvice) {
      return ((SpringAopAdvice)advice).getOrder().getValue();
    }
    final PsiElement element = advice.getIdentifyingPsiElement();
    if (element instanceof PsiAnnotation) {
      final PsiClass aClass = PsiTreeUtil.getContextOfType(element, PsiClass.class, false);
      if (aClass == null) return null;

      final PsiAnnotation annotation = aClass.getModifierList().findAnnotation("org.springframework.core.annotation.Order");
      if (annotation != null) {
        final PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (value instanceof PsiExpression) {
          final Object o = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression)value, false);
          if (o instanceof Integer) {
            return (Integer)o;
          }
        }
        return null;
      }

      final PsiClass orderedClass =
          JavaPsiFacade.getInstance(aClass.getProject()).findClass("org.springframework.core.Ordered", aClass.getResolveScope());
      if (orderedClass != null && aClass.isInheritor(orderedClass, true)) {
        final PsiMethod[] methods = aClass.findMethodsByName("getOrder", true);
        for (final PsiMethod method : methods) {
          final PsiCodeBlock body = method.getBody();
          if (method.getParameterList().getParametersCount() == 0 && body != null && body.getStatements().length == 1) {
            final PsiStatement first = body.getStatements()[0];
            if (first instanceof PsiReturnStatement) {
              final PsiExpression value = ((PsiReturnStatement)first).getReturnValue();
              final Object o = JavaConstantExpressionEvaluator.computeConstantExpression(value, false);
              if (o instanceof Integer) {
                return (Integer)o;
              }
            }
          }
        }
      }
    }

    return super.getAdviceOrder(advice);
  }
}
