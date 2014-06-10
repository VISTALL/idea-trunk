/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.highlighting.fixes;

import static com.intellij.beanValidation.constants.BvAnnoConstants.*;
import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateClassKind;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class CreateConstraintValidatorFix extends BaseBVQuickFix{
  private final PsiJavaCodeReferenceElement myRef;
  private final PsiClass myConstraint;


  public CreateConstraintValidatorFix(final PsiJavaCodeReferenceElement ref, PsiClass constraint) {
    super(BVInspectionBundle.message("create.constraint.validator", ref.getCanonicalText()));
    myRef = ref;
    myConstraint = constraint;
  }

  public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiJavaCodeReferenceElement element = myRef;
    assert element != null;
    if (!CodeInsightUtilBase.preparePsiElementForWrite(element)) return;
    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          PsiJavaCodeReferenceElement refElement = element;
          final PsiClass aClass = CreateFromUsageUtils.createClass(refElement, CreateClassKind.CLASS, null);
          if (aClass == null) return;
          try {
            refElement = (PsiJavaCodeReferenceElement)refElement.bindToElement(aClass);
          }
          catch (IncorrectOperationException e) {//
          }

          StringBuilder template = new StringBuilder();
          final int nameOffset = aClass.getNameIdentifier().getTextOffset() + aClass.getNameIdentifier().getTextLength();
          final int leftBraceOffset = aClass.getLBrace().getTextOffset();
          template.append(" implements ").append(CONSTRAINT_VALIDATOR).append("<")
            .append(myConstraint.getQualifiedName())
            .append(", $type$> {");
          template.append("\n" + "   public void initialize(").append(myConstraint.getQualifiedName())
            .append(" constraint) {\n   }\n\n")
            .append("   public boolean isValid($type$ $name$, ")
            .append(CONSTRAINT_VALIDATOR_CONTEXT)
            .append(" context) {\n      return false;\n   }");

          IdeDocumentHistory.getInstance(project).includeCurrentPlaceAsChangePlace();

          OpenFileDescriptor descriptor = new OpenFileDescriptor(refElement.getProject(), aClass.getContainingFile().getVirtualFile(),
                                                                 aClass.getTextOffset());
          String fqn = aClass.getQualifiedName();
          final Editor editor = FileEditorManager.getInstance(aClass.getProject()).openTextEditor(descriptor, true);
          editor.getSelectionModel().setSelection(nameOffset, leftBraceOffset + 1);
          final Template temp = TemplateManager.getInstance(project).createTemplate("", "", template.toString());
          temp.addVariable("type", "\"String\"", "String", true);
          temp.addVariable("name", "\"obj\"", "obj", true);
          TemplateManager.getInstance(project).startTemplate(editor, "", temp);
          final PsiExpression expression =
            JavaPsiFacade.getInstance(project).getElementFactory().createExpressionFromText(fqn + ".class", myConstraint.getModifierList());
          myConstraint.getModifierList().findAnnotation(CONSTRAINT).findAttributeValue(VALIDATED_BY).replace(expression);
        }
      }
    );
  }
}
