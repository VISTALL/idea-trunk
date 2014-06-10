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

package org.jetbrains.plugins.grails.lang.gsp.parsing.groovy;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspGroovyElementTypes;

/**
 * @author ilyas
 */
public class GspGroovyParser implements PsiParser, GspGroovyElementTypes {
  @NotNull

  public ASTNode parse(IElementType root, PsiBuilder builder) {

    PsiBuilder.Marker rootMarker = builder.mark();
    PsiBuilder.Marker gspClass = builder.mark();
    PsiBuilder.Marker runMethod = builder.mark();
    PsiBuilder.Marker runBlock = builder.mark();

    new GspAwareGroovyParser().parseBlockBody(builder);

    while (!builder.eof()) {
      PsiBuilder.Marker err = builder.mark();
      builder.advanceLexer();
      err.error(GrailsBundle.message("wrong.groovy.code"));
    }

    runBlock.done(GSP_RUN_BLOCK);
    runMethod.done(GSP_RUN_METHOD);
    gspClass.done(GSP_CLASS);
    rootMarker.done(root);
    return builder.getTreeBuilt();

  }

}
