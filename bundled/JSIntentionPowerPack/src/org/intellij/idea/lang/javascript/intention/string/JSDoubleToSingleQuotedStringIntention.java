/*
 * Copyright 2005-2006 Olivier Descout
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.idea.lang.javascript.intention.string;

import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.intention.JSIntention;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

public class JSDoubleToSingleQuotedStringIntention extends JSIntention {
    @NotNull
    protected JSElementPredicate getElementPredicate() {
        return new DoubleToSingleQuotedStringPredicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final JSLiteralExpression stringLiteral = (JSLiteralExpression) element;

        JSElementFactory.replaceExpression(stringLiteral, changeQuotes(stringLiteral.getText()));
    }

    static String changeQuotes(String stringLiteral) {
        StringBuilder buffer      = new StringBuilder(stringLiteral);
        int           simpleIndex = stringLiteral.lastIndexOf(StringUtil.SIMPLE_QUOTE);
        int           doubleIndex = stringLiteral.lastIndexOf(StringUtil.DOUBLE_QUOTE, stringLiteral.length() - 2);

        while (simpleIndex >= 0 || doubleIndex > 0) {
            if (simpleIndex > doubleIndex) {
                if (stringLiteral.charAt(simpleIndex - 1) != StringUtil.BACKSLASH) {
                  buffer.insert(simpleIndex, StringUtil.BACKSLASH);
                }
                simpleIndex = stringLiteral.lastIndexOf(StringUtil.SIMPLE_QUOTE, simpleIndex - 1);
            } else {
                if (stringLiteral.charAt(doubleIndex - 1) == StringUtil.BACKSLASH) {
                    buffer.deleteCharAt(doubleIndex - 1);
                }
                doubleIndex = stringLiteral.lastIndexOf(StringUtil.DOUBLE_QUOTE, doubleIndex - 2);
            }
        }
        buffer.setCharAt(0,                   StringUtil.SIMPLE_QUOTE);
        buffer.setCharAt(buffer.length() - 1, StringUtil.SIMPLE_QUOTE);

        return buffer.toString();
    }

    private static class DoubleToSingleQuotedStringPredicate implements JSElementPredicate {

        public boolean satisfiedBy(@NotNull PsiElement element) {
            if (!(element instanceof JSLiteralExpression)) {
                return false;
            }
            return StringUtil.isDoubleQuoteStringLiteral((JSLiteralExpression) element);
        }
    }
}
