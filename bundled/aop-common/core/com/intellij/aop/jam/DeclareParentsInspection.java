/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.jam;

import com.intellij.aop.AopBundle;
import com.intellij.aop.AopIntroduction;
import com.intellij.aop.IntroductionManipulator;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class DeclareParentsInspection extends XmlSuppressableInspectionTool {
  private static final Logger LOG = Logger.getInstance("#com.intellij.aop.jam.DeclareParentsInspection");
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return AopBundle.message("inspection.display.name.declareParents");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "DeclareParentsInspection";
  }

  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override public void visitElement(final PsiElement element) {
        if (element instanceof PsiLiteralExpression || element instanceof XmlAttributeValue) {
          final PsiFile file = InjectedLanguageUtil.findInjectedPsiNoCommit(element.getContainingFile(), element.getTextRange().getStartOffset() + 1);
          if (file instanceof AopPointcutExpressionFile) {
            final IntroductionManipulator manipulator = ((AopPointcutExpressionFile)file).getAopModel().getIntroductionManipulator();
            if (manipulator == null) return;
            final AopIntroduction introduction = manipulator.getIntroduction();
            if (introduction == null) return;

            final PsiClass intf = introduction.getImplementInterface().getValue();
            if (intf == null && introduction.getImplementInterface().getStringValue() != null || intf != null && !intf.isInterface()) {
              registerProblem(manipulator.getInterfaceElement(), AopBundle.message("error.interface.expected"), holder);
              return;
            }
            if (intf == null) return;

            final PsiClass defaultImpl = introduction.getDefaultImpl().getValue();
            if (defaultImpl == null) {
              if (!(element instanceof XmlElement) && !ContainerUtil.findAll(intf.getAllMethods(), new Condition<PsiMethod>() {
                public boolean value(final PsiMethod method) {
                  return method.hasModifierProperty(PsiModifier.ABSTRACT);
                }
              }).isEmpty()) {
                holder.registerProblem(manipulator.getCommonProblemElement(), AopBundle.message("error.default.implementation.class.should.be.specified"),
                                       new LocalQuickFix() {
                  @NotNull
                  public String getName() {
                    return AopBundle.message("quickfix.name.define.attribute", manipulator.getDefaultImplAttributeName());
                  }

                  @NotNull
                  public String getFamilyName() {
                    return getName();
                  }

                  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
                    try {
                      if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(descriptor.getPsiElement().getContainingFile().getVirtualFile()).hasReadonlyFiles()) return;
                      manipulator.defineDefaultImpl(project, descriptor);
                    }
                    catch (IncorrectOperationException e) {
                      LOG.error(e);
                    }
                  }
                });

              }
              return;
            }
            if (defaultImpl.hasModifierProperty(PsiModifier.ABSTRACT) || !defaultImpl.isInheritor(intf, true)) {
              final PsiElement defaultImplElement = manipulator.getDefaultImplElement();
              assert defaultImplElement != null;
              registerProblem(defaultImplElement, AopBundle.message("error.non.abstract.class.implemention.0.expected", intf.getQualifiedName()), holder);
            }
          }
        }
      }
    };
  }

  private static void registerProblem(final PsiElement element, final String descriptionTemplate, final ProblemsHolder holder) {
    final int startOffset = element.getTextRange().getStartOffset();
    int quotes = element.getText().startsWith("\"") ? 1 : 0;
    final TextRange range = TextRange.from(quotes, Math.max(element.getTextLength() - 2 * quotes, 1));
    holder.registerProblem(holder.getManager().createProblemDescriptor(element, range,
                                                                       descriptionTemplate, ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
  }

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return AopBundle.message("inspection.group.display.name.aop");
  }
}