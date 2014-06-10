package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: vnikolaenko
 * Date: 17.03.2009
 */
class CfmlAttributeValuesCompletionProvider extends CompletionProvider<CompletionParameters> {
    public void addCompletions(@NotNull final CompletionParameters parameters,
                               final ProcessingContext context,
                               @NotNull final CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        while (element != null && !(element instanceof CfmlAttribute)) {
            element = element.getParent();
        }
        if (element == null) {
            return;
        }
        CfmlAttribute attribute = (CfmlAttribute) element;
        String attributeName = attribute.getFirstChild().getText();
        while (element != null && !(element instanceof CfmlTag)) {
            element = element.getParent();
        }
        if (element == null) {
            return;
        }
        CfmlTag tag = (CfmlTag) element;
        String tagName = tag.getName();

        String[] attributeValue = CfmlUtil.getAttributeValues(tagName, attributeName);
        if (attributeValue == null) {
            return;
        }
        for (String s : attributeValue) {
            result.addElement(LookupElementBuilder.create(s).setCaseSensitive(false));
        }
    }
}
