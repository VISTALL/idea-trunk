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

package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.*;

public class NumParametersCalculator extends MethodCalculator {
    private int methodNestingDepth = 0;

    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        public void visitMethod(PsiMethod method) {
            if (methodNestingDepth == 0) {
                final PsiParameterList parameterList = method.getParameterList();
                final PsiParameter[] parameters = parameterList.getParameters();
                if (parameters == null) {
                    return;
                }
                postMetric(method, parameters.length);
            }
            methodNestingDepth++;
            super.visitMethod(method);
            methodNestingDepth--;
        }
    }
}
