/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.refactoring.inline;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.inline.InlineOptionsDialog;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrBlockStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrIfStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.branch.GrReturnStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringUtil;

import java.util.*;

/**
 * @author ilyas
 */
public class GroovyInlineMethodUtil {

  private static String myErrorMessage = "ok";
  public static final String REFACTORING_NAME = GroovyRefactoringBundle.message("inline.method.title");

  @Nullable
  public static InlineHandler.Settings inlineMethodSettings(GrMethod method, Editor editor, boolean invokedOnReference) {

    final Project project = method.getProject();
    if (method.isConstructor()) {
      String message = GroovyRefactoringBundle.message("refactoring.cannot.be.applied.to.constructors", REFACTORING_NAME);
      showErrorMessage(message, project, editor);
      return null;
    }

    PsiReference reference = editor != null ? TargetElementUtil.findReference(editor, editor.getCaretModel().getOffset()) : null;
    if (!invokedOnReference || reference == null) {
      String message = GroovyRefactoringBundle.message("multiple.method.inline.is.not.suppored", REFACTORING_NAME);
      showErrorMessage(message, project, editor);
      return null;
    }

    PsiElement element = reference.getElement();

    if (element.getContainingFile() instanceof GroovyFile) {
      if (!(isStaticMethod(method) || areInSameClass(element, method))) { // todo implement for other cases
//        showErrorMessage("Other class support will be implemented soon", myProject);
//        return null;
      }
    }

    if (!(element instanceof GrExpression && element.getParent() instanceof GrCallExpression)) {
      String message = GroovyRefactoringBundle.message("refactoring.is.available.only.for.method.calls", REFACTORING_NAME);
      showErrorMessage(message, project, editor);
      return null;
    }

    GrCallExpression call = (GrCallExpression) element.getParent();

    if (PsiTreeUtil.getParentOfType(element, GrParameter.class) != null) {
      String message = GroovyRefactoringBundle.message("refactoring.is.not.supported.in.parameter.initializers", REFACTORING_NAME);
      showErrorMessage(message, project, editor);
      return null;
    }


    GroovyRefactoringUtil.highlightOccurrences(project, editor, new GrExpression[]{call});
    if (method.getBlock() == null) {
      String message;
      if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
        message = GroovyRefactoringBundle.message("refactoring.cannot.be.applied.to.abstract.methods", REFACTORING_NAME);
      } else {
        message = GroovyRefactoringBundle.message("refactoring.cannot.be.applied.no.sources.attached", REFACTORING_NAME);
      }
      showErrorMessage(message, project, editor);
      return null;
    }

    if (hasBadReturns(method) && !isTailMethodCall(call)) {
      String message = GroovyRefactoringBundle.message("refactoring.is.not.supported.when.return.statement.interrupts.the.execution.flow", REFACTORING_NAME);
      showErrorMessage(message, project, editor);
      return null;
    }

    return inlineMethodDialogResult(method, project, invokedOnReference);
  }

  /**
   * Checks whether given method call is tail call of other method or closure
   *
   * @param call [tail?] Method call
   * @return
   */
  static boolean isTailMethodCall(GrCallExpression call) {
    GrStatement stmt = call;
    PsiElement parent = call.getParent();

    // return statement
    if (parent instanceof GrReturnStatement) {
      stmt = ((GrReturnStatement) parent);
      parent = parent.getParent();
    }
    // method body result
    if (parent instanceof GrOpenBlock) {
      if (parent.getParent() instanceof GrMethod) {
        GrStatement[] statements = ((GrOpenBlock) parent).getStatements();
        return statements.length > 0 && stmt == statements[statements.length - 1];

      }
    }
    // closure result
    if (parent instanceof GrClosableBlock) {
      GrStatement[] statements = ((GrClosableBlock) parent).getStatements();
      return statements.length > 0 && stmt == statements[statements.length - 1];
    }

    // todo add for inner method block statements
    // todo test me!
    if (stmt instanceof GrReturnStatement) {
      GrMethod method = PsiTreeUtil.getParentOfType(stmt, GrMethod.class);
      if (method != null) {
        Collection<GrReturnStatement> returnStatements = GroovyRefactoringUtil.findReturnStatements(method);
        return returnStatements.contains(stmt) && !hasBadReturns(method);
      }
    }

    return false;
  }

  /**
   * Shows dialog with question to inline
   */
  @Nullable
  private static InlineHandler.Settings inlineMethodDialogResult(GrMethod method, Project project, boolean invokedOnReference) {
    Application application = ApplicationManager.getApplication();
    if (!application.isUnitTestMode()) {
      final InlineMethodDialog dialog = new InlineMethodDialog(project, method, invokedOnReference, checkMethodForRecursion(method));

      dialog.show();
      if (!dialog.isOK()) {
        WindowManager.getInstance().getStatusBar(project).setInfo(GroovyRefactoringBundle.message("press.escape.to.remove.the.highlighting"));
        return null;
      } else {
        return new InlineHandler.Settings() {
          public boolean isOnlyOneReferenceToInline() {
            return dialog.isInlineThisOnly();
          }
        };
      }
    }
    return new InlineHandler.Settings() {
      public boolean isOnlyOneReferenceToInline() {
        return true;
      }
    };

  }

  private static boolean hasBadReturns(GrMethod method) {
    Collection<GrReturnStatement> returnStatements = GroovyRefactoringUtil.findReturnStatements(method);
    GrOpenBlock block = method.getBlock();
    if (block == null || returnStatements.size() == 0) return false;
    boolean checked = checkTailOpenBlock(block, returnStatements);
    return !(checked && returnStatements.isEmpty());
  }

  public static boolean checkTailIfStatement(GrIfStatement ifStatement, Collection<GrReturnStatement> returnStatements) {
    GrStatement thenBranch = ifStatement.getThenBranch();
    GrStatement elseBranch = ifStatement.getElseBranch();
    if (elseBranch == null) return false;
    boolean tb = false;
    boolean eb = false;
    if (thenBranch instanceof GrReturnStatement) {
      tb = returnStatements.remove(thenBranch);
    } else if (thenBranch instanceof GrBlockStatement) {
      tb = checkTailOpenBlock(((GrBlockStatement) thenBranch).getBlock(), returnStatements);
    }
    if (elseBranch instanceof GrReturnStatement) {
      eb = returnStatements.remove(elseBranch);
    } else if (elseBranch instanceof GrBlockStatement) {
      eb = checkTailOpenBlock(((GrBlockStatement) elseBranch).getBlock(), returnStatements);
    }

    return tb && eb;
  }

  private static boolean checkTailOpenBlock(GrOpenBlock block, Collection<GrReturnStatement> returnStatements) {
    if (block == null) return false;
    GrStatement[] statements = block.getStatements();
    if (statements.length == 0) return false;
    GrStatement last = statements[statements.length - 1];
    if (last instanceof GrReturnStatement && returnStatements.contains(last)) {
      returnStatements.remove(last);
      return true;
    }
    if (last instanceof GrIfStatement) {
      return checkTailIfStatement(((GrIfStatement) last), returnStatements);
    }
    return false;

  }

  private static void showErrorMessage(String message, final Project project, Editor editor) {
    Application application = ApplicationManager.getApplication();
    myErrorMessage = message;
    if (!application.isUnitTestMode()) {
      CommonRefactoringUtil.showErrorHint(project, editor, message, REFACTORING_NAME, HelpID.INLINE_METHOD);
    }
  }

  @Nullable
  static String getInvokedResult() {
    Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) {
      String message = myErrorMessage;
      myErrorMessage = "ok";
      return message;
    } else {
      return null;
    }
  }

  static boolean isStaticMethod(@NotNull GrMethod method) {
    return method.hasModifierProperty(PsiModifier.STATIC);
  }

  static boolean areInSameClass(PsiElement element, GrMethod method) {
    PsiElement parent = element;
    while (!(parent == null || parent instanceof PsiClass || parent instanceof PsiFile)) {
      parent = parent.getParent();
    }
    if (parent instanceof PsiClass) {
      PsiClass methodClass = method.getContainingClass();
      return parent == methodClass;
    }
    if (parent instanceof GroovyFile) {
      PsiElement mParent = method.getParent();
      return mParent instanceof GroovyFile && mParent == parent;
    }
    return false;
  }

  public static Collection<ReferenceExpressionInfo> collectReferenceInfo(GrMethod method) {
    ArrayList<ReferenceExpressionInfo> list = new ArrayList<ReferenceExpressionInfo>();
    collectReferenceInfoImpl(list, method, method);
    return list;
  }

  private static void collectReferenceInfoImpl(Collection<ReferenceExpressionInfo> infos, PsiElement elem, GrMethod method) {
    if (elem instanceof GrReferenceExpression) {
      GrReferenceExpression expr = (GrReferenceExpression) elem;
      PsiReference ref = expr.getReference();
      if (ref != null) {
        PsiElement declaration = ref.resolve();
        if (declaration instanceof GrMember) {
          int offsetInMethod = expr.getTextRange().getStartOffset() - method.getTextRange().getStartOffset();
          GrMember member = (GrMember) declaration;
          infos.add(new ReferenceExpressionInfo(expr, offsetInMethod, member, member.getContainingClass()));
        }
      }
    }
    for (PsiElement element : elem.getChildren()) {
      collectReferenceInfoImpl(infos, element, method);
    }

  }

  public static boolean hasNoSideEffects(GrExpression qualifier) {
    if (!(qualifier instanceof GrReferenceExpression)) return false;
    GrExpression qual = ((GrReferenceExpression) qualifier).getQualifierExpression();
    return qual == null || hasNoSideEffects(qual);
  }

  static class ReferenceExpressionInfo {
    public final PsiMember declaration;
    public final GrReferenceExpression expression;
    public final int offsetInMethod;
    public final PsiClass containingClass;

    @Nullable
    public String getPresentation() {
      return declaration.getName();
    }

    public boolean isStatic() {
      return declaration.hasModifierProperty(PsiModifier.STATIC);
    }

    public ReferenceExpressionInfo(GrReferenceExpression expression, int offsetInMethod, PsiMember declaration, PsiClass containingClass) {
      this.expression = expression;
      this.offsetInMethod = offsetInMethod;
      this.declaration = declaration;
      this.containingClass = containingClass;
    }
  }


  static void addQualifiersToInnerReferences(GrMethod method, Collection<ReferenceExpressionInfo> infos, @NotNull GrExpression qualifier)
      throws IncorrectOperationException {
    Set<GrReferenceExpression> exprs = new HashSet<GrReferenceExpression>();
    for (ReferenceExpressionInfo info : infos) {
      PsiReference ref = method.findReferenceAt(info.offsetInMethod);
      if (ref != null && ref.getElement() instanceof GrReferenceExpression) {
        GrReferenceExpression refExpr = (GrReferenceExpression) ref.getElement();
        if (refExpr.getQualifierExpression() == null) {
          exprs.add(refExpr);
        }
      }
    }

    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(qualifier.getProject());
    for (GrReferenceExpression expr : exprs) {
      GrExpression qual = factory.createExpressionFromText(qualifier.getText());
      if (qual instanceof GrReferenceExpression) {
        expr.setQualifierExpression(((GrReferenceExpression) qual));
      }
    }
  }

  private static boolean checkMethodForRecursion(GrMethod method) {
    return checkCalls(method.getBlock(), method);
  }

  private static boolean checkCalls(PsiElement scope, PsiMethod method) {
    if (scope instanceof GrMethodCallExpression) {
      PsiMethod refMethod = ((GrMethodCallExpression)scope).resolveMethod();
      if (method.equals(refMethod)) return true;
    }
    else if (scope instanceof GrApplicationStatement) {
      final GrExpression expression = ((GrApplicationStatement)scope).getFunExpression();
      if (expression instanceof GrReferenceExpression) {
        final PsiElement resolved = ((GrReferenceExpression)expression).resolve();
        if (method.equals(resolved)) return true;
      }
    }

    for (PsiElement child = scope.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (checkCalls(child, method)) return true;
    }

    return false;
  }


  static class InlineMethodDialog extends InlineOptionsDialog {
    public static final String REFACTORING_NAME = GroovyRefactoringBundle.message("inline.method.title");
    private final boolean myAllowInlineThisOnly;

    private final PsiMethod myMethod;

    public InlineMethodDialog(Project project,
                              PsiMethod method,
                              boolean invokedOnReference,
                              final boolean allowInlineThisOnly) {
      super(project, true, method);
      myMethod = method;
      myAllowInlineThisOnly = allowInlineThisOnly;
      myInvokedOnReference = invokedOnReference;

      setTitle(REFACTORING_NAME);

      init();
    }

    @Override
    protected String getBorderTitle() {
      return GroovyRefactoringBundle.message("inline.method.border.title");
    }

    @Override
    protected String getNameLabelText() {
      return GroovyRefactoringBundle.message("inline.method.label", GroovyRefactoringUtil.getMethodSignature(myMethod));
    }

    @Override
    protected String getInlineAllText() {
      return myMethod.isWritable()
             ? GroovyRefactoringBundle.message("all.invocations.and.remove.the.method")
             : GroovyRefactoringBundle.message("all.invocations.in.project");
    }

    @Override
    protected String getInlineThisText() {
      return GroovyRefactoringBundle.message("this.invocation.only.and.keep.the.method");
    }

    @Override
    protected boolean isInlineThis() {
      return false;
    }

    @Override
    protected void doAction() {
      if (getOKAction().isEnabled()) {
        close(OK_EXIT_CODE);
      }
    }

    protected void doHelpAction() {
      HelpManager.getInstance().invokeHelp(HelpID.INLINE_METHOD);
    }

    protected boolean canInlineThisOnly() {
      return myAllowInlineThisOnly;
    }
  }

  /**
   * Inline method call's arguments as its parameters
   *
   * @param call   method call
   * @param method given method
   */
  public static void replaceParametersWithArguments(GrCallExpression call, GrMethod method) throws IncorrectOperationException {
    GrArgumentList argumentList = call.getArgumentList();
    if (argumentList == null) {
      setDefaultValuesToParameters(method, null, call);
      return;
    }
    ArrayList<GrExpression> exprs = new ArrayList<GrExpression>();
    exprs.addAll(Arrays.asList(argumentList.getExpressionArguments()));
    exprs.addAll(Arrays.asList(call.getClosureArguments()));

    // first parameter may have map type
    boolean firstParamIsMap = argumentList.getNamedArguments().length > 0;
    GrParameter[] parameters = method.getParameters();
    if (parameters.length == 0) return;
    GrParameter firstParam = parameters[0];
    while (exprs.size() > parameters.length - (firstParamIsMap ? 1 : 0)) {
      exprs.remove(exprs.size() - 1);
    }

    int nonDefault = 0;
    for (GrParameter parameter : parameters) {
      if (!(firstParam == parameter && firstParamIsMap)) {
        if (parameter.getDefaultInitializer() == null) {
          nonDefault++;
        }
      }
    }
    nonDefault = exprs.size() - nonDefault;
    // Parameters that will be replaced by its default values
    Set<String> nameFilter = new HashSet<String>();
    for (GrParameter parameter : parameters) {
      if (!(firstParam == parameter && firstParamIsMap)) {
        GrExpression initializer = parameter.getDefaultInitializer();
        if (initializer != null) {
          if (nonDefault > 0) {
            nonDefault--;
          } else {
            nameFilter.add(parameter.getName());
          }
        }
      }
    }
    // todo add named arguments
    setDefaultValuesToParameters(method, nameFilter, call);
    setValuesToParameters(method, call, exprs, nameFilter, firstParamIsMap);
  }

   /**
   * replaces parameter occurrences in method with its default values (if it's possible)
   *
   * @param method     given method
    * @param nameFilter specified parameter names (which ma have default initializers)
    * @param call
    */
  private static void setDefaultValuesToParameters(GrMethod method, Collection<String> nameFilter, GrCallExpression call) throws IncorrectOperationException {
    if (nameFilter == null) {
      nameFilter = new ArrayList<String>();
      for (GrParameter parameter : method.getParameters()) {
        nameFilter.add(parameter.getName());
      }
    }
    GrParameter[] parameters = method.getParameters();
    for (GrParameter parameter : parameters) {
      GrExpression initializer = parameter.getDefaultInitializer();
      if (nameFilter.contains(parameter.getName()) && initializer != null) {
        replaceAllOccurrencesWithExpression(method, call, initializer, parameter);
      }
    }
  }

  /**
   * Replace first m parameters by given values, where m is length of given values vector
   *
   * @param method
   * @param call
   * @param values          values vector
   * @param nameFilter
   * @param firstParamIsMap
   */
  private static void setValuesToParameters(GrMethod method, GrCallExpression call, List<GrExpression> values, Set<String> nameFilter,
                                            boolean firstParamIsMap)
      throws IncorrectOperationException {
    if (nameFilter == null) {
      nameFilter = new HashSet<String>();
    }
    GrParameter[] parameters = method.getParameters();
    if (parameters.length == 0) return;
    int i = firstParamIsMap ? 1 : 0;
    for (GrExpression value : values) {
      while (i < parameters.length && nameFilter.contains(parameters[i].getName())) i++;
      if (i < parameters.length) {
        GrParameter parameter = parameters[i];
        replaceAllOccurrencesWithExpression(method, call, value, parameter);
      }
      i++;
    }
  }

  private static void replaceAllOccurrencesWithExpression(GrMethod method,
                                                          GrCallExpression call,
                                                          GrExpression oldExpression,
                                                          GrParameter parameter) {
    Collection<PsiReference> refs =
      ReferencesSearch.search(parameter, GlobalSearchScope.projectScope(parameter.getProject()), false).findAll();

    final GroovyPsiElementFactory elementFactory = GroovyPsiElementFactory.getInstance(call.getProject());
    GrExpression expression = elementFactory.createExpressionFromText(oldExpression.getText());


    if (!canReplaceAllReferencesWithExpression(refs, oldExpression)) {
      final String oldName = parameter.getName();
      final String newName = InlineMethodConflictSolver.suggestNewName(oldName, method, call);

      expression = elementFactory.createExpressionFromText(newName);
      final GrOpenBlock body = method.getBlock();
      final GrStatement[] statements = body.getStatements();
      GrStatement anchor = null;
      if (statements.length > 0) {
        anchor = statements[0];
      }
      body.addStatementBefore(elementFactory.createStatementFromText(createVariableDefinitionText(parameter, oldExpression, newName)),
                              anchor);
    }

    for (PsiReference ref : refs) {
      PsiElement element = ref.getElement();
      if (element instanceof GrReferenceExpression) {
        ((GrReferenceExpression)element).replaceWithExpression(expression, true);
      }
    }
  }

  private static String createVariableDefinitionText(GrParameter parameter, GrExpression expression, String varName) {
    final PsiModifierList modifierList = parameter.getModifierList();
    String modifiers;
    if (modifierList != null) {
      modifiers = modifierList.getText().trim();
    }
    else {
      modifiers = "";
    }

    String type;
    final GrTypeElement typeElement = parameter.getTypeElementGroovy();
    if (typeElement != null) {
      type = typeElement.getText();
    }
    else {
      type = "";
    }
    if (modifiers.length() == 0 && type.length() == 0) {
      modifiers = "def";
    }

    return modifiers + " " + type + " " + varName + " =  " + expression.getText();
  }

  /**
   * @param refs collection of references to method parameters. It is considered that these references have no qualifiers
   */
  private static boolean containsWriteAccess(Collection<PsiReference> refs) {
    for (PsiReference ref : refs) {
      final PsiElement element = ref.getElement();
      final PsiElement parent = element.getParent();
      if (parent instanceof GrAssignmentExpression && ((GrAssignmentExpression)parent).getLValue() == element) return true;
      if (parent instanceof GrUnaryExpression) return true;
    }
    return false;
  }

  private static boolean canReplaceAllReferencesWithExpression(Collection<PsiReference> refs, GrExpression expression) {
    if (containsWriteAccess(refs)) {
      if (expression instanceof GrReferenceExpression) {
        final PsiElement resolved = ((GrReferenceExpression)expression).resolve();

        if (resolved instanceof GrVariable && !(resolved instanceof PsiField)) {
          final boolean isFinal = ((GrVariable)resolved).hasModifierProperty(PsiModifier.FINAL);
          if (!isFinal) {
            final PsiReference lastRef =
              Collections.max(ReferencesSearch.search(resolved, resolved.getResolveScope()).findAll(), new Comparator<PsiReference>() {
                public int compare(PsiReference o1, PsiReference o2) {
                  return o1.getElement().getTextRange().getStartOffset() - o2.getElement().getTextRange().getStartOffset();
                }
              });
            return lastRef.getElement() == expression;
          }
        }
      }
      return false;
    }
    return true;
  }
}
