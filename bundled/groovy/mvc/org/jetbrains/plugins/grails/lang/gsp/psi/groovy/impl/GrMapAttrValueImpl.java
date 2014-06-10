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

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.GspCompositePsiElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrMapAttributeValue;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author ilyas
 */
public class GrMapAttrValueImpl extends GspCompositePsiElement implements GrMapAttributeValue {

  public GrMapAttrValueImpl() {
    super(GspTokenTypes.GSP_MAP_ATTR_VALUE);
  }

  public String toString() {
    return "Groovy Map Value";
  }

  public void accept(GroovyElementVisitor visitor) {
    GrExpression expression = getExpression();
    if (!(expression instanceof GrListOrMap)) return;
    visitor.visitListOrMap(((GrListOrMap) expression));
  }

  @Nullable
  public GrExpression getExpression() {
    return findChildByClass(GrExpression.class);
  }

  public GrStatement replaceWithStatement(GrStatement statement) {
    return null;
  }

  public void removeStatement() throws IncorrectOperationException {

  }
}
