/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.SpringBeanUtil;
import com.intellij.spring.model.converters.SpringConverterUtil;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpringConstructorArgResolveUtil {

  private SpringConstructorArgResolveUtil() {
  }

  public static List<PsiMethod> findMatchingMethods(final SpringBean springBean) {
    final SpringModel model = SpringUtils.getSpringModel(springBean);
    return findMatchingMethods(springBean, model);
  }

  private static List<PsiMethod> findMatchingMethods(final SpringBean springBean,
                                            final SpringModel springModel) {

    final List<PsiMethod> methods = SpringBeanUtil.getInstantiationMethods(springBean);
    if (methods.size() == 0) {
      return methods;
    }
    final Set<ConstructorArg> args = springBean.getAllConstructorArgs();
    final boolean constructorAutowire = SpringAutowireUtil.isConstructorAutowire(springBean);
    Map<Integer, ConstructorArg> indexedArgs = getIndexedConstructorArgs(args);

    List<PsiMethod> accepted = new ArrayList<PsiMethod>(methods.size());
    for (PsiMethod method : methods) {
      final PsiParameter[] parameters = method.getParameterList().getParameters();
      if (acceptMethodByAutowire(constructorAutowire, args, parameters) &&
          acceptMethodByParameterTypes(indexedArgs, constructorAutowire, springModel, args, parameters)) {

        accepted.add(method);
      }
    }

    return accepted;
  }

  public static boolean acceptMethodByAutowire(final boolean constructorAutowire,
                                     final Set<ConstructorArg> args,
                                     final PsiParameter[] parameters) {

    if ((!constructorAutowire && parameters.length != args.size()) || (constructorAutowire && parameters.length < args.size())) {
      return false;
    }

    return true;
  }

  private static boolean acceptMethodByParameterTypes(final Map<Integer, ConstructorArg> indexedArgs,
                                                      final boolean constructorAutowire,
                                                      final SpringModel springModel,
                                                      final Set<ConstructorArg> args,
                                                      final PsiParameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      PsiParameter parameter = parameters[i];
      if (!acceptParameter(parameter, args, indexedArgs, i)) {
        if (constructorAutowire && !SpringAutowireUtil.autowireByType(springModel, parameter.getType()).isEmpty()) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  public static boolean acceptParameter(final PsiParameter parameter,
                                        final Collection<ConstructorArg> list,
                                        final Map<Integer, ConstructorArg> indexedArgs,
                                        final int i) {
    final PsiType psiType = parameter.getType();

    if (indexedArgs.get(i) != null) {
      final ConstructorArg arg = indexedArgs.get(i);

      return hasProperArgumentType(psiType, arg);
    }
    else {
      for (ConstructorArg arg : list) {
        if (indexedArgs.values().contains(arg)) continue;

        if (hasProperArgumentType(psiType, arg)) return true;
      }
    }
    return false;
  }

  private static boolean hasProperArgumentType(final PsiType requiredType, final ConstructorArg arg) {
    final PsiType constructorArgType = SpringBeanUtil.getRequiredType(arg);
    if (constructorArgType != null) {
      return constructorArgType.isAssignableFrom(requiredType);
    } else {
      PsiType[] types = arg.getTypesByValue();
      if (types == null) {
        return false;
      }
      for (PsiType valueType : types) {

        if ((requiredType.isAssignableFrom(valueType) || SpringUtils.isEffectiveClassType(arg, requiredType) ||
         SpringConverterUtil.isConvertable(valueType, requiredType, arg.getManager().getProject())))
          return true;
      }
      return false;
    }
  }

  // todo move it to SpringBean
  public static Map<Integer, ConstructorArg> getIndexedConstructorArgs(final Collection<ConstructorArg> list) {
    Map<Integer, ConstructorArg> indexed = new HashMap<Integer, ConstructorArg>();

    for (ConstructorArg constructorArg : list) {
      final Integer value = constructorArg.getIndex().getValue();
      if (value != null) {
        indexed.put(value, constructorArg);
      }
    }
    return indexed;
  }

  @Nullable
  public static PsiMethod getSpringBeanConstructor(final SpringBean springBean, final SpringModel springModel) {
    if (isInstantiatedByFactory(springBean)) return null;

    final List<PsiMethod> psiMethods =
      SpringConstructorArgResolveUtil.findMatchingMethods(springBean, springModel);

    PsiMethod resolvedConstructor = null;
    for (PsiMethod psiMethod : psiMethods) {
      if (resolvedConstructor == null ||
          resolvedConstructor.getParameterList().getParametersCount() < psiMethod.getParameterList().getParametersCount()) {
        resolvedConstructor = psiMethod;
      }
    }
    return resolvedConstructor;
  }

  public static boolean isInstantiatedByFactory(final SpringBean springBean) {
    return springBean.getFactoryMethod().getXmlAttribute() != null;
  }

  public static boolean hasEmptyConstructor(final SpringBean springBean) {
    final PsiClass beanClass = springBean.getBeanClass(false);
    if (beanClass != null) {
      final PsiMethod[] constructors = beanClass.getConstructors();

      if (constructors.length == 0) return true;

      for (PsiMethod constructor : constructors) {
        if (constructor.getParameterList().getParametersCount() == 0) return true;
      }
    }
    return false;
  }

  public static String suggestParamsForConstructorArgsAsString(final SpringBean springBean) {
    List<String> params = new ArrayList<String>();
    for (PsiParameter psiParameter : SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean)) {
      params.add(psiParameter.getText());
    }

    return StringUtil.join(params, ",");
  }

  public static List<PsiParameter> suggestParamsForConstructorArgs(final SpringBean springBean) {
    List<PsiParameter> methodParameters = new ArrayList<PsiParameter>();

    final Project project = springBean.getManager().getProject();
    final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

    final PsiClass aClass = JavaPsiFacade.getInstance(project).findClass("java.lang.String", GlobalSearchScope.allScope(project));
    assert aClass != null;
    final PsiClassType defaultParamType = elementFactory.createType(aClass);

    JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);

    List<String> existedNames = new ArrayList<String>();
    final List<ConstructorArg> constructorArgs = SpringUtils.getConstructorArgs(springBean);

    final ConstructorArg[] sortedArgs = sortConstructorArgsByIndex(constructorArgs);

    for (ConstructorArg arg : sortedArgs) {
      if (arg == null) continue; // if indexes param od different constructor-args are eqaul 
      PsiType type = arg.getType().getValue();
      if (type == null) {
        PsiType[] psiTypes = arg.getTypesByValue();
        if (psiTypes != null && psiTypes.length > 0) {
          type = psiTypes[0];
        }
      }

      if (type == null || type.equals(PsiType.NULL)) type = defaultParamType;

      SuggestedNameInfo nameInfo = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, null, null, type);
      String name = nameInfo.names[0];

      int i = 1;
      while (existedNames.contains(name)) {
        name += ++i;
      }

      existedNames.add(name);

      try {
        PsiParameter psiParameter = elementFactory.createParameter(name, type);
        methodParameters.add(psiParameter);
      }
      catch (IncorrectOperationException e) {
        throw new RuntimeException(e);
      }
    }
    return methodParameters;
  }

  private static ConstructorArg[] sortConstructorArgsByIndex(final List<ConstructorArg> constructorArgs) {
    ConstructorArg[] args = new ConstructorArg[constructorArgs.size()];

    final Map<Integer, ConstructorArg> indexedConstructorArgs = SpringConstructorArgResolveUtil.getIndexedConstructorArgs(constructorArgs);

    if (indexedConstructorArgs.size() == 0) {
      return constructorArgs.toArray(new ConstructorArg[constructorArgs.size()]);
    }

    List<ConstructorArg> indexed = new ArrayList<ConstructorArg>();
    for (Integer index : indexedConstructorArgs.keySet()) {
      final int i = index.intValue();
      if (i >= 0 && i < args.length) {
        final ConstructorArg arg = indexedConstructorArgs.get(index);
        args[i] = arg;
        indexed.add(arg);
      }
    }

    for (ConstructorArg constructorArg : constructorArgs) {
      if (!indexed.contains(constructorArg)) {
        for (int i = 0; i < args.length; i++) {
          if (args[i] == null) {
            args[i] = constructorArg;
          }
        }
      }
    }

    return args;
  }
}
