package com.intellij.spring.model.jam;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.jam.JamService;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.PsiClassPattern;
import static com.intellij.patterns.PsiJavaPatterns.psiClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiRef;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemKey;
import com.intellij.semantic.SemRegistrar;
import com.intellij.semantic.SemService;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.model.jam.javaConfig.JavaSpringConfiguration;
import com.intellij.spring.model.jam.stereotype.*;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.util.NullableFunction;

import java.util.List;

public class SpringSemContributor extends SemContributor {
  private static final SemKey<JamMemberMeta<PsiClass, CustomSpringComponent>> CUSTOM_COMPONENT_META_KEY = JamService.MEMBER_META_KEY.subKey("CustomSpringComponentMeta");
  public static final SemKey<CustomSpringComponent> CUSTOM_COMPONENT_JAM_KEY = JamService.JAM_ELEMENT_KEY.subKey("CustomSpringComponent");

  private final SemService mySemService;

  public SpringSemContributor(SemService semService) {
    mySemService = semService;
  }

  public void registerSemProviders(SemRegistrar registrar) {
    PsiClassPattern psiClassPattern = psiClass().nonAnnotationType();

    JavaConfigConfiguration.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION));
    JavaSpringConfiguration.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION));

    //JavaSpringConfiguration.BEANS_METHOD_META.register(registrar, PsiJavaPatterns.psiMethod().withAnnotation(SpringAnnotationsConstants.JAVA_SPRING_BEAN_ANNOTATION));

    SpringComponent.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.COMPONENT_ANNOTATION));
    SpringController.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.CONTROLLER_ANNOTATION));
    SpringService.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.SERVICE_ANNOTATION));
    SpringRepository.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.REPOSITORY_ANNOTATION));

    // register custom components

    registrar.registerSemElementProvider(CUSTOM_COMPONENT_META_KEY, psiClassPattern, new NullableFunction<PsiClass, JamMemberMeta<PsiClass, CustomSpringComponent>>() {
      public JamMemberMeta<PsiClass, CustomSpringComponent> fun(final PsiClass member) {
        return calcNamedWebBeanMeta(member);
      }
    });

    registrar.registerSemElementProvider(CUSTOM_COMPONENT_JAM_KEY, psiClassPattern, new NullableFunction<PsiClass, CustomSpringComponent>() {
      public CustomSpringComponent fun(PsiClass member) {
        final JamMemberMeta<PsiClass, CustomSpringComponent> memberMeta = mySemService.getSemElement(CUSTOM_COMPONENT_META_KEY, member);
        return memberMeta != null ? memberMeta.createJamElement(PsiRef.real(member)) : null;
      }
    });
  }


  private static JamMemberMeta<PsiClass, CustomSpringComponent> calcNamedWebBeanMeta(PsiClass psiClass) {
    if (psiClass.isAnnotationType()) return null;
    
    final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
     if (module != null) {
       List<String> customComponentAnnotations = JamAnnotationTypeUtil.getUserDefinedCustomComponentAnnotations(module);

       for (String anno : customComponentAnnotations) {
         if (AnnotationUtil.isAnnotated(psiClass, anno, true) ) {
           return createCustomSpringComponentJamMemberMeta(anno);
         }
       }
     }

     return null;
   }

  private static JamMemberMeta<PsiClass, CustomSpringComponent> createCustomSpringComponentJamMemberMeta(final String annotationFQN) {
    return new JamMemberMeta<PsiClass, CustomSpringComponent>(null, CustomSpringComponent.class, CUSTOM_COMPONENT_JAM_KEY) {
      @Override
      public CustomSpringComponent createJamElement(PsiRef<PsiClass> psiMemberPsiRef) {
        return new CustomSpringComponent(annotationFQN, psiMemberPsiRef.getPsiElement());
      }
    };
  }
}