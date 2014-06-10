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

package org.jetbrains.plugins.grails.lang.gsp.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class GspFoldingBuilder implements FoldingBuilder, GspElementTypes, DumbAware {

  private static final TokenSet GSP_TAGS = TokenSet.create(GSP_SCRIPTLET_TAG,
          GSP_DIRECTIVE,
          GSP_EXPR_TAG,
          GSP_DECLARATION_TAG);

  @NotNull
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    node.getPsi().getChildren();
    List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
    appendGspDescriptors(node, descriptors);
    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  private void appendGspDescriptors(ASTNode node, List<FoldingDescriptor> descriptors) {
    // comments
    IElementType elementType = node.getElementType();
    if ((elementType == GSP_STYLE_COMMENT ||
            elementType == JSP_STYLE_COMMENT) &&
            isMultiline(node)) {
      descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
    }
    if (elementType == GRAILS_TAG) {
      appendGrailsTagDescriptors(node, descriptors);
    }
    if (GSP_DIRECTIVE != elementType &&
            GSP_TAGS.contains(elementType) &&
            isMultiline(node)) {
      descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
    }

    ASTNode child = node.getFirstChildNode();
    while (child != null) {
      appendGspDescriptors(child, descriptors);
      child = child.getTreeNext();
    }

  }

  private void appendGrailsTagDescriptors(ASTNode node, List<FoldingDescriptor> descriptors) {
    PsiElement element = node.getPsi();
    if (!(element instanceof GspGrailsTag)) return;
    GspGrailsTag tag = (GspGrailsTag) element;
    if (!isMultiline(node) || tag.endsByError()) return;
    int tagEndOffset = tag.isEmpty() ? tag.getTextRange().getEndOffset() - 2 : tag.getTextRange().getEndOffset() - 1;

    if (!tag.isValid()) return;
    XmlAttribute[] attributes = tag.getAttributes();
    if (attributes != null && attributes.length > 0) {
      int listEndOffset = attributes[0].getTextRange().getEndOffset();
      if (listEndOffset <= tagEndOffset - 1) {
        TextRange range = new TextRange(listEndOffset, tagEndOffset);
        descriptors.add(new FoldingDescriptor(node, range));
      }
      return;
    }

    PsiElement identifier = tag.getNameElement();
    if (identifier != null) {
      int idOffset = identifier.getTextRange().getEndOffset();
      if (idOffset < tagEndOffset - 1) {
        TextRange range = new TextRange(idOffset, tagEndOffset);
        descriptors.add(new FoldingDescriptor(node, range));
      }
    }

  }

  private static boolean isMultiline(ASTNode node) {
    return (node.getText().contains("\n") || node.getText().contains("\t"));
  }


  public String getPlaceholderText(@NotNull ASTNode node) {

    final IElementType elemType = node.getElementType();
    if (elemType == JSP_STYLE_COMMENT) return "<%--...--%>";
    if (elemType == GSP_STYLE_COMMENT) return "%{--...--}%";
    if (GRAILS_TAG.equals(elemType)) return "...";
    if (GSP_TAGS.contains(elemType)) {
      ASTNode childNode = node.getFirstChildNode();
      assert childNode != null;
      if (childNode.getElementType() == GSCRIPT_BEGIN) return "%{...}%";
      if (childNode.getElementType() == GEXPR_BEGIN) return "${...}";
      if (childNode.getElementType() == GDECLAR_BEGIN) return "!{...}!";
      if (childNode.getElementType() == GDIRECT_BEGIN) return "@{...}";
      if (childNode.getElementType() == JSCRIPT_BEGIN) return "<%...%>";
      if (childNode.getElementType() == JEXPR_BEGIN) return "<%=...%>";
      if (childNode.getElementType() == JDECLAR_BEGIN) return "<%!...%>";
      if (childNode.getElementType() == JDIRECT_BEGIN) return "<%@...%>";
      else return null;
    }

    PsiElement psi = node.getPsi();
    if (psi instanceof XmlComment) return "...";

    // Hack for some intentions
    if (psi instanceof XmlTag) return "...";

    return null;
  }

  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }


}
