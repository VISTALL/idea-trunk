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

package com.sixrr.stockmetrics.packageCalculators;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.sixrr.metrics.utils.BuckettedCount;
import com.sixrr.metrics.utils.ClassUtils;

import java.util.Set;

public class PercentFieldsJavadocedPackageCalculator extends PackageCalculator {

    private final BuckettedCount<PsiPackage> numJavadocedFieldsPerPackage = new BuckettedCount<PsiPackage>();
    private final BuckettedCount<PsiPackage> numFieldsPerPackage = new BuckettedCount<PsiPackage>();

    public void endMetricsRun() {
        final Set<PsiPackage> packages = numFieldsPerPackage.getBuckets();
        for (final PsiPackage packageName : packages) {
            final int numFields = numFieldsPerPackage.getBucketValue(packageName);
            final int numJavadocedFields = numJavadocedFieldsPerPackage.getBucketValue(packageName);

            postMetric(packageName, numJavadocedFields, numFields);
        }
    }

    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        public void visitField(PsiField field) {
            super.visitField(field);
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass == null || ClassUtils.isAnonymous(containingClass)) {
                return;
            }
            final PsiPackage aPackage = ClassUtils.findPackage(containingClass);
            numFieldsPerPackage.createBucket(aPackage);
            if (field.getFirstChild()instanceof PsiDocComment) {
                numJavadocedFieldsPerPackage.incrementBucketValue(aPackage);
            }
            numFieldsPerPackage.incrementBucketValue(aPackage);
        }
    }
}
