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

package com.sixrr.stockmetrics.moduleCalculators;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.sixrr.metrics.utils.ClassUtils;

public class PercentMethodsJavadocedModuleCalculator extends ElementRatioModuleCalculator {

    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            final PsiClass containingClass = method.getContainingClass();
            if (containingClass == null || ClassUtils.isAnonymous(containingClass)) {
                return;
            }
            if (method.getFirstChild()instanceof PsiDocComment) {
                incrementNumerator(method, 1);
            }
            incrementDenominator(method, 1);
        }

        public void visitFile(PsiFile file) {
            super.visitFile(file);
            final Module module = ClassUtils.calculateModule(file);
            numeratorPerModule.createBucket(module);
            denominatorPerModule.createBucket(module);
        }
    }
}
