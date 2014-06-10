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

import com.intellij.lang.PsiBuilder;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspGroovyElementTypes;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class GspTemplateStmtParsing implements GspTokenTypesEx, GspGroovyElementTypes {

  public static boolean parseGspTemplateStmt(PsiBuilder builder) {
    boolean smthParsed = false;
    if (JSCRIPT_END.equals(builder.getTokenType()) ||
            GSCRIPT_END.equals(builder.getTokenType())) {
      eatTemplateStatement(builder);
      smthParsed = true;
    }
    while (GSP_TEMPLATE_DATA.equals(builder.getTokenType()) ||
            GSP_COMMENTS.contains(builder.getTokenType()) ||
            GSP_GROOVY_SEPARATORS.contains(builder.getTokenType())) {
      smthParsed = true;
      if (GSP_TEMPLATE_DATA.equals(builder.getTokenType()) ||
              GSP_COMMENTS.contains(builder.getTokenType())) {
        eatTemplateStatement(builder);
      }
      if (JSCRIPT_BEGIN.equals(builder.getTokenType()) ||
              GSCRIPT_BEGIN.equals(builder.getTokenType())) {
        eatTemplateStatement(builder);
        return smthParsed;
      }
      /*
      ${...} or <%= ... %> injection
       */
      if (JEXPR_BEGIN.equals(builder.getTokenType()) ||
              GEXPR_BEGIN.equals(builder.getTokenType())) {
        parseExprInjection(builder);
      }
      /*
      !{...}! or <%! ... %> declaration
       */
      if (JDECLAR_BEGIN.equals(builder.getTokenType()) ||
              GDECLAR_BEGIN.equals(builder.getTokenType())) {
        parseDeclaration(builder);
      }
      /*
      Map value of Grails gtag attribute
       */
      if (GSP_MAP_ATTR_VALUE.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
    }
    return smthParsed;
  }


  private static void eatTemplateStatement(PsiBuilder builder) {
    ParserUtils.advance(builder);
  }

  private static void parseExprInjection(PsiBuilder builder) {
    eatTemplateStatement(builder);
    if (GROOVY_EXPR_CODE.equals(builder.getTokenType())) {
      builder.advanceLexer();
    } else {
      builder.error(GroovyBundle.message("expression.expected"));
    }
    if (JEXPR_END.equals(builder.getTokenType()) ||
            GEXPR_END.equals(builder.getTokenType())) {
      eatTemplateStatement(builder);
    } else {
      builder.error(GrailsBundle.message("expr.closing.end.tag.expected"));
      while (!builder.eof()) {
        builder.advanceLexer();
      }
    }
  }

  private static void parseDeclaration(PsiBuilder builder) {
    eatTemplateStatement(builder);
    if (GROOVY_DECLARATION.equals(builder.getTokenType())) {
      builder.advanceLexer();
    } else {
      builder.error(GrailsBundle.message("declaraion.expected"));
    }
    if (JDECLAR_END.equals(builder.getTokenType()) ||
            GDECLAR_END.equals(builder.getTokenType())) {
      eatTemplateStatement(builder);
    } else {
      builder.error(GrailsBundle.message("expr.closing.end.tag.expected"));
      while (!builder.eof()) {
        builder.advanceLexer();
      }
    }
  }

  private static void parseDirective(PsiBuilder builder) {
    eatTemplateStatement(builder);
    if (GSP_DIRECTIVE.equals(builder.getTokenType())) {
      builder.advanceLexer();
    } else {
      builder.error(GrailsBundle.message("directive.expected"));
    }
    if (JDIRECT_END.equals(builder.getTokenType()) ||
            GDIRECT_END.equals(builder.getTokenType())) {
      eatTemplateStatement(builder);
    } else {
      builder.error(GrailsBundle.message("expr.closing.end.tag.expected"));
      while (!builder.eof()) {
        builder.advanceLexer();
      }
    }
  }


}
