package com.sixrr.inspectjs.confusing;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.inspectjs.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfusingFloatingPointLiteralJSInspection extends JavaScriptInspection {
    @NonNls
    private static final Pattern pickyFloatingPointPattern =
            Pattern.compile("[0-9]+\\.[0-9]+((e|E)(-)?[0-9]+)?(f|F|d|D)?");

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.CONFUSING_GROUP_NAME;
    }

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message(
                "confusing.floating.point.literal.display.name");
    }

    @NotNull
    protected String buildErrorString(Object... args) {
        return InspectionJSBundle.message(
                "confusing.floating.point.literal.problem.descriptor");
    }

    public InspectionJSFix buildFix(PsiElement location) {
        return new ConfusingFloatingPointLiteralFix();
    }

    private static class ConfusingFloatingPointLiteralFix
            extends InspectionJSFix {

        @NotNull
        public String getName() {
            return InspectionJSBundle.message(
                    "confusing.floating.point.literal.change.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final JSExpression literalExpression =
                    (JSExpression) descriptor.getPsiElement();
            final String text = literalExpression.getText();
            final String newText = getCanonicalForm(text);
            replaceExpression(literalExpression, newText);
        }

        private static String getCanonicalForm(String text) {
            final String suffix;
            final String prefix;
            if (text.indexOf((int) 'e') > 0) {
                final int breakPoint = text.indexOf((int) 'e');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else if (text.indexOf((int) 'E') > 0) {
                final int breakPoint = text.indexOf((int) 'E');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else if (text.indexOf((int) 'f') > 0) {
                final int breakPoint = text.indexOf((int) 'f');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else if (text.indexOf((int) 'F') > 0) {
                final int breakPoint = text.indexOf((int) 'F');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else if (text.indexOf((int) 'd') > 0) {
                final int breakPoint = text.indexOf((int) 'd');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else if (text.indexOf((int) 'D') > 0) {
                final int breakPoint = text.indexOf((int) 'D');
                suffix = text.substring(breakPoint);
                prefix = text.substring(0, breakPoint);
            } else {
                suffix = "";
                prefix = text;
            }
            final int indexPoint = prefix.indexOf((int) '.');
            if (indexPoint < 0) {
                return prefix + ".0" + suffix;
            } else if (indexPoint == 0) {
                return '0' + prefix + suffix;
            } else {
                return prefix + '0' + suffix;
            }

        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ConfusingFloatingPointLiteralVisitor();
    }

    private static class ConfusingFloatingPointLiteralVisitor
            extends BaseInspectionVisitor {

        @Override public void visitJSLiteralExpression(
                @NotNull JSLiteralExpression literal) {
            super.visitJSLiteralExpression(literal);
            final String text = literal.getText();
            if (text == null) {
                return;
            }
            if (!isFloatingPoint(literal)) {
                return;
            }
            if (!isConfusing(text)) {
                return;
            }
            registerError(literal);
        }
    }

    private static boolean isConfusing(String text) {
        final Matcher matcher = pickyFloatingPointPattern.matcher(text);
        return !matcher.matches();
    }

    private static boolean isFloatingPoint(JSLiteralExpression literal) {
        final String text = literal.getText();
        final char firstChar = text.charAt(0);
        if (firstChar != '.' && !Character.isDigit(firstChar)) {
            return false;
        }
        return text.contains(".") || text.contains("e") || text.contains("E");
    }

}
