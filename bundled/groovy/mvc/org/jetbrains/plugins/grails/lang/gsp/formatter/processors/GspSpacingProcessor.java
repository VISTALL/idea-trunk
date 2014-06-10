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

package org.jetbrains.plugins.grails.lang.gsp.formatter.processors;

import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.plugins.grails.lang.gsp.formatter.AbstractGspBlock;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspExpressionTag;

/**
 * @author ilyas
 */
public abstract class GspSpacingProcessor implements GspTokenTypesEx {

  private static final Spacing NO_SPACING_WITH_NEWLINE = Spacing.createSpacing(0, 0, 0, true, 1);
  private static final Spacing NO_SPACING = Spacing.createSpacing(0, 0, 0, false, 0);
  private static final Spacing COMMON_SPACING = Spacing.createSpacing(1, 1, 0, true, 100);

  private static final TokenSet GSP_GROOVY_EXPR_SEPARATORS = TokenSet.create(
          JEXPR_BEGIN,
          GEXPR_BEGIN,
          GEXPR_END
  );


  public static Spacing getSpacing(AbstractGspBlock child1, AbstractGspBlock child2) {

    ASTNode leftNode = child1.getNode();
    ASTNode rightNode = child2.getNode();

    IElementType lt = leftNode.getElementType();
    IElementType rt = rightNode.getElementType();

    if (lt == XML_TAG_END || rt == XML_END_TAG_START) {
      return NO_SPACING_WITH_NEWLINE;
    }

    if (rt == XML_TAG_END) return NO_SPACING;
    if (rt == XML_EMPTY_ELEMENT_END) return NO_SPACING;

    if (rt == XML_BAD_CHARACTER || lt == XML_BAD_CHARACTER) {
      return null;
    }

    if (leftNode.getPsi() instanceof XmlAttribute || rightNode.getPsi() instanceof XmlAttribute) {
      return COMMON_SPACING;
    }
    if (XML_EQ == lt || XML_EQ == rt) return NO_SPACING;

    if (GSP_GROOVY_EXPR_SEPARATORS.contains(lt) ||
            GSP_GROOVY_EXPR_SEPARATORS.contains(rt)) {
      return NO_SPACING_WITH_NEWLINE;
    }
    if (rt == JEXPR_END && rightNode.getTreeParent().getPsi() instanceof GspExpressionTag) {
      return NO_SPACING_WITH_NEWLINE;
    }

    if (GSP_GROOVY_SEPARATORS.contains(lt) && rightNode.getText().trim().length() > 0 ||
            GSP_GROOVY_SEPARATORS.contains(rt) && leftNode.getText().trim().length() > 0) {
      return COMMON_SPACING;
    }


    return null;
  }


}
