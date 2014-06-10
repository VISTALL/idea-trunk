package com.intellij.webBeans.jam;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.jam.JamService;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import static com.intellij.patterns.PsiJavaPatterns.*;
import com.intellij.patterns.PsiMemberPattern;
import com.intellij.psi.*;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemKey;
import com.intellij.semantic.SemRegistrar;
import com.intellij.semantic.SemService;
import com.intellij.util.NullableFunction;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.jam.decorators.WebBeansDecorator;
import com.intellij.webBeans.jam.events.WebBeansFires;
import com.intellij.webBeans.jam.events.WebBeansObserves;
import com.intellij.webBeans.jam.interceptor.WebBeansInterceptor;
import com.intellij.webBeans.jam.specialization.WebBeansSpecializes;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import com.intellij.pom.PomTarget;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class WebBeansSemContributor extends SemContributor {
  private static final SemKey<JamMemberMeta<PsiMember, NamedWebBean>> NAMED_META_KEY = JamService.MEMBER_META_KEY.subKey("WebBeanMeta");
  public static final SemKey<NamedWebBean> NAMED_JAM_KEY = JamService.JAM_ELEMENT_KEY.subKey("WebBean");
  private final SemService mySemService;

  public WebBeansSemContributor(SemService semService) {
    mySemService = semService;
  }

  public void registerSemProviders(SemRegistrar registrar) {
    final PsiMemberPattern.Capture beanPlace = psiMember();
    registrar.registerSemElementProvider(NAMED_META_KEY, beanPlace, new NullableFunction<PsiMember, JamMemberMeta<PsiMember, NamedWebBean>>() {
      public JamMemberMeta<PsiMember, NamedWebBean> fun(final PsiMember member) {
        return calcNamedWebBeanMeta(member);
      }
    });

    registrar.registerSemElementProvider(NAMED_JAM_KEY, beanPlace, new NullableFunction<PsiMember, NamedWebBean>() {
      public NamedWebBean fun(PsiMember member) {
        final JamMemberMeta<PsiMember, NamedWebBean> memberMeta = mySemService.getSemElement(NAMED_META_KEY, member);
        return memberMeta != null ? memberMeta.createJamElement(PsiRef.real(member)) : null;
      }
    });

    WebBeansObserves.META.register(registrar, psiParameter().withAnnotation(WebBeansAnnoConstants.OBSERVES_ANNOTATION));

    WebBeansFires.METHOD_META.register(registrar, psiMethod().withAnnotation(WebBeansAnnoConstants.FIRES_ANNOTATION));
    WebBeansFires.FIELD_META.register(registrar, psiField().withAnnotation(WebBeansAnnoConstants.FIRES_ANNOTATION));

    WebBeansInterceptor.META.register(registrar, psiClass().withAnnotation(WebBeansAnnoConstants.INTERCEPTOR_BINDING_TYPE_ANNOTATION));

    WebBeansDecorator.META.register(registrar, psiClass().withAnnotation(WebBeansAnnoConstants.DECORATOR_ANNOTATION));

    WebBeansSpecializes.CLASS_META.register(registrar, psiClass().withAnnotation(WebBeansAnnoConstants.SPECIALIZES_ANNOTATION));
    WebBeansSpecializes.METHOD_META.register(registrar, psiMethod().withAnnotation(WebBeansAnnoConstants.SPECIALIZES_ANNOTATION));
  }

  private static JamMemberMeta<PsiMember, NamedWebBean> calcNamedWebBeanMeta(PsiMember member) {
    if (AnnotationUtil.isAnnotated(member, WebBeansAnnoConstants.NAMED_ANNOTATION, true)) {
      return createNamedWebBeanMeta(WebBeansAnnoConstants.NAMED_ANNOTATION).addPomTargetProducer(new PairConsumer<NamedWebBean, Consumer<PomTarget>>() {
        public void consume(NamedWebBean namedWebBean, Consumer<PomTarget> consumer) {
            consumer.consume(((NamedWebBean<?>)namedWebBean).getPomTarget());
        }
      });
    }

    final Module module = ModuleUtil.findModuleForPsiElement(member);
    if (module != null) {
      Collection<PsiClass> stereotypeAnnotationClasses =
        WebBeansCommonUtils.getStereotypeAnnotationClasses(module, WebBeansAnnoConstants.NAMED_ANNOTATION);

      for (PsiClass stereotypeAnnotation : stereotypeAnnotationClasses) {
        final String annotationFQN = stereotypeAnnotation.getQualifiedName();
        if (AnnotationUtil.isAnnotated(member, annotationFQN, true)) {
          return createNamedWebBeanMeta(annotationFQN);
        }
      }
    }

    return null;
  }

  private static JamMemberMeta<PsiMember, NamedWebBean> createNamedWebBeanMeta(final String annotationFQN) {
    return new JamMemberMeta<PsiMember, NamedWebBean>(null, NamedWebBean.class, NAMED_JAM_KEY) {
      @Override
      public NamedWebBean createJamElement(PsiRef<PsiMember> psiMemberPsiRef) {
        return createNamedBean(psiMemberPsiRef.getPsiElement(), annotationFQN);
      }
    };
  }

  @Nullable
  private static NamedWebBean createNamedBean(final PsiMember member, final String annoName) {
    if (member instanceof PsiClass && !((PsiClass)member).isAnnotationType()) {
      return new NamedWebBean.ClassMapping((PsiClass)member, annoName);
    }
    if (member instanceof PsiMethod) {
      return new NamedWebBean.ProducerMethodMapping((PsiMethod)member, annoName);
    }
    if (member instanceof PsiField) {
      return new NamedWebBean.FieldMapping((PsiField)member, annoName);
    }
    return null;
  }
}

