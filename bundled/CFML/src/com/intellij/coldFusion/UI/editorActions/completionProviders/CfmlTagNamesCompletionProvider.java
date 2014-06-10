package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementFactoryImpl;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 17.03.2009
 * Time: 14:17:13
 * To change this template use File | Settings | File Templates.
 */
class CfmlTagNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
    public void addCompletions(@NotNull final CompletionParameters parameters,
                               final ProcessingContext context,
                               @NotNull final CompletionResultSet result) {
        final LookupElementFactoryImpl lookupElementFactory = LookupElementFactoryImpl.getInstance();
        if (parameters.getOriginalFile().getFileType() != CfmlFileType.INSTANCE) {
            return;
        }

        for (String s : CfmlUtil.getTagList()) {
            result.addElement(LookupElementBuilder.create(s).setCaseSensitive(false));
        }
    }
}
