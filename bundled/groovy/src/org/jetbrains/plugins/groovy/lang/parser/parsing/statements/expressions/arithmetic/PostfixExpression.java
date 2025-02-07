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

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.arithmetic;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyParser;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class PostfixExpression implements GroovyElementTypes {


  private static final TokenSet POSTFIXES = TokenSet.create(
          mINC,
          mDEC
  );

  public static boolean parse(PsiBuilder builder, GroovyParser parser) {

    PsiBuilder.Marker marker = builder.mark();
    if (PathExpression.parse(builder, parser)) {
      subParse(builder, marker);
      return true;
    } else {
      marker.drop();
      return false;
    }
  }

  private static void subParse(PsiBuilder builder,
                               PsiBuilder.Marker marker
  ) {
    if (ParserUtils.getToken(builder, POSTFIXES)) {
      PsiBuilder.Marker newMarker = marker.precede();
      marker.done(POSTFIX_EXPRESSION);
      subParse(builder, newMarker);
    } else {
      marker.drop();
    }
  }


}
