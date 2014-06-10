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

package org.jetbrains.plugins.grails.lang.gsp.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.xml.AbstractXmlBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.formatter.processors.GspSpacingProcessor;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class GspBlock extends AbstractXmlBlock implements AbstractGspBlock, GspElementTypes {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.formatter.GspBlock");

  private final Indent myIndent;
  private final TextRange myTextRange;
  private final GspFormattingHelper myFormattingHelper;

  public GspBlock(ASTNode node,
                  Wrap wrap,
                  Alignment alignment,
                  XmlFormattingPolicy policy,
                  Indent indent,
                  TextRange textRange) {
    super(node, wrap, alignment, policy);
    myTextRange = textRange != null ? textRange : super.getTextRange();
    myIndent = indent;
    myFormattingHelper = new GspFormattingHelper(myXmlFormattingPolicy, myNode, myTextRange);
  }

  @NotNull
  public TextRange getTextRange() {
    return myTextRange;
  }

  protected List<Block> buildChildren() {
    if (myNode.getElementType() == GRAILS_TAG_ATTRIBUTE_VALUE
        || myNode.getElementType() == GSP_DIRECTIVE_ATTRIBUTE_VALUE
        || myNode.getElementType() == JSP_STYLE_COMMENT
        || myNode.getElementType() == GSP_STYLE_COMMENT) {
      return EMPTY;
    }

    if (myNode.getFirstChildNode() != null) {
      final ArrayList<Block> result = new ArrayList<Block>(5);
      ASTNode child = myNode.getFirstChildNode();
      while (child != null) {
        ASTNode newChild;
        if (!FormatterUtil.containsWhiteSpacesOnly(child) && child.getTextLength() > 0) {
          newChild = myFormattingHelper.processChild(result, child, getDefaultWrap(child), null, getChildDefaultIndent());
        } else {
          child = child.getTreeNext();
          continue;
        }
        if (newChild != null) {
          LOG.assertTrue(newChild.getTreeParent() == myNode);
        }
        child = newChild;
      }
      return result;
    } else {
      return EMPTY;
    }

  }

  public Spacing getSpacing(Block child1, Block child2) {
    if ((child1 instanceof AbstractGspBlock) && (child2 instanceof AbstractGspBlock)) {
      return GspSpacingProcessor.getSpacing(((AbstractGspBlock) child1), ((AbstractGspBlock) child2));
    }
    return null;
  }

  @Override
  @NotNull
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    ASTNode astNode = getNode();
    final PsiElement psiParent = astNode.getPsi();
    if (psiParent instanceof GspXmlRootTag) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }
    if (psiParent instanceof GspTag) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    return new ChildAttributes(Indent.getNoneIndent(), null);
  }

  public boolean insertLineBreakBeforeTag() {
    return false;
  }

  public boolean removeLineBreakBeforeTag() {
    return false;
  }

  protected Wrap getDefaultWrap(ASTNode node) {
    return null;
  }

  private Indent getChildDefaultIndent() {
    if (myNode.getElementType() == GSP_SCRIPTLET_TAG) {
      return Indent.getNoneIndent();
    } else {
      return Indent.getNoneIndent();
    }
  }

  public boolean isTextElement() {
    return false;
  }

  public Indent getIndent() {
    return myIndent;
  }

  public boolean isIncomplete() {
    return isIncomplete(myNode);
  }

  /**
   * @param node Tree node
   * @return true if node is incomplete
   */
  public boolean isIncomplete(@NotNull final ASTNode node) {
    ASTNode lastChild = node.getLastChildNode();
    while (lastChild != null &&
        (lastChild.getPsi() instanceof PsiWhiteSpace || lastChild.getPsi() instanceof PsiComment)) {
      lastChild = lastChild.getTreePrev();
    }
    return lastChild != null && (lastChild.getPsi() instanceof PsiErrorElement || isIncomplete(lastChild));
  }

  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }


}
