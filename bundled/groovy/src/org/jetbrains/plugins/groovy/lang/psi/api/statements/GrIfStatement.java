/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.psi.api.statements;

import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.psi.api.formatter.GrControlStatement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.annotations.Nullable;
import com.intellij.util.IncorrectOperationException;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiElement;

/**
 * @autor: ilyas
 */
public interface GrIfStatement extends GrStatement, GrControlStatement {

    public GrCondition getCondition();

    public GrStatement getThenBranch();

    public GrStatement getElseBranch();

    public GrStatement replaceThenBranch(GrStatement newBranch) throws IncorrectOperationException;

    public GrStatement replaceElseBranch(GrStatement newBranch) throws IncorrectOperationException;

    @Nullable
    public PsiElement getElseKeyword();

    @Nullable
    public PsiElement getRParenth();

    @Nullable
    public PsiElement getLParenth();
}
