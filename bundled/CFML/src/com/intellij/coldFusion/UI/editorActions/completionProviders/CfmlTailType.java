package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.TailType;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

/**
 * @author vnikolaenko
 */
public class CfmlTailType extends TailType {
    public static final TailType PARENTHS = new CfmlTailType(); 
    public int processTail(Editor editor, int tailOffset) {
        HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(tailOffset);
        if (iterator.getTokenType() != CfscriptTokenTypes.L_BRACKET) {
            editor.getDocument().insertString(tailOffset, "()");
        }
        return moveCaret(editor, tailOffset, 1);
    }
}
