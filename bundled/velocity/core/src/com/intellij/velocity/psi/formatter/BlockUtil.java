package com.intellij.velocity.psi.formatter;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 *         Date: Jul 3, 2009
 *         Time: 2:47:10 PM
 */
class BlockUtil {
  private BlockUtil() {
  }

  public static List<Block> buildWrappers(@NotNull final List<Block> blocks) {
    if (blocks.size() == 0) return Collections.emptyList();
    ArrayList<Block> result = new ArrayList<Block>(blocks.size());
    for (Block block : blocks) {
      createAndAddBlock(result, block, null);
    }
    return result;
  }

  public static Pair<List<Block>, List<Block>> splitBlocksByRightBound(@NotNull Block parent, @NotNull TextRange bounds) {
    final List<Block> subBlocks = parent.getSubBlocks();
    if (subBlocks.size() == 0) return new Pair<List<Block>, List<Block>>(Collections.<Block>emptyList(), Collections.<Block>emptyList());
    final ArrayList<Block> before = new ArrayList<Block>(subBlocks.size() / 2);
    final ArrayList<Block> after = new ArrayList<Block>(subBlocks.size() / 2);
    splitByRightBoundAndCollectBlocks(subBlocks, before, after, bounds);
    return new Pair<List<Block>, List<Block>>(before, after);
  }

  private static void splitByRightBoundAndCollectBlocks(@NotNull List<Block> blocks,
                                                        @NotNull List<Block> before,
                                                        @NotNull List<Block> after,
                                                        @NotNull TextRange bounds) {
    for (Block block : blocks) {
      final TextRange textRange = block.getTextRange();
      if (bounds.contains(textRange)) {
        createAndAddBlock(before, block, null);
      }
      else if (bounds.getEndOffset() < textRange.getStartOffset()) {
        createAndAddBlock(after, block, null);
      }
      else {
        splitByRightBoundAndCollectBlocks(block.getSubBlocks(), before, after, bounds);
      }
    }
  }

  private static void createAndAddBlock(List<Block> list, Block block, @Nullable final Indent indent) {
    Block wrapper = DataLanguageBlockWrapper.create(block, indent);
    if (wrapper != null) {
      list.add(wrapper);
    }
  }


  public static List<Block> mergeBlocks(@NotNull List<Block> vtlBlocks, @NotNull List<Block> foreignBlocks) {
    ArrayList<Block> result = new ArrayList<Block>(vtlBlocks.size() + foreignBlocks.size());
    int vInd = 0;
    int fInd = 0;
    while (vInd < vtlBlocks.size() && fInd < foreignBlocks.size()) {
      final VtlBlock v = (VtlBlock)vtlBlocks.get(vInd);
      final DataLanguageBlockWrapper f = (DataLanguageBlockWrapper)foreignBlocks.get(fInd);
      final TextRange vRange = v.getTextRange();
      final TextRange fRange = f.getTextRange();
      if (vRange.getStartOffset() >= fRange.getEndOffset()) {
        // add leading foreign blocks
        result.add(f);
        fInd++;
      }
      else if (vRange.getEndOffset() <= fRange.getStartOffset()) {
        // add leading vtl blocks
        result.add(v);
        vInd++;
      }
      else if (vRange.getStartOffset() < fRange.getStartOffset() ||
               vRange.getStartOffset() == fRange.getStartOffset() && vRange.getEndOffset() >= fRange.getEndOffset()) {
        // add including vtl blocks and split intersecting foreign blocks
        result.add(v);
        while (fInd < foreignBlocks.size() && vRange.contains(foreignBlocks.get(fInd).getTextRange())) {
          v.addForeignChild(foreignBlocks.get(fInd++));
        }
        if (fInd < foreignBlocks.size()) {
          final DataLanguageBlockWrapper notContainedF = (DataLanguageBlockWrapper)foreignBlocks.get(fInd);
          if (vRange.intersectsStrict(notContainedF.getTextRange())) {
            Pair<List<Block>, List<Block>> splitBlocks = BlockUtil.splitBlocksByRightBound(notContainedF.getOriginal(), vRange);
            v.addForeignChildren(splitBlocks.getFirst());
            foreignBlocks.remove(fInd);
            if (splitBlocks.getSecond().size() > 0) {
              foreignBlocks.addAll(fInd, splitBlocks.getSecond());
            }
          }
        }
        vInd++;
      }
      else if (vRange.getStartOffset() > fRange.getStartOffset() ||
               vRange.getStartOffset() == fRange.getStartOffset() && vRange.getEndOffset() < fRange.getEndOffset()) {
        // add including foreign blocks or split them if needed
        int lastContainedVtlInd = vInd;
        while (lastContainedVtlInd < vtlBlocks.size() && fRange.intersectsStrict(vtlBlocks.get(lastContainedVtlInd).getTextRange())) {
          lastContainedVtlInd++;
        }
        if (fRange.contains(vtlBlocks.get(lastContainedVtlInd - 1).getTextRange())) {
          result.add(f);
          fInd++;
          while (vInd < lastContainedVtlInd) {
            f.addVtlChild((VtlBlock)vtlBlocks.get(vInd++));
          }
        }
        else {
          foreignBlocks.remove(fInd);
          foreignBlocks.addAll(fInd, buildWrappers(f.getOriginal().getSubBlocks()));
        }
      }
    }
    while (vInd < vtlBlocks.size()) {
      result.add(vtlBlocks.get(vInd++));
    }
    while (fInd < foreignBlocks.size()) {
      result.add(foreignBlocks.get(fInd++));
    }
    return result;
  }

  @NotNull
  public static List<Block> filterBlocksByRange(@NotNull List<Block> list, @NotNull TextRange textRange) {
    int i = 0;
    while (i < list.size()) {
      final DataLanguageBlockWrapper block = (DataLanguageBlockWrapper)list.get(i);
      final TextRange range = block.getTextRange();
      if (textRange.contains(range)) {
        i++;
      }
      else if (range.intersectsStrict(textRange)) {
        list.remove(i);
        list.addAll(i, buildWrappers(block.getOriginal().getSubBlocks()));
      }
      else {
        list.remove(i);
      }
    }
    return list;
  }

  static List<Block> splitBlockIntoFragments(@NotNull Block block, @NotNull List<Block> subBlocks) {
    final List<Block> children = new ArrayList<Block>(5);
    final TextRange range = block.getTextRange();
    int childStartOffset = range.getStartOffset();
    for (Block vtlBlock : subBlocks) {
      final TextRange vtlTextRange = vtlBlock.getTextRange();
      if (vtlTextRange.getStartOffset() > childStartOffset) {
        children.add(new DataLanguageBlockFragmentWrapper(block, new TextRange(childStartOffset, vtlTextRange.getStartOffset())));
      }
      children.add(vtlBlock);
      childStartOffset = vtlTextRange.getEndOffset();
    }
    if (range.getEndOffset() > childStartOffset) {
      children.add(new DataLanguageBlockFragmentWrapper(block, new TextRange(childStartOffset, range.getEndOffset())));
    }
    return children;
  }

  static void printBlocks(@Nullable TextRange textRange, @NotNull List<Block> list) {
    StringBuilder sb = new StringBuilder(String.valueOf(textRange)).append(": ");
    for (Block block : list) {
      ASTNode node = block instanceof ASTBlock ? ((ASTBlock)block).getNode() : null;
      TextRange r = block.getTextRange();
      sb.append(" [").append(node != null ? node.getElementType() : null)//.append(" ").append(((BlockWithParent)block).getParent() != null)
          .append(r).append(block.getIndent()).append(block.getAlignment()).append("] ");
    }
    System.out.println(sb);
  }

  static List<Block> setParent(List<Block> children, BlockWithParent parent) {
    for (Block block : children) {
      if (block instanceof BlockWithParent) ((BlockWithParent)block).setParent(parent);
    }
    return children;
  }
}
