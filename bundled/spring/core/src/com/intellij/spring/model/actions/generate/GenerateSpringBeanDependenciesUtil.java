package com.intellij.spring.model.actions.generate;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.codeInsight.template.*;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GenerateSpringBeanDependenciesUtil {

  public static boolean acceptBean(SpringBean springBean, boolean isSetterDependency) {
    return getCandidates(springBean, isSetterDependency).size() > 0;
  }

  public static boolean acceptPsiClass(PsiClass psiClass, boolean isSetterDependency) {
    final SpringModel model = getSpringModel(psiClass);
    if (model == null) return false;

    List<SpringBaseBeanPointer> beansByPsiClass = model.findBeansByPsiClass(psiClass);
    return beansByPsiClass != null && beansByPsiClass.size() > 0 && getCandidates(model, psiClass, isSetterDependency).size() > 0;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependenciesFor(@Nullable SpringModel springModel,
                                                                                                   @Nullable final PsiClass psiClass,
                                                                                                   final boolean isSetterDependency) {
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> createdProperties =
      new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();

    if (springModel != null && psiClass != null) {
      final List<SpringBeanPointer> list =
        SpringUtils.findBeansByClassName(springModel.getAllCommonBeans(true), psiClass.getQualifiedName());
      if (list.size() > 0) {
        for (SpringBeanPointer pointer : list) {
          final CommonSpringBean springBean = pointer.getSpringBean();
          if (springBean instanceof SpringBean && acceptBean((SpringBean)springBean, isSetterDependency)) {
            return ensureFileWritable((SpringBean)springBean)
                   ? generateDependenciesFor((SpringBean)springBean, isSetterDependency)
                   : new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();
          }
        }
      }
      else {
        final List<SpringBeanPointer> beans =
          chooseDependentBeans(getCandidates(springModel, psiClass, isSetterDependency), psiClass.getProject(), isSetterDependency);
        if (beans.size() > 0) {
          return createBeanAndGenerateDependencies(psiClass, isSetterDependency, beans);
        }
      }
    }
    return createdProperties;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> createBeanAndGenerateDependencies(final PsiClass psiClass,
                                                                                                             final boolean isSetterDependency,
                                                                                                             final List<SpringBeanPointer> beans) {
    final CommonSpringBean springBean = beans.get(0).getSpringBean();
    SpringBean bean = null;
    if (springBean instanceof DomSpringBean) {
      final DomSpringBean domSpringBean = (DomSpringBean)springBean;
      bean = createSpingBean(domSpringBean.getParentOfType(Beans.class, false), psiClass);
    }
    return bean == null
           ? new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>()
           : generateDependencies(bean, beans, isSetterDependency);
  }

  @Nullable
  private static SpringBean createSpingBean(final Beans parentBeans, final PsiClass psiClass) {
    if (!ensureFileWritable(parentBeans)) return null;

    final SpringBean springBean = parentBeans.addBean();
    springBean.getClazz().setStringValue(psiClass.getQualifiedName());

    final String[] strings = SpringUtils.suggestBeanNames(springBean);
    springBean.getId().setStringValue(strings.length > 0 ? strings[0] : "");

    return springBean;
  }

  private static boolean ensureFileWritable(final DomElement domElement) {
    return ensureFileWritable(DomUtil.getFile(domElement).getVirtualFile(), domElement.getManager().getProject());
  }

  private static boolean ensureFileWritable(@Nullable VirtualFile virtualFile, final Project project) {
    if (virtualFile != null && !virtualFile.isWritable()) {
      final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFile);
      if (status.hasReadonlyFiles()) return false;
    }
    return true;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependenciesFor(@Nullable final SpringBean springBean,
                                                                                                   final boolean isSetterDependency) {
    if (springBean == null || springBean.getBeanClass() == null) return Collections.emptyList();

    final Project project = springBean.getManager().getProject();
    final List<SpringBeanPointer> dependencies =
      chooseDependentBeans(getCandidates(springBean, isSetterDependency), project, isSetterDependency);

    return generateDependencies(springBean, dependencies, isSetterDependency);
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependencies(@NotNull final SpringBean springBean,
                                                                                                final List<SpringBeanPointer> dependencies,
                                                                                                final boolean isSetterDependency) {
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> springInjections =
      new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();

    final SpringModel model = SpringUtils.getSpringModel(springBean);
    for (SpringBeanPointer bean : dependencies) {
      final Pair<SpringInjection, SpringGenerateTemplatesHolder> pair =
        isSetterDependency ? createDependency(springBean, bean, model) : createConstructorArg(springBean, bean, model);

      if (pair != null) springInjections.add(pair);
    }
    return springInjections;

  }

  @NotNull
  private static List<SpringBeanPointer> chooseDependentBeans(List<SpringBeanClassMember> candidates,
                                                             final Project project,
                                                             final boolean setterDependency) {
    List<SpringBeanPointer> chosenBeans = new ArrayList<SpringBeanPointer>();

    MemberChooser<SpringBeanClassMember> chooser = new MemberChooser<SpringBeanClassMember>(
      candidates.toArray(new SpringBeanClassMember[candidates.size()]), false, setterDependency, project) {
      protected ShowContainersAction getShowContainersAction() {
        return new ShowContainersAction(SpringBundle.message("spring.beans.chooser.show.context.files"), SpringIcons.CONFIG_FILE);
      }

      protected String getAllContainersNodeName() {
        return SpringBundle.message("spring.beans.chooser.all.context.files");
      }
    };

    chooser.setTitle(SpringBundle.message("spring.bean.dependencies.chooser.title"));
    chooser.setCopyJavadocVisible(false);
    chooser.show();

    if (chooser.getExitCode() == MemberChooser.OK_EXIT_CODE) {
      final SpringBeanClassMember[] members = chooser.getSelectedElements(new SpringBeanClassMember[0]);
      if (members != null) {
        for (SpringBeanClassMember member : members) {
          chosenBeans.add(member.getSpringBean());
        }
      }
    }

    return chosenBeans;
  }

  @NotNull
  public static List<SpringBeanClassMember> getCandidates(final SpringBean springBean, final boolean setterDependency) {
    List<SpringBeanClassMember> beanClassMembers = new ArrayList<SpringBeanClassMember>();

    final SpringModel model = SpringUtils.getSpringModel(springBean);
    final PsiClass springBeanClass = springBean.getBeanClass();
    if (springBeanClass != null) {
      final Collection<? extends SpringBaseBeanPointer> allBeans = model.getAllCommonBeans();
      for (final SpringBeanPointer pointer : allBeans) {
        if (pointer.isReferenceTo(springBean)) continue;

        final PsiClass[] dependentBeanClasses = pointer.getEffectiveBeanType();
        if (canBeReferenced(pointer, allBeans) && dependentBeanClasses.length > 0 && !hasDependency(springBean, pointer, setterDependency)) {

          if (setterDependency && !isCompiledElementWithoutSetter(springBeanClass, dependentBeanClasses) || !setterDependency &&
                                                                                                            !isCompiledElementWithoutProperConstructor(
                                                                                                              springBean, model,
                                                                                                              springBean.getBeanClass(),
                                                                                                              dependentBeanClasses)) {
            beanClassMembers.add(new SpringBeanClassMember(pointer));
          }
        }
      }
    }
    return beanClassMembers;
  }

  private static boolean canBeReferenced(final SpringBeanPointer bean, final Collection<? extends SpringBeanPointer> beans) {
    return SpringUtils.getReferencedName(bean, beans) != null;
  }

  public static List<SpringBeanClassMember> getCandidates(@NotNull final SpringModel model,
                                                          PsiClass psiClass,
                                                          final boolean setterDependency) {

    List<SpringBeanClassMember> beanClassMembers = new ArrayList<SpringBeanClassMember>();

    final Collection<? extends SpringBeanPointer> allBeans = model.getAllCommonBeans();
    for (final SpringBeanPointer bean : allBeans) {
      final PsiClass[] dependentBeanClasses = bean.getEffectiveBeanType();

      if (canBeReferenced(bean, allBeans) && dependentBeanClasses.length > 0) {
        if ((setterDependency && !isCompiledElementWithoutSetter(psiClass, dependentBeanClasses)) ||
            (!setterDependency && !isCompiledElementWithoutProperConstructor(null, model, psiClass, dependentBeanClasses))) {
          beanClassMembers.add(new SpringBeanClassMember(bean));
        }
      }
    }
    return beanClassMembers;
  }

  private static boolean isCompiledElementWithoutProperConstructor(@Nullable final SpringBean springBean,
                                                                   final SpringModel model,
                                                                   final PsiClass springBeanClass,
                                                                   final PsiClass[] beanClasses) {
    if (springBeanClass instanceof PsiCompiledElement || springBeanClass.getOriginalElement() instanceof PsiCompiledElement) {

      if (springBean != null) {
        for (PsiClass beanClass : beanClasses) {
          if (getCompiledElementCandidateConstructor(springBean, springBeanClass, beanClass) != null) {
            return false;
          }
        }
      }
      else {
        List<SpringBeanPointer> list = SpringUtils.findBeansByClassName(model.getAllCommonBeans(true), springBeanClass.getQualifiedName());
        for (PsiClass beanClass : beanClasses) {
          for (SpringBeanPointer pointer : list) {
            final CommonSpringBean bean = pointer.getSpringBean();
            if (bean instanceof SpringBean &&
                getCompiledElementCandidateConstructor((SpringBean)bean, springBeanClass, beanClass) != null) {
              return false;
            }
          }
        }
        for (PsiMethod constructor : springBeanClass.getConstructors()) {
          if (constructor.getParameterList().getParametersCount() == 1) {
            PsiType type = constructor.getParameterList().getParameters()[0].getType();
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(springBeanClass.getProject()).getElementFactory();
            for (PsiClass beanClass : beanClasses) {
              PsiClassType classType = psiElementFactory.createType(beanClass);
              if (type.isAssignableFrom(classType)) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }
    return false;
  }

  @Nullable
  private static PsiMethod getCompiledElementCandidateConstructor(final SpringBean currentBean,
                                                                  final PsiClass currentBeanClass,
                                                                  final PsiClass candidateParameterClass) {

    PsiType candidatePsiType = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory().createType(candidateParameterClass);

    if (SpringUtils.getConstructorArgs(currentBean).size() == 0) {
      return findConstructor(currentBeanClass.getConstructors(), Collections.singletonList(candidatePsiType));
    }
    else {
      final List<PsiMethod> methods = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);

      for (PsiMethod method : methods) {
        final List<PsiType> psiParameterTypes = getParameterTypes(method);
        psiParameterTypes.add(candidatePsiType);
        PsiMethod existedConstructor = findConstructor(currentBeanClass.getConstructors(), psiParameterTypes);
        if (existedConstructor != null) return existedConstructor;
      }
    }
    return null;
  }

  private static PsiMethod findConstructor(final PsiMethod[] constructors, final List<PsiType> psiParameterTypes) {
    for (PsiMethod constructor : constructors) {
      if (constructor.getParameterList().getParametersCount() == psiParameterTypes.size()) {
        boolean isAccepted = true;
        final PsiParameter[] parameters = constructor.getParameterList().getParameters();
        for (int i = 0; i < psiParameterTypes.size(); i++) {
          if (!psiParameterTypes.get(i).isAssignableFrom(parameters[i].getType())) {
            isAccepted = false;
            break;
          }
        }
        if (isAccepted) return constructor;
      }
    }
    return null;
  }

  private static List<PsiType> getParameterTypes(final PsiMethod method) {
    List<PsiType> psiParameterTypes = new ArrayList<PsiType>();
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    for (PsiParameter parameter : parameters) {
      psiParameterTypes.add(parameter.getType());
    }
    return psiParameterTypes;
  }

  private static boolean isCompiledElementWithoutSetter(final PsiClass springBeanClass, final PsiClass[] beanClasses) {
    if (springBeanClass instanceof PsiCompiledElement || springBeanClass.getOriginalElement() instanceof PsiCompiledElement) {
      for (PsiClass beanClass : beanClasses) {
        if (getExistedSetter(springBeanClass, beanClass) != null) return false;
      }
      return true;
    }

    return false;
  }

  private static boolean hasDependency(final CommonSpringBean currentBean,
                                       final SpringBeanPointer candidateBean,
                                       final boolean isSetterDependency) {
    return isSetterDependency
           ? SpringUtils.getSetterDependencies(currentBean).contains(candidateBean)
           : SpringUtils.getConstructorDependencies(currentBean).contains(candidateBean);
  }

  @Nullable
  private static Pair<SpringInjection, SpringGenerateTemplatesHolder> createDependency(final SpringBean currentBean,
                                                                                       final SpringBeanPointer bean, final SpringModel model) {

    final SpringGenerateTemplatesHolder templatesHolder = new SpringGenerateTemplatesHolder(currentBean.getManager().getProject());

    final PsiClass currentBeanClass = currentBean.getBeanClass();
    final PsiClass[] candidateBeanClasses = bean.getEffectiveBeanType();

    if (currentBeanClass != null && candidateBeanClasses.length > 0) {
      final PsiMethod setter = getOrCreateSetter(bean, currentBeanClass, candidateBeanClasses, templatesHolder, model);

      if (setter != null) {
        final SpringProperty property = currentBean.addProperty();
        property.getName().ensureXmlElementExists();
        property.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(setter));
        property.getRefAttr().setStringValue(getReferencedName(currentBean, bean));
        return new Pair<SpringInjection, SpringGenerateTemplatesHolder>(property, templatesHolder);
      }
    }

    return null;
  }

  @Nullable
  private static Pair<SpringInjection, SpringGenerateTemplatesHolder> createConstructorArg(final SpringBean currentBean,
                                                                                           final SpringBeanPointer bean,
                                                                                           final SpringModel model) {
    ConstructorArg arg = null;
    final SpringGenerateTemplatesHolder holder = new SpringGenerateTemplatesHolder(currentBean.getManager().getProject());

    final PsiClass currentBeanClass = currentBean.getBeanClass();
    final PsiClass[] candidateBeanClasses = bean.getEffectiveBeanType();

    if (currentBeanClass != null && candidateBeanClasses.length > 0) {
      PsiMethod existedConstructor = findExistedConstructor(currentBean, currentBeanClass, candidateBeanClasses);

      if (existedConstructor == null) {
        if (!ensureFileWritable(currentBeanClass.getContainingFile().getVirtualFile(), currentBeanClass.getProject())) return null;

        existedConstructor = findProperConstructorAndAddParameter(currentBean, bean, currentBeanClass, candidateBeanClasses, holder, model);
      }
      arg = currentBean.addConstructorArg();
      arg.getRefAttr().setStringValue(getReferencedName(currentBean, bean));
      if (existedConstructor == null && SpringConstructorArgResolveUtil.findMatchingMethods(currentBean).size() == 0) {
        final PsiMethod psiMethod = createConstructor(currentBean);
        if (psiMethod.getParameterList().getParametersCount() == 1) {
          final PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
          final PsiType type = parameter.getType();
          if (type instanceof PsiClassType) {
            final PsiClass psiClass = ((PsiClassType)type).resolve();
            if (psiClass != null) {
              addCreateSetterTemplate(psiMethod, new PsiClass[]{psiClass}, bean, holder, model);
            }
          }
        }
      }
    }

    return new Pair<SpringInjection, SpringGenerateTemplatesHolder>(arg, holder);
  }

  @Nullable
  private static String getReferencedName(final SpringBean currentBean, final SpringBeanPointer bean) {
    final SpringModel model = SpringUtils.getSpringModel(currentBean);
    return model != null ? SpringUtils.getReferencedName(bean, model.getAllCommonBeans(true)) : null;
  }

  @Nullable
  private static PsiMethod getOrCreateSetter(final SpringBeanPointer candidateBean,
                                             final PsiClass currentBeanClass,
                                             final PsiClass[] candidateBeanClasses,
                                             final SpringGenerateTemplatesHolder templatesHolder, final SpringModel model) {
    for (PsiClass candidateBeanClass : candidateBeanClasses) {
      final PsiMethod existedSetter = getExistedSetter(currentBeanClass, candidateBeanClass);
      if (existedSetter != null) return existedSetter;
    }

    final boolean isWritable = ensureFileWritable(currentBeanClass.getContainingFile().getVirtualFile(), currentBeanClass.getProject());
    if (!isWritable) return null;

    final PsiMethod setter = createSetter(candidateBean, currentBeanClass, candidateBeanClasses);

    addCreateSetterTemplate(setter, candidateBeanClasses, candidateBean, templatesHolder, model);

    return setter;
  }

  @Nullable
  private static PsiMethod findExistedConstructor(final SpringBean currentBean,
                                                  final PsiClass currentBeanClass,
                                                  final PsiClass[] candidateParameterClasses) {
    final List<PsiMethod> constructors = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);
    for (PsiClass candidateBeanClass : candidateParameterClasses) {
      for (PsiMethod constructor : constructors) {
        final List<PsiType> psiParameterTypes = getParameterTypes(constructor);
        final PsiClassType candidateBeanType = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory().createType(candidateBeanClass);

        psiParameterTypes.add(candidateBeanType);

        final PsiMethod existedConstructorWithRequiredParameter = findConstructor(currentBeanClass.getConstructors(), psiParameterTypes);
        if (existedConstructorWithRequiredParameter != null) return existedConstructorWithRequiredParameter;
      }
    }
    return null;
  }

  @Nullable
  private static PsiMethod findProperConstructorAndAddParameter(final SpringBean currentBean,
                                                                final SpringBeanPointer bean,
                                                                final PsiClass currentBeanClass,
                                                                final PsiClass[] candidateParameterClasses,
                                                                final SpringGenerateTemplatesHolder holder, final SpringModel model) {
    PsiMethod properConstructor = null;
    final List<PsiMethod> constructors = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);
    for (PsiClass candidateBeanClass : candidateParameterClasses) {
      for (PsiMethod constructor : constructors) {
        if (properConstructor == null ||
            properConstructor.getParameterList().getParametersCount() < constructor.getParameterList().getParametersCount()) {
          properConstructor = constructor;
        }
      }

      if (properConstructor != null) {
        addConstructorParameter(currentBeanClass, candidateBeanClass, properConstructor);
        addCreateSetterTemplate(properConstructor, new PsiClass[]{candidateBeanClass}, bean, holder,
                                properConstructor.getParameterList().getParametersCount() - 1, model);

        return properConstructor;
      }
    }
    return null;
  }

  @NotNull
  private static PsiMethod createConstructor(final SpringBean springBean) {
    PsiClass instantiationClass = null;
    PsiMethod instantiationMethod = null;

    final PsiClass beanClass = springBean.getBeanClass();
    try {
      assert beanClass != null;
      final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

      if (isInstantiatedByFactory(springBean)) {
        SpringBeanPointer beanPointer = springBean.getFactoryBean().getValue();
        if (beanPointer != null) {
          instantiationClass = beanPointer.getBeanClass();
          String methodName = getInstantiationMethodName(instantiationClass, springBean);
          @NonNls String methodText = PsiModifier.PUBLIC + " " + beanClass.getName() + " " + methodName + "() { return null; }";
          instantiationMethod = elementFactory.createMethodFromText(methodText, null);
        }
      }
      else if (isInstantiatedByFactoryMethod(springBean)) {
        instantiationClass = beanClass;
        String methodName = getInstantiationMethodName(instantiationClass, springBean);
        @NonNls String methodText =
          PsiModifier.PUBLIC + " " + PsiModifier.STATIC + " " + beanClass.getName() + " " + methodName + "() { return null; }";
        instantiationMethod = elementFactory.createMethodFromText(methodText, null);
      }
      else {
        instantiationClass = beanClass;
        instantiationMethod = elementFactory.createConstructor();
      }

      final List<PsiParameter> parameters = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean);
      assert instantiationMethod != null;
      for (PsiParameter parameter : parameters) {
        instantiationMethod.getParameterList().add(parameter);
      }
      assert instantiationClass != null;
      instantiationMethod = (PsiMethod)instantiationClass.add(instantiationMethod);

    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }
    return instantiationMethod;
  }

  @NotNull private static String getInstantiationMethodName(final PsiClass factoryBeanClass, final SpringBean springBean) {
    String methodName = springBean.getFactoryMethod().getStringValue();
    if (!StringUtil.isEmptyOrSpaces(methodName)) return methodName;
    PsiClass beanClass = springBean.getBeanClass();

    @NonNls String methodPrefix = "create";
    methodName = methodPrefix + beanClass.getName();
    int i = 0;
    while (factoryBeanClass.findMethodsByName(methodName, true).length > 0) {
      methodName = methodPrefix + beanClass.getName() + (++i);
    }

    return methodName;
  }

  private static boolean isInstantiatedByFactoryMethod(final SpringBean springBean) {
    return DomUtil.hasXml(springBean.getFactoryMethod());
  }

  private static boolean isInstantiatedByFactory(final SpringBean springBean) {
    return DomUtil.hasXml(springBean.getFactoryBean());
  }

  private static void addConstructorParameter(final PsiClass currentBeanClass,
                                              final PsiClass candidateBeanClass,
                                              final PsiMethod constructor) {
    final PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory();
    try {
      final PsiClassType psiClassType = psiElementFactory.createType(candidateBeanClass);
      SuggestedNameInfo nameInfo =
        JavaCodeStyleManager.getInstance(currentBeanClass.getProject()).suggestVariableName(VariableKind.PARAMETER, null, null, psiClassType);
      String name = nameInfo.names[0];
      int i = 0;
      while (hasSuchName(constructor.getParameterList().getParameters(), name)) {
        name += ++i;
      }

      final PsiParameter parameter = psiElementFactory.createParameter(name, psiClassType);
      constructor.getParameterList().add(parameter);
    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean hasSuchName(final PsiParameter[] parameters, final String name) {
    for (PsiParameter parameter : parameters) {
      if (name.equals(parameter.getName())) return true;
    }
    return false;
  }

  @NotNull
  private static PsiMethod createSetter(final SpringBeanPointer candidateBean,
                                        final PsiClass currentBeanClass,
                                        final PsiClass[] candidateBeanClasses) {
    PsiMethod method;
    try {
      final PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getNameHelper();

      final String beanName = candidateBean.getName();
      final String name = beanName == null || !psiNameHelper.isIdentifier(beanName) ? candidateBeanClasses[0].getName() : beanName;

      final PsiManager psiManager = PsiManager.getInstance(currentBeanClass.getProject());
      final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory();

      @NonNls final String methodText = "public void set" + StringUtil.capitalize(name) + "(" + candidateBeanClasses[0].getQualifiedName() +
                                        " " + StringUtil.decapitalize(name) + ") { }";

      method = elementFactory.createMethodFromText(methodText, null);
      method = (PsiMethod)currentBeanClass.add(method);

      final CodeStyleManager formatter = CodeStyleManager.getInstance(psiManager.getProject());
      final JavaCodeStyleManager styler = JavaCodeStyleManager.getInstance(psiManager.getProject());
      styler.shortenClassReferences(formatter.reformat(method));
    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }

    return method;
  }

  private static void addCreateSetterTemplate(final PsiMethod method,
                                              final PsiClass[] psiClasses,
                                              final SpringBeanPointer bean,
                                              final SpringGenerateTemplatesHolder templatesHolder, final SpringModel model) {
    addCreateSetterTemplate(method, psiClasses, bean, templatesHolder, 0, model);
  }

  private static void addCreateSetterTemplate(final PsiMethod method,
                                              final PsiClass[] psiClasses,
                                              final SpringBeanPointer bean,
                                              final SpringGenerateTemplatesHolder templatesHolder,
                                              final int paramId,
                                              final SpringModel model) {
    templatesHolder.addTemplateFactory(method.getParameterList(), new Factory<Template>() {
      public Template create() {
        final PsiParameter parameter = method.getParameterList().getParameters()[paramId];
        final PsiTypeElement typeElement = parameter.getTypeElement();

        final Collection<PsiClass> variants = getSuperTypeVariants(psiClasses);
        final Expression interfaces = getSuperTypesExpression(typeElement.getType().getCanonicalText(), variants);

        final Expression ids = getSuggestNamesExpression(method, bean, paramId, model);

        final TemplateBuilderImpl builder = new TemplateBuilderImpl(method.getParameterList());

        if (variants.size() > 1) {
          builder.replaceElement(typeElement, "type", interfaces, true);
        }
        builder.replaceElement(parameter.getNameIdentifier(), "names", ids, true);

        return builder.buildInlineTemplate();
      }
    });
  }

  private static Collection<PsiClass> getSuperTypeVariants(final PsiClass[] psiClasses) {
    Collection<PsiClass> variants = new HashSet<PsiClass>();
    for (PsiClass beanClass : psiClasses) {
      variants.add(beanClass);
      variants.addAll(Arrays.asList(beanClass.getInterfaces()));

      for (PsiClass psiClass : beanClass.getSupers()) {
        if (Object.class.getName().equals(psiClass.getQualifiedName())) continue;
        variants.add(psiClass);
      }
    }
    return variants;
  }

  private static Expression getSuggestNamesExpression(final PsiMethod method, final SpringBeanPointer bean, final int paramId,
                                                      final SpringModel model) {
    final PsiParameter parameter = method.getParameterList().getParameters()[paramId];
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
        final PsiIdentifier psiIdentifier = parameter.getNameIdentifier();

        return new TextResult(psiIdentifier != null ? psiIdentifier.getText() : "foo");
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (String name : getSuggestedNames()) {
          items.add(LookupItemUtil.objectToLookupItem(name));
        }

        return items.toArray(new LookupElement[items.size()]);
      }

      private Collection<String> getSuggestedNames() {
        final PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(method.getProject()).getNameHelper();
        final Set<String> names = new HashSet<String>();
        final String beanName = bean.getName();
        if (beanName != null) {
          for (String name : model.getAllBeanNames(beanName)) {
            if (psiNameHelper.isIdentifier(name)) {
              names.add(name);
            }
          }
        }

        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(method.getProject());
        final PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length < paramId) {
          final SuggestedNameInfo info = codeStyleManager
            .suggestVariableName(VariableKind.PARAMETER, null, null, parameters[paramId].getType());

          names.addAll(Arrays.asList(info.names));
        }

        return names;
      }
    };
  }

  private static Expression getSuperTypesExpression(final String psiType, final Collection<PsiClass> psiClasses) {
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        return new TextResult(psiType);
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (PsiClass psiClass : psiClasses) {
          items.add(LookupItemUtil.objectToLookupItem(psiClass));
        }
        return items.toArray(new LookupElement[items.size()]);
      }
    };
  }


  @Nullable
  private static PsiMethod getExistedSetter(final PsiClass currentBeanClass, final PsiClass setterPsiClass) {
    final PsiClassType psiClassType = JavaPsiFacade.getInstance(setterPsiClass.getProject()).getElementFactory().createType(setterPsiClass);

    for (PsiMethod psiMethod : currentBeanClass.getAllMethods()) {
      if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
        final PsiType type = psiMethod.getParameterList().getParameters()[0].getType();
        if (type.isAssignableFrom(psiClassType)) {
          return psiMethod;
        }
      }
    }
    return null;
  }

  @Nullable
  public static Module getSpringModule(@NotNull PsiClass psiClass) {
    ProjectFileIndex index = ProjectRootManagerEx.getInstanceEx(psiClass.getProject()).getFileIndex();

    PsiFile psiFile = psiClass.getContainingFile();

    VirtualFile virtualFile = psiFile.getVirtualFile();

    if (virtualFile == null) return null;

    if (index.isLibraryClassFile(virtualFile) || index.isInLibrarySource(virtualFile)) {
      List<OrderEntry> orderEntries = index.getOrderEntriesForFile(virtualFile);
      for (OrderEntry orderEntry : orderEntries) {
        Module module = orderEntry.getOwnerModule();
        if (isSpringModule(module)) {
          return module;
        }
      }
    }

    Module module = index.getModuleForFile(virtualFile);
    return isSpringModule(module) ? module : null;
  }

  private static boolean isSpringModule(final Module module) {
    return module != null && SpringFacet.getInstance(module) != null;
  }

  @Nullable
  public static SpringModel getSpringModel(@Nullable PsiClass psiClass) {
    if (psiClass == null) return null;

    Module module = getSpringModule(psiClass);

    return module != null ? SpringManager.getInstance(psiClass.getProject()).getCombinedModel(module) : null;
  }
}
