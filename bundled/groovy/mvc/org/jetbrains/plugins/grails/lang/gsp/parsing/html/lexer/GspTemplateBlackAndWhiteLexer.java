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

package org.jetbrains.plugins.grails.lang.gsp.parsing.html.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.gsp.lexer.GspLexer;

/**
 * @author ilyas
 */
public class GspTemplateBlackAndWhiteLexer extends Lexer {
  private final GspLexer myGspLexer = new GspLexer();
  private final Lexer myTemplateLexer;
  private int myTemplateState = 0;

  public GspTemplateBlackAndWhiteLexer(Lexer templateLexer) {
    myTemplateLexer = templateLexer;
  }

  public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    myGspLexer.start(buffer, startOffset, endOffset, initialState);
    setupTemplateToken();
  }

  public CharSequence getBufferSequence() {
    return myGspLexer.getBufferSequence();
  }

  public int getState() {
    return myGspLexer.getState();
  }

  @Nullable
  public IElementType getTokenType() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (tokenType == null) return null;

    if (tokenType == GspTokenTypesEx.GSP_TEMPLATE_DATA ||
            tokenType == XmlTokenType.XML_WHITE_SPACE) {
      return GspTokenTypesEx.GSP_TEMPLATE_DATA;
    } else {
      return GspTokenTypesEx.GSP_FRAGMENT_IN_HTML;
    }
  }

  public int getTokenStart() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (tokenType == GspTokenTypesEx.GSP_TEMPLATE_DATA ||
            tokenType == XmlTokenType.XML_WHITE_SPACE) {
      return myTemplateLexer.getTokenStart();
    } else {
      return myGspLexer.getTokenStart();
    }
  }

  public int getTokenEnd() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (tokenType == GspTokenTypesEx.GSP_TEMPLATE_DATA ||
            tokenType == XmlTokenType.XML_WHITE_SPACE) {
      return myTemplateLexer.getTokenEnd();
    } else {
      return myGspLexer.getTokenEnd();
    }
  }

  public void advance() {
    IElementType tokenType = myGspLexer.getTokenType();
    if (tokenType == GspTokenTypesEx.GSP_TEMPLATE_DATA ||
            tokenType == XmlTokenType.XML_WHITE_SPACE) {
      myTemplateLexer.advance();
      myTemplateState = myTemplateLexer.getState();
      if (myTemplateLexer.getTokenType() != null) return;
    }
    myGspLexer.advance();
    setupTemplateToken();
  }

  private void setupTemplateToken() {
    while (true) {
      IElementType tokenType = myGspLexer.getTokenType();
      if (tokenType != GspTokenTypesEx.GSP_TEMPLATE_DATA &&
              tokenType != XmlTokenType.XML_WHITE_SPACE) {
        return;
      }

      myTemplateLexer.start(myGspLexer.getBufferSequence(), myGspLexer.getTokenStart(), myGspLexer.getTokenEnd(), myTemplateState);
      if (myTemplateLexer.getTokenType() != null) return;
      myGspLexer.advance();
    }
  }


  private static class Position implements LexerPosition {
    private final LexerPosition myTemplatePosition;
    private final LexerPosition myGspPosition;

    public Position(LexerPosition templatePosition, LexerPosition jspPosition) {
      myTemplatePosition = templatePosition;
      myGspPosition = jspPosition;
    }

    public int getOffset() {
      return Math.max(myGspPosition.getOffset(), myTemplatePosition.getOffset());
    }

    public LexerPosition getTemplatePosition() {
      return myTemplatePosition;
    }

    public LexerPosition getGspPosition() {
      return myGspPosition;
    }

    public int getState() {
      throw new UnsupportedOperationException("Method getState is not yet implemented in " + getClass().getName());
    }
  }

  public LexerPosition getCurrentPosition() {
    return new Position(myTemplateLexer.getCurrentPosition(), myGspLexer.getCurrentPosition());
  }

  public void restore(LexerPosition position) {
    final Position p = (Position) position;
    myGspLexer.restore(p.getGspPosition());
    final LexerPosition templatePos = p.getTemplatePosition();
    if (templatePos != null && templatePos.getOffset() < myTemplateLexer.getBufferEnd()) {
      myTemplateLexer.restore(templatePos);
    } else {
      setupTemplateToken();
    }
  }

  public int getBufferEnd() {
    return myGspLexer.getBufferEnd();
  }

}
