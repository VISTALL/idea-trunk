/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.aop.AopBundle;
import static com.intellij.aop.psi.AopElementTypes.AOP_LEFT_PAR;
import static com.intellij.aop.psi.AopElementTypes.AOP_RIGHT_PAR;
import com.intellij.aop.psi.AopPointcutTypes;
import static com.intellij.aop.psi.AopPrattParser.parsePatternPart;
import com.intellij.aop.psi.PointcutDescriptor;
import com.intellij.aop.psi.PsiBeanPointcutExpression;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.lang.ASTNode;
import com.intellij.lang.pratt.PrattBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.patterns.DomPatterns;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.meta.MetaDataRegistrar;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.refactoring.rename.RenameInputValidatorRegistry;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.metadata.SpringBeanMetaData;
import com.intellij.spring.metadata.SpringStereotypeQualifierMetaData;
import com.intellij.spring.model.highlighting.*;
import com.intellij.spring.model.highlighting.jam.SpringJavaConfigExternalBeansErrorInspection;
import com.intellij.spring.model.highlighting.jam.SpringJavaConfigInconsistencyInspection;
import com.intellij.spring.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.spring.model.jam.stereotype.SpringComponent;
import com.intellij.spring.model.jam.stereotype.SpringController;
import com.intellij.spring.model.jam.stereotype.SpringRepository;
import com.intellij.spring.model.jam.stereotype.SpringService;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.Function;
import com.intellij.util.Icons;
import com.intellij.util.NullableFunction;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.TypeNameManager;
import org.jdom.Document;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringApplicationComponent implements ApplicationComponent, InspectionToolProvider, Disposable {

  private static final Logger LOG = Logger.getInstance(SpringApplicationComponent.class.getName());
  @NonNls private static final String LIVE_TEMPLATES_DIR = "/liveTemplates/";
  @NonNls private static final String[] LIVE_TEMPLATES_FILES =
    {"spring.xml", "aop.xml", "dataAccess.xml", "scheduling.xml", "integration.xml", "commonBeans.xml", "webflow.xml", "osgi.xml"};

  static {
    AopPointcutTypes.registerPointcut(new PointcutDescriptor("bean") {
      public void parseToken(final PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
          parsePatternPart(builder);
          builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        }
      }

      public PsiPointcutExpression createPsi(final ASTNode node) {
        return new PsiBeanPointcutExpression(node);
      }
    });
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {

    FacetTypeRegistry.getInstance().registerFacetType(SpringFacetType.INSTANCE);

    registerPresentations();

    registerLiveTemplates();

    registerMetaData();

    ExternalResourceManagerEx.getInstanceEx().registerImplicitNamespace(SpringConstants.P_NAMESPACE, new PNamespaceDescriptor(), this);

    RenameInputValidatorRegistry.getInstance()
      .registerInputValidator(DomPatterns.domTargetElement(DomPatterns.domElement(SpringBean.class)), new RenameInputValidator() {
        public boolean isInputValid(String newName, PsiElement element, ProcessingContext context) {
          return true;
        }
      });
  }

  private static void registerMetaData() {
    MetaDataRegistrar.getInstance().registerMetaData(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof XmlTag) {
          final XmlTag tag = (XmlTag)element;
          final DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
          if (!(domElement instanceof DomSpringBean)) return false;

          if (!(domElement instanceof CustomBeanWrapper)) return true;
          if (!((CustomBeanWrapper)domElement).isParsed()) return true;
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return XmlTag.class.isAssignableFrom(hintClass);
      }
    }, SpringBeanMetaData.class);

    MetaDataRegistrar.getInstance().registerMetaData(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof PsiAnnotation) {
          Module module = ModuleUtil.findModuleForPsiElement(context);
          if (module != null) {
            for (PsiClass psiClass : JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module)) {
              PsiAnnotation annotation = (PsiAnnotation)element;
              if (annotation.getQualifiedName().equals(psiClass.getQualifiedName())) {
                return true;
              }
            }
          }
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return PsiAnnotation.class.isAssignableFrom(hintClass);
      }
    }, SpringStereotypeQualifierMetaData.class);
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  private static void registerPresentations() {
    ElementPresentationManager.registerNameProvider(new Function<Object, String>() {

      @Nullable
      public String fun(final Object s) {

        if (s instanceof CommonSpringBean) {
          final CommonSpringBean springBean = (CommonSpringBean)s;
          final String beanName = springBean.getBeanName();
          if (beanName != null) return beanName;
          final PsiClass beanClass = springBean.getBeanClass();
          if (beanClass != null) {
            return beanClass.getName();
          }

          return SpringBundle.message("spring.bean.with.unknown.name");
        }
        else if (s instanceof BeanProperty) {
          return ((BeanProperty)s).getName();
        }
        else if (s instanceof SpringBeanPointer) {
          return ((SpringBeanPointer)s).getName();
        }
        else if (s instanceof SpringImport) {
          return ((SpringImport)s).getResource().getStringValue();
        }
        return null;
      }
    });

    ElementPresentationManager.registerDocumentationProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof SpringBean) {
          return ((SpringBean)o).getDescription().getStringValue();
        }
        return null;
      }
    });

    TypeNameManager.registerTypeName(SpringProperty.class, SpringBundle.message("spring.property"));

    ElementPresentationManager.registerIcons(Beans.class, SpringIcons.SPRING_BEANS_ICON);
    ElementPresentationManager.registerIcons(SpringBean.class, SpringIcons.SPRING_BEAN_ICON);
    ElementPresentationManager.registerIcons(PsiMethod.class, Icons.METHOD_ICON);
    ElementPresentationManager.registerIcons(SpringBeanScope.class, SpringIcons.SPRING_BEAN_SCOPE_ICON);
    ElementPresentationManager.registerIcons(SpringProperty.class, SpringIcons.SPRING_BEAN_PROPERTY_ICON);
    ElementPresentationManager.registerIcons(BeanProperty.class, SpringIcons.SPRING_BEAN_PROPERTY_ICON);
    ElementPresentationManager.registerIcons(SpringImport.class, SpringIcons.CONFIG_FILE);

    TypeNameManager.registerTypeName(SpringJamQualifier.class, "@Qualifier");
    TypeNameManager.registerTypeName(SpringService.class, "@Service");
    TypeNameManager.registerTypeName(SpringRepository.class, "@Repository");
    TypeNameManager.registerTypeName(SpringController.class, "@Controller");
    TypeNameManager.registerTypeName(SpringComponent.class, "@Component");
  }

  public Class[] getInspectionClasses() {
    return getSpringInspectionClasses();
  }

  public static Class<? extends LocalInspectionTool>[] getSpringInspectionClasses() {
    return new Class[]{SpringModelInspection.class, SpringScopesInspection.class, SpringBeanNameConventionInspection.class,
      InjectionValueTypeInspection.class, SpringAutowiringInspection.class, SpringConstructorArgInspection.class,
      FactoryMethodInspection.class, SpringDependencyCheckInspection.class, LookupMethodInspection.class,
      InjectionValueStyleInspection.class, ReplacedMethodsInspection.class, InjectionValueConsistencyInspection.class,
      AbstractBeanReferencesInspection.class, AutowiredDependenciesInspection.class, DuplicatedBeanNamesInspection.class,
      UtilSchemaInspection.class, SpringBeanInstantiationInspection.class, SpringJavaConfigExternalBeansErrorInspection.class, SpringAopErrorsInspection.class,
      SpringAopWarningsInspection.class, SpringFacetInspection.class, MissingAspectjAutoproxyInspection.class,
      SpringJavaAutowiringInspection.class, SpringRequiredAnnotationInspection.class, SpringRequiredPropertyInspection.class,
      UnparsedCustomBeanInspection.class, SpringJavaConfigInconsistencyInspection.class,
      JdkProxiedBeanTypeInspection.class, RequiredBeanTypeInspection.class};
  }

  private void registerLiveTemplates() {
    final TemplateSettings settings = TemplateSettings.getInstance();
    for (String templatesFile : LIVE_TEMPLATES_FILES) {
      try {
        final Document document = JDOMUtil.loadDocument(getClass().getResourceAsStream(LIVE_TEMPLATES_DIR + templatesFile));
        settings.readHiddenTemplateFile(document);
      }
      catch (Exception e) {
        LOG.error("Can't load " + templatesFile, e);
      }
    }
  }

}
