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

package com.intellij.velocity.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Alexey Chmutov
 *         Date: Jun 30, 2009
 *         Time: 7:18:37 PM
 */
public class DataLanguageBlockFragmentWrapper implements Block {
  private final Block myOwner;
  private final TextRange myRange;

  public DataLanguageBlockFragmentWrapper(@NotNull final Block owner, @NotNull final TextRange range) {
    myOwner = owner;
    myRange = range;
  }

  @NotNull
  public TextRange getTextRange() {
    return myRange;
  }

  @NotNull
  public List<Block> getSubBlocks() {
    return AbstractBlock.EMPTY;
  }

  public Wrap getWrap() {
    return myOwner.getWrap();
  }

  public Indent getIndent() {
    return myOwner.getIndent();
  }

  public Alignment getAlignment() {
    return myOwner.getAlignment();
  }

  @Nullable
  public Spacing getSpacing(Block child1, Block child2) {
    return Spacing.getReadOnlySpacing();
  }

  @NotNull
  public ChildAttributes getChildAttributes(int newChildIndex) {
    return myOwner.getChildAttributes(newChildIndex);
  }

  public boolean isIncomplete() {
    return myOwner.isIncomplete();
  }

  public boolean isLeaf() {
    return true;
  }

  @Override
  public String toString() {
    return "Fragment " + getTextRange();
  }
}
