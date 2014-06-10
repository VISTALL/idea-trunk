package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 17.03.2009
 * Time: 14:15:34
 * To change this template use File | Settings | File Templates.
 */

class CfmlFunctionNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
    public void addCompletions(@NotNull final CompletionParameters parameters,
                               final ProcessingContext context,
                               @NotNull final CompletionResultSet result) {
        for (String s : CfmlUtil.getPredifinedFunctions()) {
            addFunctionName(result.caseInsensitive(), s);
        }/*
        for (String s : CfmlPsiUtil.getFunctionsNamesDefined(parameters.getOriginalFile())) {
            addFunctionName(result, lookupElementFactory, s);
        }
        */
    }

    private static void addFunctionName(CompletionResultSet result, String s) {
        result.addElement(LookupElementBuilder.create(s).setInsertHandler(ParenthesesInsertHandler.WITH_PARAMETERS));
    }
}
