package com.intellij.velocity.editorActions;

import com.intellij.codeInsight.TailType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;

/**
 * @author Alexey Chmutov
 */
public class VtlTailType extends TailType {

    public static final TailType METHOD_CALL_TAIL_TYPE = new VtlTailType(false);

    private final boolean myClosingBraceNeeded;

    public VtlTailType(boolean closingBraceNeeded) {
        myClosingBraceNeeded = closingBraceNeeded;
    }

    public int processTail(final Editor editor, final int tailOffset) {
        Document document = editor.getDocument();
        int textLength = document.getTextLength();
        CharSequence chars = document.getCharsSequence();
        int offsetToInsertOpeningParen = tailOffset;
        if (myClosingBraceNeeded) {
            if (offsetToInsertOpeningParen == textLength || chars.charAt(offsetToInsertOpeningParen) != '}') {
                document.insertString(offsetToInsertOpeningParen, "}");
                textLength++;
            }
            offsetToInsertOpeningParen++;
        }
        int existingOpeningParenOffset = offsetToInsertOpeningParen;
        if (openingParenNeeded()) {
            while (existingOpeningParenOffset < textLength && chars.charAt(existingOpeningParenOffset) == ' ') {
                existingOpeningParenOffset++;
            }
            if (existingOpeningParenOffset == textLength || chars.charAt(existingOpeningParenOffset) != '(') {
                document.insertString(offsetToInsertOpeningParen, "()");
                existingOpeningParenOffset = offsetToInsertOpeningParen;
            }
            existingOpeningParenOffset++;
        }
        return moveCaret(editor, tailOffset, existingOpeningParenOffset - tailOffset);
    }

    protected boolean openingParenNeeded() {
        return true;
    }
}
