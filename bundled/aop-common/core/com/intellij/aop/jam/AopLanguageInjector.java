/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.*;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.javaee.util.AnnotationTextUtil;
import com.intellij.lang.injection.ConcatenationAwareInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author peter
 */
public class AopLanguageInjector implements ConcatenationAwareInjector {
  private static final Logger LOG = Logger.getInstance("#com.intellij.aop.jam.AopLanguageInjector");
  private static final Set<String> POINTCUT_ANNOTATIONS = new THashSet<String>(Arrays.asList(AopConstants.POINTCUT_ANNO, AopConstants.AFTER_ANNO, AopConstants.AFTER_RETURNING_ANNO, AopConstants.AFTER_THROWING_ANNO, AopConstants.AROUND_ANNO, AopConstants.BEFORE_ANNO));
  private static final Key<PsiMethod> AOP_METHOD_KEY = Key.create("AopMethod");
  private static final Key<PsiField> AOP_FIELD_KEY = Key.create("AopField");
  private static final ElementPattern AOP_ANNO_PATTERN =
    PsiJavaPatterns.literalExpression().withText(string().longerThan(1)).and(or(
      PsiJavaPatterns.psiElement().insideAnnotationParam(string().oneOf(POINTCUT_ANNOTATIONS), "value"),
      PsiJavaPatterns.psiElement().insideAnnotationParam(string().oneOf(AopConstants.AFTER_RETURNING_ANNO, AopConstants.AFTER_THROWING_ANNO), AopConstants.POINTCUT_PARAM)
    )
    ).inside(true, PsiJavaPatterns.psiMethod().save(AOP_METHOD_KEY));
  private static final ElementPattern AOP_INTRO_PATTERN =
    PsiJavaPatterns.literalExpression().withText(string().longerThan(1)).annotationParam(AopConstants.DECLARE_PARENTS_ANNO, "value").inside(true,
                                                                                                                        PsiJavaPatterns.psiField().save(AOP_FIELD_KEY));

  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement... operands) {
    PsiElement host = operands[0];
    final ProcessingContext context = new ProcessingContext();
    if (AOP_ANNO_PATTERN.accepts(host, context)) {
      final AopAdvisedElementsSearcher searcher = getAopAdvisedElementsSearcher(host);
      if (searcher != null) {
        final PsiMethod method = context.get(AOP_METHOD_KEY);
        if (method != null) {
          LocalAopModel model = new LocalAopModel(host, method, searcher) {
            @NotNull
            @Override
            public ArgNamesManipulator getArgNamesManipulator() {
              PointcutContainer container = AopModuleService.getAdvice(method);
              if (container == null) {
                container = AopModuleService.getPointcut(method);
              }
              if (container == null) {
                container = AopModuleService.getAdvice(method);
                if (container == null) {
                  container = AopModuleService.getPointcut(method);
                }

                final PsiModifierList modifierList = method.getContainingClass().getModifierList();
                final String modifiers = modifierList == null ? "NOMODIFIERS" : modifierList.getText();
                LOG.error("No AOP JAM for method: " + method.getClass().getName() + "; modifiers: " + method.getModifierList().getText() + "; in class: " + modifiers + "; again: " + container);
              }
              return new JavaArgNamesManipulator(container);
            }
          };
          host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, model);
          List<PsiLanguageInjectionHost> validOperands = validStringLiteralExpressions(operands);
          if (!validOperands.isEmpty()) {
            registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
            for (PsiLanguageInjectionHost operand : validOperands) {
              TextRange range = getLiteralRange(operand);
              registrar.addPlace(null, null, operand, range);
            }
            registrar.doneInjecting();
          }
        }
      }
    }
    else if (AOP_INTRO_PATTERN.accepts(host, context)) {
      final AopAdvisedElementsSearcher searcher = getAopAdvisedElementsSearcher(host);
      if (searcher != null) {
        final PsiField field = context.get(AOP_FIELD_KEY);
        final PsiAnnotation annotation = field.getModifierList().findAnnotation(AopConstants.DECLARE_PARENTS_ANNO);
        host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new JavaIntroLocalAopModel(host, searcher, annotation, field));
        registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
        for (int i = 0; i < operands.length; i++) {
          PsiElement operand = operands[i];
          if (operand instanceof PsiLanguageInjectionHost) {
            TextRange range = getLiteralRange(operand);
            registrar.addPlace(i == 0 ? "target(" : null, i == operands.length-1 ? ")" : null, (PsiLanguageInjectionHost)operand, range);
          }
        }
        registrar.doneInjecting();
      }
    }
  }
  private static List<PsiLanguageInjectionHost> validStringLiteralExpressions(PsiElement... operands) {
    List<PsiLanguageInjectionHost> validOperands = new ArrayList<PsiLanguageInjectionHost>(operands.length);
    for (PsiElement operand : operands) {
      if (operand instanceof PsiLiteralExpressionImpl && ((PsiLiteralExpression)operand).getValue() instanceof String) {
        validOperands.add((PsiLanguageInjectionHost)operand);
      }
    }
    return validOperands;
  }

  private static TextRange getLiteralRange(PsiElement operand) {
    int length = operand.getTextLength();
    return TextRange.from(1, length == 1 ? length - 1 : length - 2);
  }

  @Nullable
  public static AopAdvisedElementsSearcher getAopAdvisedElementsSearcher(final PsiElement element) {
    final List<AopProvider> providers = getAopProviders(element);
    if (providers.isEmpty()) return null;

    final PsiClass psiClass = PsiTreeUtil.getContextOfType(element, PsiClass.class, false);
    if (psiClass == null) return null;

    return providers.get(0).getAdvisedElementsSearcher(PsiUtilBase.getOriginalElement(psiClass, PsiClass.class));
  }

  @NotNull
  public static List<AopProvider> getAopProviders(final PsiElement element) {
    final PsiClass psiClass = PsiTreeUtil.getContextOfType(element, PsiClass.class, false);
    if (psiClass == null) return Collections.emptyList();

    return ContainerUtil.findAll(Extensions.getExtensions(AopProvider.EXTENSION_POINT_NAME), new Condition<AopProvider>() {
      public boolean value(final AopProvider aopProvider) {
        return aopProvider.getAdvisedElementsSearcher(PsiUtilBase.getOriginalElement(psiClass, PsiClass.class)) != null;
      }
    });
  }

  private static class JavaIntroLocalAopModel extends LocalAopModel {
    private final PsiAnnotation myAnnotation;
    private final PsiField myField;

    public JavaIntroLocalAopModel(final PsiElement host,
                                  final AopAdvisedElementsSearcher searcher, final PsiAnnotation annotation, final PsiField field) {
      super(host, null, searcher);
      myAnnotation = annotation;
      myField = field;
    }

    @Nullable
    public IntroductionManipulator getIntroductionManipulator() {
      return new IntroductionManipulator() {
        @NotNull
        public PsiElement getCommonProblemElement() {
          return myAnnotation.getNameReferenceElement();
        }

        public AopIntroduction getIntroduction() {
          return AopModuleService.getIntroduction(myField);
        }

        public void defineDefaultImpl(final Project project, final ProblemDescriptor descriptor) throws IncorrectOperationException {
          final VirtualFile virtualFile = myField.getContainingFile().getVirtualFile();
          AnnotationTextUtil.setAnnotationParameter(myAnnotation, AopConstants.DEFAULT_IMPL_PARAM, "a");
          final PsiAnnotationMemberValue value =
            myField.getModifierList().findAnnotation(AopConstants.DECLARE_PARENTS_ANNO).findDeclaredAttributeValue(AopConstants.DEFAULT_IMPL_PARAM);
          final int offset = value.getTextRange().getStartOffset();
          value.delete();
          new OpenFileDescriptor(project, virtualFile, offset).navigate(true);
        }

        @NonNls
        public String getDefaultImplAttributeName() {
          return AopConstants.DEFAULT_IMPL_PARAM;
        }

        @NotNull
        public PsiElement getInterfaceElement() {
          return myField.getTypeElement();
        }

        @Nullable
        public PsiElement getDefaultImplElement() {
          return myAnnotation.findDeclaredAttributeValue(AopConstants.DEFAULT_IMPL_PARAM);
        }
      };
    }
  }
}
