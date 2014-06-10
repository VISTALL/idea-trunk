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

package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import static com.advancedtools.webservices.rest.RestAnnotations.*;
import com.advancedtools.webservices.rest.RestUriTemplateParser;
import static com.advancedtools.webservices.utils.RestUtils.*;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class PathAnnotationInspection extends BaseWebServicesInspection {
  protected void checkMember(final ProblemsHolder problemsHolder, final PsiMember psiMember) {
    if (psiMember instanceof PsiAnnotation) {
      
    }
  }

  protected void doCheckClass(final PsiClass c, final ProblemsHolder problemsHolder) {
    if (isAnnotatedAs(PATH, c)) {
      final PsiAnnotation pathAnno = findAnnotation(PATH, c);
      if (pathAnno == null) return;
      final String path = getAnnotationValue(pathAnno);
      if (path == null) return;
      final PsiNameValuePair[] attributes = pathAnno.getParameterList().getAttributes();
      if (attributes.length != 1 || attributes[0] == null) return;
      final PsiNameValuePair valueAttr = attributes[0];
      List<String> names = new ArrayList<String>();
      try {
        final RestUriTemplateParser parser = new RestUriTemplateParser(path);
        names.addAll(parser.getNames());
      } catch (Exception e) {
        problemsHolder.registerProblem(valueAttr,
                                       WSBundle.message("webservices.inspections.wrong.path.annotation.value.problem"),
                                       ProblemHighlightType.GENERIC_ERROR);
        return;
      }
      if (!names.isEmpty()) {
        checkPathNames(c, names, problemsHolder, pathAnno);
      }

      Project project = c.getProject();
      Collection<PsiAnnotation> annos = JavaAnnotationIndex.getInstance().get(PATH_SHORT, project, GlobalSearchScope.projectScope(project));
      if (annos.size() > 1) { // we are in
        for (PsiAnnotation anno : annos) {
          if (anno != pathAnno && isNonAbstractAndPublicClass(getParent(anno)) && path.equals(getAnnotationValue(anno)) ) {
            PsiClass parent = getParent(anno);
            problemsHolder.registerProblem(pathAnno,
                                           WSBundle.message("webservices.inspections.ambiguous.path.annotation.problem", parent == null ? "" : parent.getQualifiedName()),
                                           ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
          }
        }
      }
    }
  }

  private static void checkPathNames(PsiElement c, final List<String> names, ProblemsHolder problemsHolder, PsiAnnotation pathAnno) {
    c.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof PsiAnnotation
            && PATH_PARAM.equals(((PsiAnnotation)element).getQualifiedName())) {
          final String param = getAnnotationValue((PsiAnnotation)element);
          if (names.contains(param)) {
            names.remove(param);
          }
        } else {
          super.visitElement(element);
        }
      }
    });
    for (String name : names) {
      selectNotUsedPathTemplate(name, pathAnno, problemsHolder);
    }
  }

  private static void selectNotUsedPathTemplate(String name, PsiAnnotation pathAnno, ProblemsHolder problemsHolder) {
    final PsiNameValuePair[] attributes = pathAnno.getParameterList().getAttributes();
    if (attributes.length == 1) {
      final PsiAnnotationMemberValue value = attributes[0].getValue();
      final String problemText = "{" + name + "}";
      if (value instanceof PsiLiteralExpression && value.getText().contains(problemText)) {
        final int start = value.getText().indexOf(problemText);
        final int end = start + problemText.length();
        final PsiClass context = PsiTreeUtil.getParentOfType(pathAnno, PsiClass.class);
        if (context != null) {
          PsiField field = null;
          for (PsiField psiField : context.getFields()) {
            if (name.equals(psiField.getName())) {
              field = psiField;
              break;
            }
          }
        problemsHolder.registerProblem(value,
                                       WSBundle.message("webservices.inspections.unused.path.template", name),
                                       ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                       new TextRange(start, end),
                                       field == null ? new CreateFieldWithPathParam(name, context) : new AnnotateElementWithPathParam(field, name));
        }
      }
      else if (value instanceof PsiReferenceExpression) {
        final PsiClass context = PsiTreeUtil.getParentOfType(pathAnno, PsiClass.class);
        if (context != null) {
          PsiField field = null;
          for (PsiField psiField : context.getFields()) {
            if (name.equals(psiField.getName())) {
              field = psiField;
              break;
            }
          }
        problemsHolder.registerProblem(value,
                                       WSBundle.message("webservices.inspections.unused.path.template", name),
                                       ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                       field == null ? new CreateFieldWithPathParam(name, context) : new AnnotateElementWithPathParam(field, name));
        }
      }
    }


  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.ambiguous.path.annotation.display.name");
  }

  @NotNull
  public String getShortName() {
    return "PathAnnotation";
  }

  static class CreateFieldWithPathParam implements LocalQuickFix {
    private static final String FIELD_TEMPLATE = "private String {0};\n";
    private String name;
    private PsiClass context;

    public CreateFieldWithPathParam(String name, PsiClass context) {
      this.name = name;
      this.context = context;
    }

    @NotNull
    public String getName() {
      return WSBundle.message("create.field.annotated.path.param", name);
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          final PsiClass psiClass = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiClass.class);
          if (psiClass == null ||
             !EnvironmentFacade.getInstance().prepareFileForWrite(psiClass.getContainingFile())) return;

          final String text = MessageFormat.format(FIELD_TEMPLATE, name);
          final PsiField field = JavaPsiFacade.getElementFactory(project).createFieldFromText(text, null);
          final PsiField[] fields = context.getFields();
          final PsiElement element = context.addAfter(field, fields.length == 0 ? context.getLBrace() : fields[fields.length - 1]);

          if (element instanceof PsiField) {
            annotateFieldWithPathParam(project, (PsiField)element, name);
          }
        }
      });
    }
  }

  private static class AnnotateElementWithPathParam implements LocalQuickFix {
    private PsiField myField;
    private String myName;

    public AnnotateElementWithPathParam(PsiField field, String name) {
      myField = field;
      myName = name;
    }

    @NotNull
    public String getName() {
      return WSBundle.message("annotate.field.with.path.param", myName, myName);
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          annotateFieldWithPathParam(project, myField, myName);
        }
      });
    }
  }

  private static void annotateFieldWithPathParam(Project project, PsiField field, String name) {
    final PsiModifierList modifierList = field.getModifierList();
    if (modifierList == null) return;

    if (!EnvironmentFacade.getInstance().prepareFileForWrite(field.getContainingFile())) return;

    final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
    final PsiAnnotation annotation = factory.createAnnotationFromText("@" + PATH_PARAM + "(\"" + name + "\")", field);
    final PsiElement firstChild = modifierList.getFirstChild();
    if (firstChild != null) {
      modifierList.addBefore(annotation, firstChild);
    }
    else {
      modifierList.add(annotation);
    }

    JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
  }
}
