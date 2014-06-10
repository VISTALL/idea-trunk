package com.intellij.velocity.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Chmutov
 *         Date: Jun 30, 2009
 *         Time: 7:18:37 PM
 */
public class DataLanguageBlockWrapper implements ASTBlock, BlockWithParent {
  private final Block myOriginal;
  private final Indent myIndent;
  private List<Block> myBlocks;
  private List<Block> myVtlBlocks;
  private BlockWithParent myParent;

  private DataLanguageBlockWrapper(@NotNull final Block original, @Nullable final Indent indent) {
    assert !(original instanceof DataLanguageBlockWrapper) && !(original instanceof VtlBlock);
    myOriginal = original;
    myIndent = indent;
  }

  @NotNull
  public TextRange getTextRange() {
    return myOriginal.getTextRange();
  }

  @NotNull
  public List<Block> getSubBlocks() {
    if (myBlocks == null) {
      myBlocks = buildBlocks();
    }
    return myBlocks;
  }

  private List<Block> buildBlocks() {
    assert myBlocks == null;
    if (isLeaf()) {
      return AbstractBlock.EMPTY;
    }
    final List<Block> originalSubBlocks = myOriginal.getSubBlocks();
    final List<Block> subBlocks = BlockUtil.buildWrappers(originalSubBlocks);
    final List<Block> children;
    if (myVtlBlocks == null) {
      children = subBlocks;
    }
    else if (subBlocks.size() == 0) {
      children = originalSubBlocks.size() > 0 ? myVtlBlocks : BlockUtil.splitBlockIntoFragments(myOriginal, myVtlBlocks);
    }
    else {
      children = BlockUtil.mergeBlocks(myVtlBlocks, subBlocks);
    }
    //BlockUtil.printBlocks(getTextRange(), children);
    return BlockUtil.setParent(children, this);
  }

  public Wrap getWrap() {
    return myOriginal.getWrap();
  }

  @NotNull
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    return myOriginal.getChildAttributes(newChildIndex);
  }

  public Indent getIndent() {
    return myOriginal.getIndent();
  }

  public Alignment getAlignment() {
    return myOriginal.getAlignment();
  }

  @Nullable
  public Spacing getSpacing(Block child1, Block child2) {
    if (child1 instanceof DataLanguageBlockWrapper && child2 instanceof DataLanguageBlockWrapper) {
      return myOriginal.getSpacing(((DataLanguageBlockWrapper)child1).myOriginal, ((DataLanguageBlockWrapper)child2).myOriginal);
    }
    return null;
  }

  public boolean isIncomplete() {
    return myOriginal.isIncomplete();
  }

  public boolean isLeaf() {
    return myVtlBlocks == null && myOriginal.isLeaf();
  }

  void addVtlChild(VtlBlock vtlBlock) {
    assert myBlocks == null;
    if (myVtlBlocks == null) {
      myVtlBlocks = new ArrayList<Block>(5);
    }
    myVtlBlocks.add(vtlBlock);
    vtlBlock.setParent(this);
  }

  Block getOriginal() {
    return myOriginal;
  }

  @Override
  public String toString() {
    String vtlBlocksInfo = " VtlBlocks " + (myVtlBlocks == null ? "0" : myVtlBlocks.size()) + "|" + getTextRange() + "|";
    return vtlBlocksInfo + myOriginal.toString();
  }

  public static Block create(@NotNull final Block original, @Nullable final Indent indent) {
    final boolean doesntNeedWrapper = original instanceof ASTBlock && ((ASTBlock)original).getNode() instanceof OuterLanguageElement;
    return doesntNeedWrapper ? null : new DataLanguageBlockWrapper(original, indent);
  }

  public ASTNode getNode() {
    return myOriginal instanceof ASTBlock ? ((ASTBlock)myOriginal).getNode() : null;
  }

  public BlockWithParent getParent() {
    return myParent;
  }

  public void setParent(BlockWithParent parent) {
    myParent = parent;
  }
}
