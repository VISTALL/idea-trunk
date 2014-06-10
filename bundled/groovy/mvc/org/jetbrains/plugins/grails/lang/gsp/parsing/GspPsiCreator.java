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

package org.jetbrains.plugins.grails.lang.gsp.parsing;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.template.GspTemplateStatementImpl;

/**
 * @author ilyas
 */
public class GspPsiCreator implements GspGroovyElementTypes, GspElementTypes {
  private GspPsiCreator() {}

  public static PsiElement createElement(ASTNode node) {

    IElementType type = node.getElementType();

    if (GSP_TEMPLATE_STATEMENT.equals(type)) return new GspTemplateStatementImpl(node);

    return new ASTWrapperPsiElement(node);
  }
}
