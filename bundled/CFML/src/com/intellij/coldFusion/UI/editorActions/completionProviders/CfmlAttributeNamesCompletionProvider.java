package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 17.03.2009
 * Time: 14:18:15
 * To change this template use File | Settings | File Templates.
 */
class CfmlAttributeNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
    public void addCompletions(@NotNull final CompletionParameters parameters,
                               final ProcessingContext context,
                               @NotNull final CompletionResultSet result) {

        PsiElement element = parameters.getPosition();
        while (element != null && !(element instanceof CfmlTag)) {
            element = element.getParent();
        }
        if (element == null) {
            return;
        }
        CfmlTag tag = (CfmlTag) element;
        String tagName = tag.getTagName();
        for (CfmlUtil.AttributeFormat s : CfmlUtil.getAttributes(tagName)) {
            if (s.getName() == null) {
                continue;
            }
            result.addElement(TailTypeDecorator.withTail(LookupElementBuilder.create(s.getName()).
                    setCaseSensitive(false), new TailType() {
                public int processTail(Editor editor, int tailOffset) {
                    HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(tailOffset);
                    if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.WHITE_SPACE) iterator.advance();
                    if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.ASSIGN) iterator.advance();
                    else {
                        editor.getDocument().insertString(tailOffset, "=\"\"");
                        return moveCaret(editor, tailOffset, 2);
                    }
                    int offset = iterator.getStart();
                    if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.WHITE_SPACE) iterator.advance();
                    if (!iterator.atEnd() && CfmlTokenTypes.STRING_ELEMENTS.contains(iterator.getTokenType())) {
                        return tailOffset;
                    }

                    editor.getDocument().insertString(offset, "\"\"");
                    return moveCaret(editor, tailOffset, offset - tailOffset + 1);
                }
            }));
        }
    }
}
