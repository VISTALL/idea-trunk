package org.jetbrains.plugins.groovy.intentions.base;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.intentions.GroovyIntentionsBundle;
import org.jetbrains.plugins.groovy.intentions.utils.BoolUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.annotator.intentions.QuickfixUtil;


public abstract class Intention implements IntentionAction {
  private final PsiElementPredicate predicate;

  /**
   * @noinspection AbstractMethodCallInConstructor,OverridableMethodCallInConstructor
   */
  protected Intention() {
    super();
    predicate = getElementPredicate();
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file)
      throws IncorrectOperationException {
    if (!QuickfixUtil.ensureFileWritable(project, file)) {
      return;
    }
    final PsiElement element = findMatchingElement(file, editor);
    if (element == null) {
      return;
    }
    assert element.isValid() : element;
    processIntention(element);
  }

  protected abstract void processIntention(@NotNull PsiElement element)
      throws IncorrectOperationException;

  @NotNull
  protected abstract PsiElementPredicate getElementPredicate();


  protected static void replaceExpressionWithNegatedExpressionString(
      @NotNull String newExpression,
      @NotNull GrExpression expression)
      throws IncorrectOperationException {
    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(expression.getProject());

    GrExpression expressionToReplace = expression;
    final String expString;
    if (BoolUtils.isNegated(expression)) {
      expressionToReplace = BoolUtils.findNegation(expression);
      expString = newExpression;
    } else {
      expString = "!(" + newExpression + ')';
    }
    final GrExpression newCall =
        factory.createExpressionFromText(expString);
    assert expressionToReplace != null;
    expressionToReplace.replaceWithExpression(newCall, true);
  }


  @Nullable
  PsiElement findMatchingElement(PsiFile file,
                                 Editor editor) {
    final CaretModel caretModel = editor.getCaretModel();
    final int position = caretModel.getOffset();
    PsiElement element = file.findElementAt(position);
    while (element != null) {
      if (predicate.satisfiedBy(element)) {
        return element;
      } else {
        element = element.getParent();
        if (isStopElement(element)) {
          break;
        }
      }
    }
    return null;
  }

  protected boolean isStopElement(PsiElement element) {
    return element instanceof PsiFile;
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return findMatchingElement(file, editor) != null;
  }

  public boolean startInWriteAction() {
    return true;
  }

  private String getPrefix() {
    final Class<? extends Intention> aClass = getClass();
    final String name = aClass.getSimpleName();
    final StringBuilder buffer = new StringBuilder(name.length() + 10);
    buffer.append(Character.toLowerCase(name.charAt(0)));
    for (int i = 1; i < name.length(); i++) {
      final char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        buffer.append('.');
        buffer.append(Character.toLowerCase(c));
      } else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  @NotNull
  public String getText() {
    return GroovyIntentionsBundle.message(getPrefix() + ".name");
  }

  @NotNull
  public String getFamilyName() {
    return GroovyIntentionsBundle.message(getPrefix() + ".family.name");
  }
}
