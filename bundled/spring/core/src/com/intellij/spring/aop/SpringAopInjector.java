/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.IntroductionManipulator;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import static com.intellij.patterns.DomPatterns.*;
import static com.intellij.patterns.StandardPatterns.*;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.aop.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * @author peter
 */
public class SpringAopInjector implements MultiHostInjector {
  private static final Key<BasicAdvice> SPRING_ADVICE_KEY = Key.create("SpringAdviceKey");
  private static final Key<SpringAspect> SPRING_ASPECT_KEY = Key.create("SpringAspectKey");
  private static final Key<DeclareParents> SPRING_INTRO_KEY = Key.create("SPRING_INTRO_KEY");
  private static final XmlAttributeValuePattern SPRING_AOP_INJECTION_PATTERN = XmlPatterns.xmlAttributeValue().withText(string().longerThan(1)).withParent(
    or(
      XmlPatterns.xmlAttribute("expression").and(
        withDom(
          domElement().withParent(
            domElement(SpringPointcut.class).and(
              not(domElement().withChild("type", genericDomValue(PointcutType.class).withValue(PointcutType.REGEX)))
            ).and(
              optional(domElement().inside(domElement(DomSpringBean.class)))
            ))
        )
      ),
      XmlPatterns.xmlAttribute("pointcut").and(withDom(domElement().withParent(or(
        domElement(Advisor.class),
        domElement(BasicAdvice.class).save(SPRING_ADVICE_KEY).withParent(
          domElement(SpringAspect.class).save(SPRING_ASPECT_KEY)
        )
      ))))
    )
  );
  private static final XmlAttributeValuePattern INTRO_PATTERN = XmlPatterns.xmlAttributeValue().withText(string().longerThan(1)).withParent(
    XmlPatterns.xmlAttribute("types-matching").and(
      withDom(
        domElement().withParent(
          domElement(DeclareParents.class).save(SPRING_INTRO_KEY).withParent(
            domElement(SpringAspect.class).save(SPRING_ASPECT_KEY)
          )
        )
      )
    )
  );

  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    final ProcessingContext context = new ProcessingContext();
    if (SPRING_AOP_INJECTION_PATTERN.accepts(host, context)) {
      final SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(host.getManager(), getBeansFromContext(host));
      host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new SpringAdviceLocalAopModel(host, context.get(SPRING_ADVICE_KEY), searcher));
      registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
      registrar.addPlace(null, null, (PsiLanguageInjectionHost)host, TextRange.from(1, host.getTextLength() - 2));
      registrar.doneInjecting();
    }
    else if (INTRO_PATTERN.accepts(host, context)) {
      final SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(host.getManager(), getBeansFromContext(host));
      host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new SpringLocalAopModel(host, context.get(SPRING_ADVICE_KEY), searcher) {
        @Nullable
        public IntroductionManipulator getIntroductionManipulator() {
          return new IntroductionManipulator() {
            @NotNull
            public PsiElement getCommonProblemElement() {
              return XmlTagUtil.getStartTagNameElement(getIntroduction().getXmlTag());
            }

            @NotNull
            public DeclareParents getIntroduction() {
              return context.get(SPRING_INTRO_KEY);
            }

            public void defineDefaultImpl(final Project project, final ProblemDescriptor descriptor) throws IncorrectOperationException {
            }

            @NonNls
            public String getDefaultImplAttributeName() {
              return getIntroduction().getDefaultImpl().getXmlElementName();
            }

            @NotNull
            public PsiElement getInterfaceElement() {
              return getIntroduction().getImplementInterface().getXmlAttributeValue();
            }

            @Nullable
            public PsiElement getDefaultImplElement() {
              return getIntroduction().getDefaultImpl().getXmlAttributeValue();
            }
          };
        }
      });
      registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
      registrar.addPlace("target(", ")", (PsiLanguageInjectionHost)host, TextRange.from(1, host.getTextLength() - 2));
      registrar.doneInjecting();
    }
  }

  private static List<SpringModel> getBeansFromContext(PsiElement host) {
    final PsiFile file = host.getContainingFile();
    if (file instanceof XmlFile) {
      return SpringUtils.getNonEmptySpringModelsByFile((XmlFile)file);
    }
    return Collections.emptyList();
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class, XmlAttribute.class);
  }

}
