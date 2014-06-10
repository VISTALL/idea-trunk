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

package org.jetbrains.plugins.grails.lang.gsp.parsing.gsp.chameleons;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerUtil;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.CustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspDirectiveFlexLexer;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.directive.GspDirectiveImpl;

/**
 * @author ilyas
 */
public class GspDirectiveElement extends CustomParsingType implements GspTokenTypes, GspElementTypes {
  public GspDirectiveElement(String debugName) {
    super(debugName, GspFileType.GSP_FILE_TYPE.getLanguage());
  }

  public ASTNode parse(CharSequence text, CharTable table) {
    CompositeElement root = new GspDirectiveImpl();
    final Lexer lexer = new GspDirectiveFlexLexer() {
      public IElementType getTokenType() {
        IElementType type = super.getTokenType();
        if (type == XML_TAG_NAME) return XML_NAME;
        return type;
      }
    };

    lexer.start(text);

    parseDirective(lexer, root, table);
    if (lexer.getTokenType() != null) {
      final CompositeElement errorElement = addErrorElement(root, GrailsBundle.message("gsp.unparseable.content"));
      while (lexer.getTokenType() != null) {
        addAndAdvance(errorElement, lexer, table);
      }
    }
    return root;
  }


  private static void parseDirective(final Lexer lexer, final CompositeElement treeElement, CharTable table) {
    if (lexer.getTokenType() == GDIRECT_BEGIN || lexer.getTokenType() == JDIRECT_BEGIN) {
      addAndAdvance(treeElement, lexer, table);
    }
    else {
      return;
    }
    if (lexer.getTokenType() == XML_WHITE_SPACE) addAndAdvance(treeElement, lexer, table);
    if (lexer.getTokenType() != XML_NAME) return;
    addAndAdvance(treeElement, lexer, table);
    while (lexer.getTokenType() == XML_WHITE_SPACE || lexer.getTokenType() == XML_NAME) {
      if (lexer.getTokenType() == XML_WHITE_SPACE) addAndAdvance(treeElement, lexer, table);
      if (lexer.getTokenType() == XML_NAME) {
        final CompositeElement attribute = ASTFactory.composite(GSP_DIRECTIVE_ATTRIBUTE);
        treeElement.rawAddChildren(attribute);
        parseAttribute(lexer, attribute, table);
      }
    }
    if (lexer.getTokenType() == GDIRECT_END || lexer.getTokenType() == JDIRECT_END) {
      addAndAdvance(treeElement, lexer, table);
    }
  }

  private static void parseAttribute(final Lexer lexer, final CompositeElement attribute, final CharTable table) {
    addAndAdvance(attribute, lexer, table);
    if (lexer.getTokenType() == XML_WHITE_SPACE) addAndAdvance(attribute, lexer, table);
    if (lexer.getTokenType() != XML_EQ) {
      addErrorElement(attribute, GrailsBundle.message("expected.attribute.eq.sign"));

      return;
    }
    addAndAdvance(attribute, lexer, table);
    if (lexer.getTokenType() == XML_WHITE_SPACE) addAndAdvance(attribute, lexer, table);
    if (lexer.getTokenType() != XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      addErrorElement(attribute, GrailsBundle.message("attribute.value.expected"));
      return;
    }

    final CompositeElement attributeValue = ASTFactory.composite(GSP_DIRECTIVE_ATTRIBUTE_VALUE);
    attribute.rawAddChildren(attributeValue);
    addAndAdvance(attributeValue, lexer, table);
    if (lexer.getTokenType() == XML_ATTRIBUTE_VALUE_TOKEN) addAndAdvance(attributeValue, lexer, table);
    if (lexer.getTokenType() != XML_ATTRIBUTE_VALUE_END_DELIMITER) {
      addErrorElement(attributeValue, GrailsBundle.message("quote.expected"));
      return;
    }
    addAndAdvance(attributeValue, lexer, table);
  }

  private static void addAndAdvance(final CompositeElement attribute, final Lexer lexer, final CharTable table) {
    attribute.rawAddChildren(createTokenElement(lexer, table));
    lexer.advance();
  }

  private static CompositeElement addErrorElement(final CompositeElement treeElement, String message) {
    final CompositeElement errorElement = Factory.createErrorElement(message);
    treeElement.rawAddChildren(errorElement);
    return errorElement;
  }

  @Nullable
  private static TreeElement createTokenElement(Lexer lexer, CharTable table) {
    IElementType tokenType = lexer.getTokenType();
    if (tokenType == null) return null;

    if (tokenType instanceof ILazyParseableElementType) {
      return ASTFactory.lazy((ILazyParseableElementType)tokenType, LexerUtil.internToken(lexer, table));
    }

    return ASTFactory.leaf(tokenType, LexerUtil.internToken(lexer, table));
  }


}
