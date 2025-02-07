/*
 *  Copyright 2000-2007 JetBrains s.r.o.
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
 *
 */

package org.jetbrains.plugins.groovy.lang.psi.impl.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrBuiltInTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;

/**
 * @author ilyas
 */
public class GrBuiltInTypeElementImpl extends GroovyPsiElementImpl implements GrBuiltInTypeElement {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.lang.psi.impl.types.GrBuiltInTypeElementImpl");

  public GrBuiltInTypeElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitBuiltinTypeElement(this);
  }

  public String toString() {
    return "Built in type";
  }

  private static final PsiType[] PRIMITIVES = new PsiType[]{PsiType.BYTE, PsiType.CHAR,
      PsiType.DOUBLE, PsiType.FLOAT,
      PsiType.INT, PsiType.SHORT,
      PsiType.LONG, PsiType.BOOLEAN,
      PsiType.VOID
  };

  @NotNull
  public PsiType getType() {
    String typeText = getText();
    for (final PsiType primitive : PRIMITIVES) {
      if (PsiType.VOID.equals(primitive)) {
        return primitive;
      }
      if (primitive.getCanonicalText().equals(typeText)) {
        return primitive;
      }

//      if (primitive.getCanonicalText().equals(typeText)) {
//        final String boxedQName = ((PsiPrimitiveType) primitive).getBoxedTypeName();
//        return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(boxedQName, getResolveScope());
//      }
    }

    LOG.error("Unknown primitive type");
    return null;
  }
}