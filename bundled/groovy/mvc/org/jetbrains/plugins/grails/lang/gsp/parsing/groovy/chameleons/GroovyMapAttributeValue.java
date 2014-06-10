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
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.CustomParsingType;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.GspAwareGroovyParser;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GrMapAttrValueImpl;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyLexer;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.primary.ListOrMapConstructorExpression;

/**
 * @author ilyas
 */
public class GroovyMapAttributeValue extends CustomParsingType {
  public GroovyMapAttributeValue(String debugName) {
    super(debugName, GspFileType.GSP_FILE_TYPE.getLanguage());
  }

  @NotNull
  public Language getLanguage() {
    return GspFileType.GSP_FILE_TYPE.getLanguage();
  }

  public ASTNode parse(CharSequence text, CharTable table) {
    GrMapAttrValueImpl root = new GrMapAttrValueImpl();

    Language groovyLanguage = GroovyFileType.GROOVY_FILE_TYPE.getLanguage();
    final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(new GroovyLexer(), groovyLanguage, text);

    PsiBuilder.Marker rootMarker = builder.mark();
    ListOrMapConstructorExpression.parse(builder, new GspAwareGroovyParser());
    while (!builder.eof()) {
      PsiBuilder.Marker err = builder.mark();
      builder.advanceLexer();
      err.error(GrailsBundle.message("wrong.groovy.code"));
    }
    rootMarker.done(this);

    root.rawAddChildren((TreeElement)builder.getTreeBuilt().getFirstChildNode());

    return root;
  }

}
