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

package org.jetbrains.plugins.grails.lang.gsp.formatter;

import com.intellij.formatting.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author ilyas
 */
public class GspTextSyntheticBlock implements Block {
  private final Block myParentBlock;
  private Indent myIndent;
  private final List<Block> mySubBlocks;

  private final int myFromIndex;

  private final TextRange myTextRange;

  public GspTextSyntheticBlock(final Block parentBlock,
                               final int fromIndex, final Indent indent,
                               final List<Block> subBlocks) {
    myParentBlock = parentBlock;
    myIndent = indent;
    mySubBlocks = subBlocks;
    myFromIndex = fromIndex;

    myTextRange =
        new TextRange(subBlocks.get(0).getTextRange().getStartOffset(), subBlocks.get(subBlocks.size() - 1).getTextRange().getEndOffset());

  }

  @NotNull
  public TextRange getTextRange() {
    return myTextRange;
  }

  @NotNull
  public List<Block> getSubBlocks() {
    return mySubBlocks;
  }

  @Nullable
  public Wrap getWrap() {
    return null;
  }

  @Nullable
  public Indent getIndent() {
    return myIndent;
  }

  @Nullable
  public Alignment getAlignment() {
    return null;
  }

  @NotNull
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    if (newChildIndex > 0 && mySubBlocks.get(newChildIndex - 1) instanceof GspTextSyntheticBlock) {
      return ChildAttributes.DELEGATE_TO_PREV_CHILD;
    }

    if (newChildIndex == 0 && mySubBlocks.get(0) instanceof GspTextSyntheticBlock) {
      return ChildAttributes.DELEGATE_TO_NEXT_CHILD;
    }

    return myParentBlock.getChildAttributes(myFromIndex + newChildIndex);
  }

  public boolean isIncomplete() {
    return myParentBlock.isIncomplete();
  }

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.formatter.GspTextSyntheticBlock");

  @Nullable
  public Spacing getSpacing(Block child1, Block child2) {
    final Block first;
    final Block second;

    if (child1 instanceof GspTextSyntheticBlock) {
      first = ((GspTextSyntheticBlock) child1).myParentBlock;
    } else {
      first = child1;
    }

    if (child2 instanceof GspTextSyntheticBlock) {
      second = ((GspTextSyntheticBlock) child2).myParentBlock;
    } else {
      second = child2;
    }

    return myParentBlock.getSpacing(first, second);
  }

  public void setIndent(final Indent parentIndent) {
    myIndent = parentIndent;
  }

  public boolean isLeaf() {
    return false;
  }
}
