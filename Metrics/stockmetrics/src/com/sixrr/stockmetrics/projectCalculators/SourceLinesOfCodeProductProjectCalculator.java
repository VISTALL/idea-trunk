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

package com.sixrr.stockmetrics.projectCalculators;

import com.intellij.psi.*;
import com.sixrr.stockmetrics.utils.LineUtil;
import com.sixrr.metrics.utils.TestUtils;

public class SourceLinesOfCodeProductProjectCalculator extends ElementCountProjectCalculator {

    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);
            if (!TestUtils.isTest(file)) {
                numElements += LineUtil.countLines(file);
            }
        }

        public void visitComment(PsiComment comment) {
            super.visitComment(comment);
            final PsiFile file = comment.getContainingFile();
            if (!TestUtils.isTest(file)) {
                numElements -= LineUtil.countCommentOnlyLines(comment);
            }
        }
    }
}
