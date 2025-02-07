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

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef;

import com.intellij.lang.ASTNode;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrExtendsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyBaseElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrReferenceListStub;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 26.03.2007
 */
public class GrExtendsClauseImpl extends GroovyBaseElementImpl<GrReferenceListStub>
    implements GrExtendsClause, StubBasedPsiElement<GrReferenceListStub> {

  public GrExtendsClauseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public GrExtendsClauseImpl(final GrReferenceListStub stub) {
    super(stub, GroovyElementTypes.EXTENDS_CLAUSE);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitExtendsClause(this);
  }

  public String toString() {
    return "Extends clause";
  }

  @NotNull
  public GrCodeReferenceElement[] getReferenceElements() {
    return findChildrenByClass(GrCodeReferenceElement.class);
  }
}
