/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.editorActions;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;
import static com.intellij.velocity.psi.VtlElementTypes.*;


/**
 * @author Alexey Chmutov
 */
public class VelocityQuoteHandler extends SimpleTokenSetQuoteHandler {
    public VelocityQuoteHandler() {
        super(SINGLE_QUOTE, DOUBLE_QUOTE);
    }

    public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
        if (!myLiteralTokenSet.contains(iterator.getTokenType()) || iterator.getEnd() - iterator.getStart() != 1) {
            return false;
        }
        return !isOpeningQuoteInternal(iterator);
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        if (!myLiteralTokenSet.contains(iterator.getTokenType()) || offset != iterator.getStart()) {
            return false;
        }
        return isOpeningQuoteInternal(iterator);
    }

    private boolean isOpeningQuoteInternal(final HighlighterIterator iterator) {
        iterator.retreat();
        try {
            if (iterator.atEnd()) {
                return true;
            }
            final IElementType type = iterator.getTokenType();
            return !(myLiteralTokenSet.contains(type) || STRING_TEXT.equals(type) || CHAR_ESCAPE.equals(type));
        } finally {
            iterator.advance();
        }
    }

    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
        int start = iterator.getStart();
        try {
            Document doc = editor.getDocument();
            CharSequence chars = doc.getCharsSequence();
            int lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset));

            while (!iterator.atEnd() && iterator.getStart() < lineEnd) {
                IElementType tokenType = iterator.getTokenType();

                if (myLiteralTokenSet.contains(tokenType) &&
                        (iterator.getStart() >= iterator.getEnd() - 1
                         || chars.charAt(iterator.getEnd() - 1) != '\"' && chars.charAt(iterator.getEnd() - 1) != '\'')) {
                    return true;
                }

                iterator.advance();
            }
        } finally {
            while (iterator.atEnd() || iterator.getStart() != start) {
                iterator.retreat();
            }
        }

        return false;
    }

}
