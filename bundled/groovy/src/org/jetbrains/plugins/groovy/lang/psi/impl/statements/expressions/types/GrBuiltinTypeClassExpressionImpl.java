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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrBuiltinTypeClassExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrExpressionImpl;

/**
 * @author ven
 */
public class GrBuiltinTypeClassExpressionImpl extends GrExpressionImpl implements GrBuiltinTypeClassExpression {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.types.GrBuiltinTypeClassExpressionImpl");

  private static final Function<GrBuiltinTypeClassExpressionImpl, PsiType> TYPES_CALCULATOR = new MyTypesCalculator();

  public GrBuiltinTypeClassExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitBuiltinTypeClassExpression(this);
  }

  public String toString() {
    return "builtin type class expression";
  }

  public PsiType getType() {
    return GroovyPsiManager.getInstance(getProject()).getType(this, TYPES_CALCULATOR);
  }

  private static class MyTypesCalculator implements Function<GrBuiltinTypeClassExpressionImpl, PsiType> {
    public PsiType fun(GrBuiltinTypeClassExpressionImpl expression) {
      JavaPsiFacade facade = JavaPsiFacade.getInstance(expression.getProject());
      PsiClass clazz = facade.findClass("java.lang.Class", expression.getResolveScope());
      if (clazz != null) {
        PsiElementFactory factory = facade.getElementFactory();
        PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
        PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
        if (typeParameters.length == 1) {
          try {
            PsiType type = factory.createTypeFromText(expression.getText(), null);
            substitutor = substitutor.put(typeParameters[0], type);
          } catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
        return factory.createType(clazz, substitutor);
      }
      return null;
    }
  }
}