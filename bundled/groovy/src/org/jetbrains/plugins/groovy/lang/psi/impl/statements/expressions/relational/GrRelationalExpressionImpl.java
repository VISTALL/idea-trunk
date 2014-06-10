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

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.relational;

import com.intellij.lang.ASTNode;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrRelationalExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrBinaryExpressionImpl;

/**
 * @author ilyas
 */
public class GrRelationalExpressionImpl extends GrBinaryExpressionImpl implements GrRelationalExpression {

  public GrRelationalExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "Relational expression";
  }

  public PsiType getType() {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName("java.lang.Boolean", getResolveScope());
  }
}