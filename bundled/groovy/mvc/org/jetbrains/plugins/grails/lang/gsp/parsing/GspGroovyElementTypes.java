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

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.lang.ASTNode;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.lexer.IGspElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.chameleons.GroovyDeclarationsInGspFileRoot;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GrGspClassImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GrGspRunMethodImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GrGspRunBlockImpl;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author ilyas
 */
public interface GspGroovyElementTypes extends GspTokenTypesEx {
  IFileElementType GSP_GROOVY_DECLARATIONS_ROOT = new GroovyDeclarationsInGspFileRoot("GROOVY_DECLARATIONS_IN_GSP_FILE");

  IElementType GSP_CLASS = new GroovyElementType.PsiCreator("GSP_CLASS") {
    @Override
    public GroovyPsiElement createPsi(@NotNull ASTNode node) {
      return new GrGspClassImpl(node);
    }
  };
  IElementType GSP_RUN_METHOD = new GroovyElementType.PsiCreator("GSP_RUN_METHOD") {
    @Override
    public GroovyPsiElement createPsi(@NotNull ASTNode node) {
      return new GrGspRunMethodImpl(node);
    }
  };
  IElementType GSP_RUN_BLOCK = new GroovyElementType.PsiCreator("GSP_RUN_BLOCK") {
    @Override
    public GroovyPsiElement createPsi(@NotNull ASTNode node) {
      return new GrGspRunBlockImpl(node);
    }
  };
  
  IElementType GSP_TEMPLATE_STATEMENT = new IGspElementType("GSP_TEMPLATE_STATEMENT");
}
