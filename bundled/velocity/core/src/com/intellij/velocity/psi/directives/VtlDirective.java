package com.intellij.velocity.psi.directives;

import com.intellij.psi.PsiElement;
import com.intellij.velocity.psi.PsiUtil;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.psi.files.VtlFile;

/**
 * @author Alexey Chmutov
 */
public interface VtlDirective extends VtlDirectiveHolder {
    String[] DIRECTIVE_NAMES = {
            "break", "define", "else", "elseif", "end", "evaluate", "foreach", "if", "include", "literal", "macro", "parse", "set", "stop",
    };

    int getFoldingStartOffset();

    int getFoldingEndOffset();

    boolean needsClosing();

    class Validator {
        private Validator() {
        }

        public static boolean areParenthesesNeeded(final VtlDirective child, final String childName) {
            return !("break".equals(childName) || "else".equals(childName) || "end".equals(childName) || "stop".equals(childName));
        }

        public static boolean isAllowed(final VtlDirective child, final String childName) {
            PsiElement parent = child.getParent();
            if ("macro".equals(childName)) {
                return parent instanceof VtlFile;
            }
            if ("end".equals(childName)) {
                return PsiUtil.isTypeOf(parent, SHARP_FOREACH, SHARP_IF, SHARP_LITERAL, SHARP_MACRODECL, SHARP_DEFINE)
                        || PsiUtil.isTypeOf(parent.getParent(), SHARP_IF)
                        && PsiUtil.isTypeOf(parent, SHARP_ELSE, SHARP_ELSEIF);
            }
            if ("else".equals(childName) || "elseif".equals(childName)) {
                return PsiUtil.isTypeOf(parent, SHARP_IF)
                        || PsiUtil.isTypeOf(parent.getParent(), SHARP_IF)
                        && PsiUtil.isTypeOf(parent, SHARP_ELSEIF);
            }
            if ("break".equals(childName)) {
                return PsiUtil.isTypeOf(parent, SHARP_FOREACH);
            }
            return true;
        }
    }
}
