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

package org.jetbrains.plugins.grails.lang.gsp.parsing.groovy.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspFlexLexer;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyLexer;

/**
 * @author ilyas
 */
public class GspGroovyLexer extends Lexer implements GspTokenTypes {

  static final TokenSet TOKENS_TO_IGNORE = TokenSet.create(
          GTAG_END_TAG_START,
          GTAG_START_TAG_END,
          GTAG_START_TAG_START,
          GTAG_TAG_END,
          JDIRECT_BEGIN,
          JDIRECT_END,
          GDIRECT_BEGIN,
          GDIRECT_END,
          GSP_WHITE_SPACE,
          GSP_TAG_NAME,
          GSP_ATTR_NAME,
          GSP_EQ,
          GSP_BAD_CHARACTER,
          GSP_ATTR_VALUE_START_DELIMITER,
          GSP_ATTR_VALUE_END_DELIMITER,
          GSP_ATTRIBUTE_VALUE_TOKEN,
          GSP_STYLE_COMMENT,
          JSP_STYLE_COMMENT
  );


  private final Lexer myGspLexer = new GspFlexLexer();
  private final GroovyLexer myGroovyLexer = new GroovyLexer();

  private Lexer myCurrentGroovyLexer = null;

  public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myGspLexer.start(buffer, startOffset, endOffset, initialState);
    setUpGroovyLexer();
  }

  public int getState() {
    return myGspLexer.getState();
  }

  @Nullable
  public IElementType getTokenType() {
    IElementType tokenType = myGspLexer.getTokenType();

    if (GROOVY_CODE.equals(tokenType)) {
      return myCurrentGroovyLexer.getTokenType();
    }
    return convertTokenType(tokenType);
  }

  private IElementType convertTokenType(IElementType tokenType) {
    if (TOKENS_TO_IGNORE.contains(tokenType) ||
            GSP_DIRECTIVE == tokenType ||
            XmlTokenType.XML_WHITE_SPACE == tokenType) {
      return GspTokenTypesEx.GSP_TEMPLATE_DATA;
    }
    return tokenType;
  }

  public int getTokenStart() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (GROOVY_CODE.equals(tokenType)) {
      return myCurrentGroovyLexer.getTokenStart();
    }
    return myGspLexer.getTokenStart();
  }

  public int getTokenEnd() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (GROOVY_CODE.equals(tokenType)) {
      return myCurrentGroovyLexer.getTokenEnd();
    }
    return myGspLexer.getTokenEnd();
  }

  public void advance() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (GROOVY_CODE.equals(tokenType)) {
      myCurrentGroovyLexer.advance();
      if (myCurrentGroovyLexer.getTokenType() != null) {
        return;
      }
    }
    myGspLexer.advance();
    setUpGroovyLexer();
  }

  private void setUpGroovyLexer() {
    while (true) {
      IElementType tokenType = myGspLexer.getTokenType();
      if (tokenType == GROOVY_CODE) {
        myCurrentGroovyLexer = myGroovyLexer;
      } else {
        return;
      }

      myCurrentGroovyLexer.start(myGspLexer.getBufferSequence(), myGspLexer.getTokenStart(), myGspLexer.getTokenEnd());
      if (myCurrentGroovyLexer.getTokenType() != null) {
        return;
      }
      myGspLexer.advance();
    }
  }


  private static class GspPosition implements LexerPosition {
    private final LexerPosition myGroovyPosition;
    private final LexerPosition myGspPosition;

    public GspPosition(final LexerPosition groovyPosition, final LexerPosition gspPosition) {
      myGroovyPosition = groovyPosition;
      myGspPosition = gspPosition;
    }

    public int getOffset() {
      final int gspPos = myGspPosition != null ? myGspPosition.getOffset() : 0;
      final int groovyPos = myGroovyPosition == null ? 0 : myGroovyPosition.getOffset();
      return Math.max(gspPos, groovyPos);
    }

    public LexerPosition getGroovyPosition() {
      return myGroovyPosition;
    }

    public LexerPosition getGspPosition() {
      return myGspPosition;
    }

    public int getState() {
      throw new UnsupportedOperationException("Method getState is not yet implemented in " + getClass().getName());
    }
  }

  public LexerPosition getCurrentPosition() {
    return new GspPosition(myCurrentGroovyLexer != null ? myCurrentGroovyLexer.getCurrentPosition() : null,
            myGspLexer.getCurrentPosition());
  }

  public void restore(LexerPosition position) {
    if (position instanceof GspPosition) {
      GspPosition gspPosition = (GspPosition) position;
      myGspLexer.restore(gspPosition);

      LexerPosition groovyPosition = gspPosition.getGroovyPosition();
      if (groovyPosition != null &&
              myCurrentGroovyLexer != null &&
              groovyPosition.getOffset() < myCurrentGroovyLexer.getBufferEnd()) {
        myCurrentGroovyLexer.restore(groovyPosition);
      } else {
        myCurrentGroovyLexer = null;
        setUpGroovyLexer();
      }
    }

  }

  public CharSequence getBufferSequence() {
    return myGspLexer.getBufferSequence();
  }

  public int getBufferEnd() {
    return myGspLexer.getBufferEnd();
  }
}
