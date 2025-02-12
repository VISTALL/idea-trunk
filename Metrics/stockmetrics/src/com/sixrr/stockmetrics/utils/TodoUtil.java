/*
 * Copyright 2005, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.stockmetrics.utils;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.TodoItem;

public class TodoUtil {
    private TodoUtil() {
        super();
    }

    public static boolean isTodoComment(PsiComment comment) {
        final PsiFile file = comment.getContainingFile();
        final PsiManager psiManager = comment.getManager();
        final PsiSearchHelper searchHelper = psiManager.getSearchHelper();
        final TodoItem[] todoItems = searchHelper.findTodoItems(file);
        for (final TodoItem todoItem : todoItems) {
            final TextRange commentTextRange = comment.getTextRange();
            final TextRange todoTextRange = todoItem.getTextRange();
            if (commentTextRange.contains(todoTextRange)) {
                return true;
            }
        }
        return false;
    }
}
