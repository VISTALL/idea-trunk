/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.*;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.ResolvedConstructorArgs;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.SpringBeanFactoryMethodConverter;
import com.intellij.spring.model.highlighting.SpringAutowireUtil;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
class ResolvedConstructorArgsImpl implements ResolvedConstructorArgs {

  /**
   * @see org.springframework.beans.factory.support.AutowireUtils#sortConstructors(java.lang.reflect.Constructor[])
   */
  private static final Comparator<PsiMethod> CTOR_COMPARATOR = new Comparator<PsiMethod>() {
    public int compare(final PsiMethod o1, final PsiMethod o2) {
      final boolean p1 = o1.hasModifierProperty(PsiModifier.PUBLIC);
      final boolean p2 = o2.hasModifierProperty(PsiModifier.PUBLIC);
      if (p1 != p2) {
        return (p1 ? -1 : 1);
      }
      return o1.getParameterList().getParametersCount() - o2.getParameterList().getParametersCount();
    }
  };

  private static final Comparator<ConstructorArg> ARG_COMPARATOR = new Comparator<ConstructorArg>() {
    public int compare(final ConstructorArg o1, final ConstructorArg o2) {
      final boolean hasValue1 = DomUtil.hasXml(o1.getValueElement());
      final boolean hasValue2 = DomUtil.hasXml(o2.getValueElement());
      return hasValue1 ? (hasValue2 ? 0 : 1) : (hasValue2 ? -1 : 0);
    }
  };

  @Nullable private PsiMethod myResolvedMethod;
  private final Map<PsiMethod, Map<ConstructorArg, PsiParameter>> myResolvedArgs =
    new HashMap<PsiMethod, Map<ConstructorArg, PsiParameter>>();
  private final Map<PsiMethod, Map<PsiParameter, Collection<SpringBaseBeanPointer>>> myAutowiredParams =
    new HashMap<PsiMethod, Map<PsiParameter, Collection<SpringBaseBeanPointer>>>();

  private final boolean myResolved;
  private List<PsiMethod> myCheckedMethods;

  ResolvedConstructorArgsImpl(@NotNull SpringBean bean) {
    myResolved = resolve(bean, SpringUtils.getSpringModel(bean));
  }

  public boolean isResolved() {
    return myResolved;
  }

  @Nullable
  public PsiMethod getResolvedMethod() {
    return myResolvedMethod;
  }

  @Nullable
  public List<PsiMethod> getCheckedMethods() {
    return myCheckedMethods;
  }

  @Nullable
  public Map<ConstructorArg, PsiParameter> getResolvedArgs() {
    return myResolvedMethod == null ? null : getResolvedArgs(myResolvedMethod);
  }

  public Map<ConstructorArg, PsiParameter> getResolvedArgs(@NotNull PsiMethod method) {
    return myResolvedArgs.get(method);
  }

  public Map<PsiParameter, Collection<SpringBaseBeanPointer>> getAutowiredParams(@NotNull final PsiMethod method) {
    return myAutowiredParams.get(method);
  }

  /**
   * @param bean spring bean
   * @return true if matched method has been found.
   * @see org.springframework.beans.factory.support.ConstructorResolver#autowireConstructor(String,org.springframework.beans.factory.support.RootBeanDefinition)
   */
  private boolean resolve(SpringBean bean, SpringModel springModel) {

    final List<PsiMethod> methods;
    final String factoryMethod = bean.getFactoryMethod().getStringValue();
    if (factoryMethod != null) {
      methods = SpringBeanFactoryMethodConverter.getFactoryMethodCandidates(bean, factoryMethod);
      if (methods.size() == 0) {
        return false;
      }
    }
    else {
      final PsiClass beanClass = bean.getBeanClass();
      if (beanClass != null) {
        final PsiMethod[] constructors = beanClass.getConstructors();
        if (constructors.length == 0) { // ctor by default
          return bean.getAllConstructorArgs().size() == 0;
        }
        methods = Arrays.asList(constructors);
      }
      else {
        return false;
      }
    }

    // there is at least one candidate here
    myCheckedMethods = new ArrayList<PsiMethod>(methods.size());

    final Set<ConstructorArg> args = bean.getAllConstructorArgs();
    final boolean constructorAutowire = SpringAutowireUtil.isConstructorAutowire(bean);
    final ConstructorArgumentValues values = new ConstructorArgumentValues();
    final int minNrOfArgs = values.init(args);

    Collections.sort(methods, CTOR_COMPARATOR);
    for (final PsiMethod method : methods) {
      final PsiParameter[] params = method.getParameterList().getParameters();
      if (myResolvedMethod != null && params.length < myResolvedMethod.getParameterList().getParametersCount()) {
        return true;
      }
      final HashMap<ConstructorArg, PsiParameter> resolvedArgs = new HashMap<ConstructorArg, PsiParameter>(params.length);
      myResolvedArgs.put(method, resolvedArgs);
      final HashMap<PsiParameter, Collection<SpringBaseBeanPointer>> autowiredParams = new HashMap<PsiParameter, Collection<SpringBaseBeanPointer>>();
      myAutowiredParams.put(method, autowiredParams);
      Set<ConstructorArg> usedArgs = new HashSet<ConstructorArg>(args.size());
      int autowired = 0;
      for (int i = 0; i < params.length; i++) {
        final PsiParameter param = params[i];
        final PsiType paramType = param.getType();
        ConstructorArg arg = values.resolve(i, paramType, usedArgs);

        if (arg == null && constructorAutowire) {
          final Collection<SpringBaseBeanPointer> beans = SpringAutowireUtil.autowireByType(springModel, paramType);
          if (beans.size() == 1) {
            autowired++;
          }
          autowiredParams.put(param, beans);
        }

        if (arg != null) {
          resolvedArgs.put(arg, param);
        }
      }
      if (resolvedArgs.size() + autowired == params.length && minNrOfArgs <= params.length) {
        // wow!
        myResolvedMethod = method;
      }
      myCheckedMethods.add(method);
    }
    return myResolvedMethod != null;
  }

  private static class ConstructorArgumentValues {
    Map<Integer, ConstructorArg> indexedArgs;
    List<ConstructorArg> genericArgs;

    private int init(final Set<ConstructorArg> args) {
      indexedArgs = new HashMap<Integer, ConstructorArg>(args.size());
      genericArgs = new ArrayList<ConstructorArg>(args.size());

      int minNrOfArgs = args.size();
      for (ConstructorArg arg : args) {
        final Integer index = arg.getIndex().getValue();
        if (index != null) {
          indexedArgs.put(index, arg);
          minNrOfArgs = Math.max(minNrOfArgs, index.intValue());
        }
        else {
          genericArgs.add(arg);
        }
      }
      Collections.sort(genericArgs, ARG_COMPARATOR);
      return minNrOfArgs;
    }

    @Nullable
    private ConstructorArg resolve(final int index, final PsiType paramType, Set<ConstructorArg> usedArgs) {
      ConstructorArg arg = resolveIndexed(index, paramType);
      if (arg == null) {
        return resolveGeneric(paramType, usedArgs);
      }
      else {
        return arg;
      }
    }

    @Nullable
    private ConstructorArg resolveGeneric(@Nullable final PsiType requiredType, Set<ConstructorArg> usedArgs) {

      for (final ConstructorArg arg : genericArgs) {
        if (usedArgs.contains(arg)) {
          continue;
        }
        final PsiType type = arg.getType().getValue();
        if (requiredType == null) {
          if (type == null) {
            return arg;
          }
        }
        else {
          if (type != null) {
            if (requiredType.isAssignableFrom(type)) {
              usedArgs.add(arg);
              return arg;
            }
          }
          else if (arg.isAssignable(requiredType)) {
            usedArgs.add(arg);
            return arg;
          }
        }
      }
      return null;
    }

    @Nullable
    private ConstructorArg resolveIndexed(final int index, final PsiType paramType) {
      final ConstructorArg arg = indexedArgs.get(index);
      if (arg != null) {
        final PsiType type = arg.getType().getValue();
        if (type == null || type.isAssignableFrom(paramType)) {
          return arg;
        }
      }
      return null;
    }
  }
}
