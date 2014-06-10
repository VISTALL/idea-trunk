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

package org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.chameleons;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.GspGroovyExpressionParser;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GrGspExprInjectionImpl;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyLexer;

/**
 * @author ilyas
 */
public class GroovyExpressionInjectionElement extends ILazyParseableElementType {
  public GroovyExpressionInjectionElement(String debugName) {
    super(debugName, GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
  }

  public ASTNode parseContents(final ASTNode chameleon) {

    final PeerFactory factory = PeerFactory.getInstance();
    final PsiElement parentElement = chameleon.getTreeParent().getPsi();
    final Project project = parentElement.getProject();

    Language groovyLanguage = GroovyFileType.GROOVY_FILE_TYPE.getLanguage();
    final PsiBuilder builder = factory.createBuilder(chameleon, new GroovyLexer(), groovyLanguage, chameleon.getText(), project);
    final PsiParser parser = new GspGroovyExpressionParser();

    return parser.parse(this, builder).getFirstChildNode();
  }

  @Override
  public ASTNode createNode(CharSequence text) {
    return new GrGspExprInjectionImpl(text);
  }
}
