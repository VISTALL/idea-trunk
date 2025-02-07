package org.jetbrains.plugins.groovy.intentions.control;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.branch.GrReturnStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

class ExpandBooleanPredicate implements PsiElementPredicate {
  private static final Logger LOGGER = Logger.getInstance("ExpandBooleanPredicate");

  public boolean satisfiedBy(PsiElement element) {
    if (!(element instanceof GrStatement)) {
      return false;
    }
    final GrStatement statement = (GrStatement) element;
    return isBooleanReturn(statement) || isBooleanAssignment(statement);
  }

  public static boolean isBooleanReturn(GrStatement statement) {
    if (!(statement instanceof GrReturnStatement)) {
      return false;
    }
    final GrReturnStatement returnStatement =
        (GrReturnStatement) statement;
    final GrExpression returnValue = returnStatement.getReturnValue();
    if (returnValue == null) {
      return false;
    }
    if (returnValue instanceof GrLiteral) {
      return false;
    }
    final PsiType returnType = returnValue.getType();
    if (returnType == null) {
      return false;
    }
    return returnType.equals(PsiType.BOOLEAN) || returnType.equalsToText("java.lang.Boolean");
  }

  public static boolean isBooleanAssignment(GrStatement expression) {

    if (!(expression instanceof GrAssignmentExpression)) {
      return false;
    }
    final GrAssignmentExpression assignment =
        (GrAssignmentExpression) expression;
    final GrExpression rhs = assignment.getRValue();
    if (rhs == null) {
      return false;
    }
    if (rhs instanceof GrLiteral) {
      return false;
    }
    final PsiType assignmentType = rhs.getType();
    if (assignmentType == null) {
      return false;
    }
    return assignmentType.equals(PsiType.BOOLEAN) || assignmentType.equalsToText("java.lang.Boolean");
  }
}
