package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import static com.intellij.codeInsight.completion.CompletionType.BASIC;
import com.intellij.codeInsight.completion.DummyIdentifierPatcher;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 09.10.2008
 */
public class CfmlCompletionContributor extends CompletionContributor {

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        int offset = context.getStartOffset();
        if (offset == 0 || !context.getFile().getViewProvider().getLanguages().contains(CfmlLanguage.INSTANCE)) {
            return;
        }
        final PsiElement element = context.getFile().findElementAt(offset);
        if (element != null && element.getTextRange().getStartOffset() != offset
                && context.getFile().findReferenceAt(offset) != null) {
            context.setFileCopyPatcher(new DummyIdentifierPatcher(""));
        } else {/*
            final CharSequence chars = context.getEditor().getDocument().getCharsSequence();
            if (offset < 1) {
                return;
            }
            char currChar = chars.charAt(offset - 1);
            if (currChar == '<' || (offset >= 2 && currChar == '/' && chars.charAt(offset - 2) == '<')) {
                context.setFileCopyPatcher(new DummyIdentifierPatcher("cf"));
            } else if ((currChar == 'c' || currChar == 'C') &&
                    ((offset >= 2 && chars.charAt(offset - 2) == '<') || ((offset >= 3 && chars.charAt(offset - 2) == '/' && chars.charAt(offset - 3) == '<')))) {
                context.setFileCopyPatcher(new DummyIdentifierPatcher("f"));
            }
            */
        }
    }

    @Override
    public String handleEmptyLookup(@NotNull CompletionParameters parameters, Editor editor) {
        return super.handleEmptyLookup(parameters, editor);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CfmlCompletionContributor() {
        // tag completion providers (after < and </)
        /*
        extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("<")).withLanguage(StdLanguages.HTML),
                       new CfmlTagNamesCompletionProvider());
                       */
        // TODO: check fileType somewhere!!!!!!!!!!!!!!!!!!!!!!!

        // tag names completion in template data, in open and close constructions in cfml data
        extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("<")).withLanguage(StdLanguages.XML),
                new CfmlTagNamesCompletionProvider());
        extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("<")).withLanguage(CfmlLanguage.INSTANCE),
                new CfmlTagNamesCompletionProvider());
        extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("</")).withLanguage(CfmlLanguage.INSTANCE),
                new CfmlTagNamesCompletionProvider());
        // attributes completion
        extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.ATTRIBUTE).withLanguage(CfmlLanguage.INSTANCE),
                new CfmlAttributeNamesCompletionProvider());
        // predefined attributes values completion
        extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
                new CfmlAttributeValuesCompletionProvider());
        // predefined and user defined fucntion names completion
        extend(BASIC, PlatformPatterns.psiElement().
                withLanguage(CfmlLanguage.INSTANCE).
                withElementType(CfscriptTokenTypes.IDENTIFIER).
                with(new PatternCondition<PsiElement>("") {
                    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                        return psiElement.getPrevSibling() == null || psiElement.getPrevSibling().getNode().getElementType() != CfscriptTokenTypes.POINT;
                    }
        }), new CfmlFunctionNamesCompletionProvider());
    }
}
