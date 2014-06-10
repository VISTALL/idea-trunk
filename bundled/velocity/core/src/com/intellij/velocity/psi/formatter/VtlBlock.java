package com.intellij.velocity.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.psi.VtlCompositeElementTypes;
import com.intellij.velocity.psi.VtlDirectiveType;
import com.intellij.velocity.psi.VtlElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Chmutov
 *         Date: Jun 26, 2009
 *         Time: 4:05:40 PM
 */
public class VtlBlock extends AbstractBlock implements BlockWithParent {
  private List<Block> myForeignChildren;
  private boolean myChildrenBuilt = false;
  private BlockWithParent myParent;

  public VtlBlock(@NotNull ASTNode node, @NotNull List<Block> foreignChildren) {
    super(node, null, null);
    myForeignChildren = foreignChildren;
  }

  VtlBlock(@NotNull ASTNode node, @NotNull BlockWithParent parent) {
    super(node, null, null);
    myParent = parent;
  }

  protected List<Block> buildChildren() {
    myChildrenBuilt = true;
    if (isLeaf()) {
      return EMPTY;
    }
    final ArrayList<Block> vtlChildren = new ArrayList<Block>(5);
    for (ASTNode childNode = getNode().getFirstChildNode(); childNode != null; childNode = childNode.getTreeNext()) {
      if (FormatterUtil.containsWhiteSpacesOnly(childNode)) continue;
      if (childNode.getElementType() != VtlElementTypes.TEMPLATE_TEXT || noForeignChildren()) {
        final VtlBlock childBlock = new VtlBlock(childNode, this);
        vtlChildren.add(childBlock);
      }
    }
    final List<Block> children = myForeignChildren == null ? vtlChildren : BlockUtil.mergeBlocks(vtlChildren, myForeignChildren);
    //BlockUtil.printBlocks(getTextRange(), children);
    return BlockUtil.setParent(children, this);
  }

  private boolean noForeignChildren() {
    return (myForeignChildren == null || myForeignChildren.isEmpty());
  }

  void addForeignChild(@NotNull Block foreignChild) {
    initForeignChildren();
    myForeignChildren.add(foreignChild);
  }

  void addForeignChildren(List<Block> foreignChildren) {
    initForeignChildren();
    myForeignChildren.addAll(foreignChildren);
  }

  private void initForeignChildren() {
    assert !myChildrenBuilt;
    if (myForeignChildren == null) {
      myForeignChildren = new ArrayList<Block>(5);
    }
  }


  public Spacing getSpacing(final Block child1, final Block child2) {
    return null;
  }

  public boolean isLeaf() {
    return noForeignChildren() && getNode().getFirstChildNode() == null;
  }

  @Override
  public Indent getIndent() {
    boolean dontIndent = myParent == null || myParent.getParent() == null;
    return dontIndent ? Indent.getNoneIndent() : getIndent(getNode().getElementType());
  }

  @Override
  protected Indent getChildIndent() {
    return getNode().getTreeParent() == null ? Indent.getNoneIndent() : Indent.getNormalIndent();
  }

  static Indent getIndent(IElementType childType) {
    if (childType instanceof VtlDirectiveType &&
        childType != VtlCompositeElementTypes.DIRECTIVE_ELSEIF &&
        childType != VtlCompositeElementTypes.DIRECTIVE_ELSE ||
        childType == VtlCompositeElementTypes.DIRECTIVE_BREAK ||
        childType == VtlCompositeElementTypes.INTERPOLATION) {
      return Indent.getNormalIndent();
    }
    return Indent.getNoneIndent();
  }

  public BlockWithParent getParent() {
    return myParent;
  }

  public void setParent(BlockWithParent newParent) {
    myParent = newParent;
  }
}

