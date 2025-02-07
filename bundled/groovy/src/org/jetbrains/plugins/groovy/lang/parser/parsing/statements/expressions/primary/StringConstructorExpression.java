/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
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
 */

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.primary;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyParser;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.blocks.OpenOrClosableBlock;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.arithmetic.PathExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class StringConstructorExpression implements GroovyElementTypes {

  public static GroovyElementType parse(PsiBuilder builder, GroovyParser parser) {

    Marker sMarker = builder.mark();
    if (ParserUtils.getToken(builder, mGSTRING_SINGLE_BEGIN)) {
      GroovyElementType result = stringConstructorValuePart(builder, parser);
      if (result.equals(WRONGWAY)) {
        builder.error(GroovyBundle.message("identifier.or.block.expected"));
        sMarker.done(GSTRING);
        return GSTRING;
      } else {
        while (ParserUtils.getToken(builder, mGSTRING_SINGLE_CONTENT) && !result.equals(WRONGWAY)) {
          result = stringConstructorValuePart(builder, parser);
        }
        ParserUtils.getToken(builder, mGSTRING_SINGLE_END, GroovyBundle.message("string.end.expected"));
        sMarker.done(GSTRING);
        return GSTRING;
      }
    } else {
      sMarker.drop();
      return WRONGWAY;
    }
  }

  /**
   * Parses heredoc's content in GString
   *
   * @param builder given builder
   * @return nothing
   */
  private static GroovyElementType stringConstructorValuePart(PsiBuilder builder, GroovyParser parser) {
    ParserUtils.getToken(builder, mSTAR);
    if (mIDENT.equals(builder.getTokenType())) {
      PathExpression.parse(builder, parser);
      return PATH_EXPRESSION;
    } else if (mLCURLY.equals(builder.getTokenType())) {
      OpenOrClosableBlock.parseClosableBlock(builder, parser);
      return CLOSABLE_BLOCK;
    }
    return WRONGWAY;
  }

}