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
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.GspGroovyParser;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.lexer.GspGroovyLexer;
import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * Root element of Groovy representation of GSP file
 *
 * @author ilyas
 */
public class GroovyDeclarationsInGspFileRoot extends IFileElementType {
  public GroovyDeclarationsInGspFileRoot(String debugName) {
    super(debugName, GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
  }

  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(GspTokenTypesEx.GSP_TEMPLATE_DATA);

  public ASTNode parseContents(final ASTNode chameleon) {

    final PeerFactory factory = PeerFactory.getInstance();
    final PsiElement parentElement = chameleon.getPsi();
    final Project project = JavaPsiFacade.getInstance(parentElement.getProject()).getProject();

    GspGroovyLexer gspGroovyLexer = new GspGroovyLexer();
    MergingLexerAdapter lexer = new MergingLexerAdapter(gspGroovyLexer, TOKENS_TO_MERGE);
    Language groovyLanguage = GroovyFileType.GROOVY_FILE_TYPE.getLanguage();
    final PsiBuilder builder = factory.createBuilder(chameleon, lexer, groovyLanguage, chameleon.getText(), project);
    final PsiParser parser = new GspGroovyParser();

    return parser.parse(this, builder).getFirstChildNode();
  }

}
