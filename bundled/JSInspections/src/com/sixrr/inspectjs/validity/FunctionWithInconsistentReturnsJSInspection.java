package com.sixrr.inspectjs.validity;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.inspectjs.*;
import com.sixrr.inspectjs.utils.ControlFlowUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FunctionWithInconsistentReturnsJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("function.with.inconsistent.returns.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.VALIDITY_GROUP_NAME;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @Nullable
    protected String buildErrorString(Object... args) {
        final JSFunction function = PsiTreeUtil.getParentOfType((PsiElement)args[0], JSFunction.class);
        assert function != null;

        if(missingReturnStatementProblem(function)) {
            return InspectionJSBundle.message("missing.return.statement.error.string");
        }
        if (functionHasIdentifier(function)) {
            return InspectionJSBundle.message("function.has.inconsistent.return.points.error.string");
        } else {
            return InspectionJSBundle.message("anonymous.function.has.inconsistent.return.points.error.string");
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static boolean missingReturnStatementProblem(JSFunction function) {
        String typeString = function.getReturnTypeString();
        return typeString != null && function.getContainingFile().getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4;
    }

    @Override
    protected InspectionJSFix[] buildFixes(PsiElement location) {
      JSFunction fun = PsiTreeUtil.getParentOfType(location, JSFunction.class);

      if (fun != null && missingReturnStatementProblem(fun)) {
          return new InspectionJSFix[] {
            new InspectionJSFix() {
              @Override
              protected void doFix(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
                JSFunction fun = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), JSFunction.class);
                if (fun != null) {
                  PsiElement typeElement = fun.getReturnTypeElement();
                  if (typeElement != null) {
                    typeElement.replace(JSChangeUtil.createExpressionFromText(project, "void").getPsi());
                  }
                }
              }

              @NotNull
              public String getName() {
                return InspectionJSBundle.message("set.return.type.to.void.fix.name");
              }
            }, new InspectionJSFix() {
              @Override
              protected void doFix(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
                PsiElement element = descriptor.getPsiElement();
                JSFunction fun = PsiTreeUtil.getParentOfType(element, JSFunction.class);

                if (fun != null) {
                  String typeString = fun.getReturnTypeString();
                  TemplateManager templateManager = TemplateManager.getInstance(project);
                  Template t = templateManager.createTemplate("","");
                  t.setToReformat(true);

                  t.addTextSegment("return ");
                  t.addEndVariable();
                  t.addSelectionStartVariable();

                  String value = "null";
                  if ("Boolean".equals(typeString)) {
                    value = "false";
                  } else if ("Number".equals(typeString) || "int".equals(typeString) || "uint".equals(typeString)) {
                    value = "0";
                  }
                  t.addTextSegment(value);
                  t.addSelectionEndVariable();

                  String s = JSChangeUtil.getSemicolon(project);
                  if (s.length() > 0) t.addTextSegment(s);
                  t.addTextSegment("\n");

                  int offset = element.getTextRange().getStartOffset() + PsiUtilBase.findInjectedElementOffsetInRealDocument(element);
                  final Editor editor = FileEditorManager.getInstance(project).openTextEditor(
                    new OpenFileDescriptor(
                      project,
                      fun.getContainingFile().getVirtualFile(), offset
                    ),
                    false
                  );
                  if (editor != null) {
                    if (offset != -1) editor.getCaretModel().moveToOffset(offset);
                    templateManager.startTemplate(editor, t);
                  }
                }
              }

              @NotNull
              public String getName() {
                return InspectionJSBundle.message("add.return.statement.fix.name");
              }
            }
          };
        }
        return super.buildFixes(location);
    }

  private static class Visitor extends BaseInspectionVisitor {
        protected ProblemHighlightType getProblemHighlightType(PsiElement location) {
            return location.getContainingFile().getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4 ?
                   ProblemHighlightType.GENERIC_ERROR:super.getProblemHighlightType(location);
        }

        @Override
        protected void registerFunctionError(JSFunction function) {
          if (missingReturnStatementProblem(function)) {
                JSSourceElement[] body = function.getBody();

                if (body.length > 0 && body[0] instanceof JSBlockStatement) {
                    registerError(findValidEditorLocation(body[0].getLastChild()));
                    return;
                }
            }
            super.registerFunctionError(function);
        }

        @Override
        public void visitJSFunctionDeclaration(JSFunction function) {
            super.visitJSFunctionDeclaration(function);
            String typeString = function.getReturnTypeString();
            if (typeString == null && !functionHasReturnValues(function)) {
                return;
            }
            if ("void".equals(typeString) || !functionHasValuelessReturns(function)) {
                return;
            }

            registerFunctionError(function);
        }

        @Override
        public void visitJSFunctionExpression(JSFunctionExpression node) {
            super.visitJSFunctionExpression(node);
            JSFunction function = node.getFunction();
            String typeString = function.getReturnTypeString();

            if (typeString != null && !"void".equals(typeString)) {
                if (functionHasValuelessReturns(function)) {
                    registerFunctionError(function);
                }
            }
        }
    }

    private static boolean functionHasReturnValues(JSFunction function) {
        final ReturnValuesVisitor visitor = new ReturnValuesVisitor(function);
        function.accept(visitor);
        return visitor.hasReturnValues();
    }

    private static boolean functionHasValuelessReturns(JSFunction function) {
        final PsiElement lastChild = function.getLastChild();
        if (lastChild instanceof JSBlockStatement) {
            if (ControlFlowUtils.statementMayCompleteNormally((JSStatement) lastChild)) {
                return true;
            }
        }
        final ValuelessReturnVisitor visitor = new ValuelessReturnVisitor(function);
        function.acceptChildren(visitor);
        return visitor.hasValuelessReturns();
    }

    private static class ReturnValuesVisitor extends JSRecursiveElementVisitor {
        private final JSFunction function;
        private boolean hasReturnValues = false;

        ReturnValuesVisitor(JSFunction function) {
            this.function = function;
        }

        @Override public void visitJSReturnStatement(JSReturnStatement statement) {
            super.visitJSReturnStatement(statement);
            if (statement.getExpression() != null) {
                final JSFunction containingFunction =
                        PsiTreeUtil.getParentOfType(statement, JSFunction.class);
                if (function.equals(containingFunction)) {
                    hasReturnValues = true;
                }
            }
        }

        public boolean hasReturnValues() {
            return hasReturnValues;
        }
    }

    private static class ValuelessReturnVisitor extends JSRecursiveElementVisitor {
        private final JSFunction function;
        private boolean hasValuelessReturns = false;

        ValuelessReturnVisitor(JSFunction function) {
            this.function = function;
        }

        @Override public void visitJSReturnStatement(JSReturnStatement statement) {
            super.visitJSReturnStatement(statement);
            if (statement.getExpression() == null) {
                final JSFunction containingFunction =
                        PsiTreeUtil.getParentOfType(statement, JSFunction.class);
                if (function.equals(containingFunction)) {
                    hasValuelessReturns = true;
                }
            }
        }

        @Override public void visitJSFunctionDeclaration(JSFunction function) {
            // do nothing, so that it doesn't drill into nested functions
        }

        public boolean hasValuelessReturns() {
            return hasValuelessReturns;
        }
    }
}
