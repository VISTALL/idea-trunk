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

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrInstanceOfExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrExpressionImpl;

/**
 * @author ven
 */
public class GrInstanceofExpressionImpl extends GrExpressionImpl implements GrInstanceOfExpression {

  public GrInstanceofExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitInstanceofExpression(this);
  }

  public String toString() {
    return "Instanceof expression";
  }

  public PsiType getType() {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName("java.lang.Boolean", getResolveScope());
  }

  public GrTypeElement getTypeElement() {
    return findChildByClass(GrTypeElement.class);
  }

  public GrExpression getOperand() {
    return findChildByClass(GrExpression.class);
  }
}