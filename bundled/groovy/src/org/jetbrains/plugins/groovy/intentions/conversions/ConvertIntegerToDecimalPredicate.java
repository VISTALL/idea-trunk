package org.jetbrains.plugins.groovy.intentions.conversions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;


class ConvertIntegerToDecimalPredicate implements PsiElementPredicate {

  public boolean satisfiedBy(PsiElement element) {
    if (!(element instanceof GrLiteral)) {
      return false;
    }
    final GrLiteral expression = (GrLiteral) element;
    final PsiType type = expression.getType();
    if (type == null) {
      return false;
    }
    if (!PsiType.INT.equals(type) && !PsiType.LONG.equals(type) &&
        !type.equalsToText("java.lang.Integer") && !type.equalsToText("java.lang.Long")) {
      return false;
    }
    @NonNls final String text = expression.getText();
    if (text == null || text.length() < 2) {
      return false;
    }
    if ("0".equals(text) || "0L".equals(text) || "0l".equals(text)) {
      return false;
    }
    return text.charAt(0) == '0';
  }
}
