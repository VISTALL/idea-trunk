package com.intellij.velocity.inspections;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey Chmutov
 */
class StringCollectionExpression extends Expression {
    private final Collection<String> myAllOptions;
    private final Function<String, LookupElement> myMapper;

    public StringCollectionExpression(@NotNull Collection<String> allOptions) {
        this.myAllOptions = allOptions;
        this.myMapper = new Function<String, LookupElement>() {
            public LookupElement fun(final String option) {
                return LookupElementBuilder.create(option);
            }
        };
    }

    public StringCollectionExpression(@NotNull Collection<String> allOptions, @NotNull Function<String, LookupElement> mapper) {
        this.myAllOptions = allOptions;
        this.myMapper = mapper;
    }

    public com.intellij.codeInsight.template.Result calculateResult(final ExpressionContext context) {
        return calculateQuickResult(context);
    }

    public com.intellij.codeInsight.template.Result calculateQuickResult(final ExpressionContext context) {
        return myAllOptions.size() == 1 ? new TextResult(myAllOptions.iterator().next()) : null;
    }

    public LookupElement[] calculateLookupItems(final ExpressionContext context) {
        return ContainerUtil.map2Array(myAllOptions, LookupElement.class, myMapper);
    }
}
