/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.jam.AopConstants;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.search.MethodSuperSearcher;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.*;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.AspectjAutoproxy;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.tx.AnnotationDriven;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.Collection;

/**
 * @author peter
 */
public class SpringAdvisedElementsSearcher extends AopAdvisedElementsSearcher {
  private static final Key<CachedValue<Boolean>> CGLIB_PROXYING = Key.create("CGLIB_PROXYING");
  private final List<SpringModel> myModels;
  private final AtomicNotNullLazyValue<Boolean> myCglibProxyType = new AtomicNotNullLazyValue<Boolean>() {
    @NotNull
    @Override
    protected Boolean compute() {
      for (final SpringModel model : myModels) {
        for (final DomFileElement<Beans> root : model.getRoots()) {
          CachedValue<Boolean> value = root.getUserData(CGLIB_PROXYING);
          if (value == null) {
            root.putUserData(CGLIB_PROXYING, value = getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Boolean>() {
              public Result<Boolean> compute() {
                return Result.create(isCglib(root), root);
              }
            }, false));
          }
          if (value.getValue().booleanValue()) {
            return true;
          }
        }

      }
      return false;
    }
  };
  private static final Key<CachedValue<Boolean>> INHERITANCE_CACHE_KEY = Key.create("INHERITANCE_CACHE");

  public SpringAdvisedElementsSearcher(@NotNull final PsiManager manager, final List<SpringModel> models) {
    super(manager);
    myModels = models;
  }

  private static boolean isCglib(DomFileElement<Beans> root) {
    final DomElement element = root.getRootElement();
        for (final AopConfig config : DomUtil.getDefinedChildrenOfType(element, AopConfig.class)) {
          if (Boolean.TRUE.equals(config.getProxyTargetClass().getValue())) {
            return true;
          }
        }

        for (final CommonSpringBean springBean : SpringUtils.getChildBeans(element, false)) {
          if (springBean instanceof AnnotationDriven && Boolean.TRUE.equals(((AnnotationDriven)springBean).getProxyTargetClass().getValue()) ||
              springBean instanceof AspectjAutoproxy && Boolean.TRUE.equals(((AspectjAutoproxy)springBean).getProxyTargetClass().getValue())) {
            return true;
          }
        }
    return false;
  }

  public boolean isAcceptable(final PsiClass psiClass) {
    return _isAcceptable(psiClass) && isSpringBeanClass(psiClass);

  }

  protected boolean isSpringBeanClass(PsiClass psiClass) {
    for (final SpringModel model : myModels) {
      if (!model.findBeansByPsiClassWithInheritance(psiClass).isEmpty()) return true;
    }
    return false;
  }

  private static boolean _isAcceptable(final PsiClass psiClass) {
    if (psiClass == null || psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.FINAL)) return false;

    if (isAopClass(psiClass)) return false;

    final PsiModifierList modifierList = psiClass.getModifierList();
    if ((modifierList != null && modifierList.findAnnotation(AopConstants.ASPECT_ANNO) != null)) return false;
    return true;
  }

  public boolean process(final Processor<PsiClass> processor) {
    final MyBeanVisitor visitor = new MyBeanVisitor(processor);
    final Set<SpringModel> visited = new THashSet<SpringModel>();
    for (final SpringModel model : myModels) {
      ProgressManager.getInstance().checkCanceled();
      if (!visited.add(model)) continue;

      final Collection<? extends SpringBaseBeanPointer> beans =
        ApplicationManager.getApplication().runReadAction(new Computable<Collection<? extends SpringBaseBeanPointer>>() {
          public Collection<? extends SpringBaseBeanPointer> compute() {
            return model.getAllCommonBeans(true);
          }
        });

      for (final SpringBaseBeanPointer pointer : beans) {
        ProgressManager.getInstance().checkCanceled();

        final boolean[] stop = new boolean[]{false};
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          public void run() {
            if (!pointer.isValid()) {
              return;
            }

            if (pointer instanceof DomSpringBeanPointer) {
              stop[0] = !SpringModelVisitor.visitBean(visitor, ((DomSpringBeanPointer)pointer).getSpringBean());
            }
            else {
              stop[0] = !visitor.processBeanClass(pointer.getBeanClass());
            }
          }
        });
        if (stop[0]) {
          return false;
        }
      }
    }

    return true;
  }

  private static class MyBeanVisitor extends SpringModelVisitor {
    private final Processor<PsiClass> myProcessor;

    private MyBeanVisitor(final Processor<PsiClass> processor) {
      myProcessor = processor;
    }

    protected boolean visitBean(final CommonSpringBean bean) {
      ProgressManager.getInstance().checkCanceled();
      return processBeanClass(bean.getBeanClass()) && super.visitBean(bean);
    }

    final boolean processBeanClass(@Nullable final PsiClass beanClass) {
      return !_isAcceptable(beanClass) || InheritanceUtil.processSupers(beanClass, true, myProcessor);
    }

  }

  private static boolean isAopClass(@NotNull final PsiClass psiClass) {
    CachedValue<Boolean> value = psiClass.getUserData(INHERITANCE_CACHE_KEY);
    if (value == null) {
      value = psiClass.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Boolean>() {
        public Result<Boolean> compute() {
          final boolean result = !InheritanceUtil.processSupers(psiClass, true, new Processor<PsiClass>() {
            public boolean process(final PsiClass psiClass) {
              @NonNls final String qname = psiClass.getQualifiedName();
              return !"org.springframework.aop.Advisor".equals(qname) &&
                     !"org.aopalliance.aop.Advice".equals(qname) &&
                     !"org.springframework.aop.framework.AopInfrastructureBean".equals(qname);
            }
          });
          return Result.create(result, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      }, false);
      psiClass.putUserData(INHERITANCE_CACHE_KEY, value);
    }
    return value.getValue().booleanValue();
  }

  private static boolean hasInterfaces(@NotNull final PsiClass psiClass, @NotNull Set<PsiClass> visited) {
    if (psiClass.getInterfaces().length > 0) return true;
    final PsiClass superClass = psiClass.getSuperClass();
    return superClass != null && visited.add(superClass) && hasInterfaces(superClass, visited);
  }

  public boolean acceptsBoundMethod(@NotNull final PsiMethod method) {
    if (!super.acceptsBoundMethod(method)) return false;
    if (method.hasModifierProperty(PsiModifier.STATIC)) return false;
    if (method.hasModifierProperty(PsiModifier.FINAL)) return false;
    if (method.hasModifierProperty(PsiModifier.PRIVATE)) return false;
    return true;
  }

  @Override
  public boolean acceptsBoundMethodHeavy(@NotNull PsiMethod method) {
    if (isJdkProxyType()) {
      final PsiClass psiClass = method.getContainingClass();
      if (psiClass == null || hasInterfaces(psiClass, new THashSet<PsiClass>()) && !isFromInterface(method, psiClass)) return false;
    }
    return super.acceptsBoundMethodHeavy(method);
  }

  public boolean isJdkProxyType() {
    return !myCglibProxyType.getValue();
  }

  private static boolean isFromInterface(final PsiMethod method, final PsiClass psiClass) {
    return !new MethodSuperSearcher().execute(new SuperMethodsSearch.SearchParameters(method, psiClass, true, false), new Processor<MethodSignatureBackedByPsiMethod>() {
      public boolean process(final MethodSignatureBackedByPsiMethod signature) {
        final PsiClass aClass = signature.getMethod().getContainingClass();
        return aClass == null || !aClass.isInterface();
      }
    });
  }

  public List<SpringModel> getSpringModels() {
    return myModels;
  }

}
