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

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyParser;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.AssignmentExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.StrictContextExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class SwitchStatement implements GroovyElementTypes {

  public static boolean parse(PsiBuilder builder, GroovyParser parser) {
    PsiBuilder.Marker marker = builder.mark();
    ParserUtils.getToken(builder, kSWITCH);

    if (!ParserUtils.getToken(builder, mLPAREN, GroovyBundle.message("lparen.expected"))) {
      marker.done(SWITCH_STATEMENT);
      return true;
    }
    if (!StrictContextExpression.parse(builder, parser)) {
      builder.error(GroovyBundle.message("expression.expected"));
    }
    ParserUtils.getToken(builder, mNLS);

    if (!ParserUtils.getToken(builder, mRPAREN, GroovyBundle.message("rparen.expected"))) {
      while (!builder.eof() && !mNLS.equals(builder.getTokenType()) && !mRPAREN.equals(builder.getTokenType())) {
        builder.error(GroovyBundle.message("rparen.expected"));
        builder.advanceLexer();
      }
      if (!ParserUtils.getToken(builder, mRPAREN)) {
        marker.done(SWITCH_STATEMENT);
        return true;
      }
    }
    PsiBuilder.Marker warn = builder.mark();
    ParserUtils.getToken(builder, mNLS);

    if (!mLCURLY.equals(builder.getTokenType())) {
      warn.rollbackTo();
      builder.error(GroovyBundle.message("case.block.expected"));
      marker.done(SWITCH_STATEMENT);
      return true;
    }
    warn.drop();
    parseCaseBlock(builder, parser);
    marker.done(SWITCH_STATEMENT);
    return true;

  }

  /**
   * Parses cases block
   *
   * @param builder
   */
  private static void parseCaseBlock(PsiBuilder builder, GroovyParser parser) {
    ParserUtils.getToken(builder, mLCURLY);
    ParserUtils.getToken(builder, mNLS);
    if (ParserUtils.getToken(builder, mRCURLY)) {
      return;
    }
    if (!kCASE.equals(builder.getTokenType()) &&
        !kDEFAULT.equals(builder.getTokenType())) {
      builder.error(GroovyBundle.message("case.expected"));
      while (!builder.eof() &&
          !(kCASE.equals(builder.getTokenType()) ||
              kDEFAULT.equals(builder.getTokenType()) ||
              mRCURLY.equals(builder.getTokenType()))) {
        builder.error(GroovyBundle.message("case.expected"));
        builder.advanceLexer();
      }
    }

    while (kCASE.equals(builder.getTokenType()) ||
        kDEFAULT.equals(builder.getTokenType())) {
      PsiBuilder.Marker sectionMarker = builder.mark();
      parseCaseLabel(builder, parser);
      if (builder.getTokenType() == mRCURLY ||
          ParserUtils.lookAhead(builder, mNLS, mRCURLY)) {
        builder.error(GroovyBundle.message("expression.expected"));
      } else {
        parser.parseSwitchCaseList(builder);
      }
      sectionMarker.done(CASE_SECTION);
    }
    ParserUtils.getToken(builder, mRCURLY, GroovyBundle.message("rcurly.expected"));
  }

  /**
   * Parses one or more sequential 'case' or 'default' labels
   *
   * @param builder
   */
  public static void parseCaseLabel(PsiBuilder builder, GroovyParser parser) {
    PsiBuilder.Marker label = builder.mark();
    IElementType elem = builder.getTokenType();
    ParserUtils.getToken(builder, TokenSet.create(kCASE, kDEFAULT));

    if (kCASE.equals(elem)) {
      AssignmentExpression.parse(builder, parser);
    }
    ParserUtils.getToken(builder, mCOLON, GroovyBundle.message("colon.expected"));
    label.done(CASE_LABEL);
    ParserUtils.getToken(builder, mNLS);
    if (builder.getTokenType() == kCASE ||
        builder.getTokenType() == kDEFAULT) {
      parseCaseLabel(builder, parser);
    }
  }

}
