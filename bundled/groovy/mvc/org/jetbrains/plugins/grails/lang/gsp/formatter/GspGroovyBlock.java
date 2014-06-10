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

import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.formatter.java.ReadonlyWhitespaceBlock;
import com.intellij.psi.formatter.xml.AbstractXmlBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterGroovyElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class GspGroovyBlock extends AbstractXmlBlock implements AbstractGspBlock {

  @Nullable
  private final Block myBaseLanguageBlock;
  private final Indent myParentIndent;

  private boolean myHeadIncomplete = false;
  private boolean myTailIncomplete = false;

  public GspGroovyBlock(final ASTNode node,
                        final XmlFormattingPolicy policy,
                        Pair<PsiElement, Language> rootBlockInfo,
                        Indent indent
  ) {

    super(node, null, null, policy);
    myParentIndent = indent;
    myBaseLanguageBlock = policy.getOrCreateBlockFor(rootBlockInfo);
  }


  public boolean insertLineBreakBeforeTag() {
    return false;
  }

  public boolean removeLineBreakBeforeTag() {
    return false;
  }

  public boolean isTextElement() {
    return true;
  }

  protected List<Block> buildChildren() {
    if (myBaseLanguageBlock == null) {
      return new ArrayList<Block>();
    } else {
      final ArrayList<Block> result = new ArrayList<Block>();
      extractBlocks(myBaseLanguageBlock, myNode.getTextRange(), result);
      for (Block block : result) {
        if (block instanceof GspTextSyntheticBlock) {
          ((GspTextSyntheticBlock) block).setIndent(myParentIndent);
        }
      }
      return result;
    }
  }

  private boolean extractBlocks(final Block parentBlock,
                                final TextRange textRange,
                                final ArrayList<Block> result) {

    if (textRange.getStartOffset() >= textRange.getEndOffset()) return false;

    final TextRange blockRange = parentBlock.getTextRange();

    if (blockRange.getStartOffset() >= textRange.getStartOffset() && blockRange.getEndOffset() <= textRange.getEndOffset()) {
      result.add(parentBlock);
      return true;
    }

    if (blockRange.getEndOffset() < textRange.getStartOffset()) return false;

    if (blockRange.getStartOffset() >= textRange.getEndOffset()) return false;

    final List<Block> subBlocks = parentBlock.getSubBlocks();
    final ArrayList<Block> localResult = new ArrayList<Block>();
    int fromIndex = -1;
    for (int i = 0; i < subBlocks.size(); i++) {
      final Block block = subBlocks.get(i);

      final TextRange subRange = block.getTextRange();

      if (subRange.getStartOffset() < textRange.getStartOffset()
          && subRange.getEndOffset() > textRange.getStartOffset()
          && subRange.getEndOffset() <= textRange.getEndOffset()
          ) {
        myHeadIncomplete = true;
      }

      if (subRange.getStartOffset() >= textRange.getStartOffset()
          && subRange.getStartOffset() < textRange.getEndOffset()
          && subRange.getEndOffset() > textRange.getEndOffset()
          ) {
        myTailIncomplete = true;
      }

      boolean added = extractBlocks(block, new TextRange(Math.max(textRange.getStartOffset(), subRange.getStartOffset()),
          Math.min(textRange.getEndOffset(), subRange.getEndOffset())), localResult);
      if (fromIndex == -1 && added) {
        fromIndex = i;
      }
    }
    if (!localResult.isEmpty()) {
      final Indent parentIndent;
      final int firstBlockStartOffset = localResult.get(0).getTextRange().getStartOffset();
      final int lastBlockEndOffset = localResult.get(localResult.size() - 1).getTextRange().getEndOffset();
      if (firstBlockStartOffset > blockRange.getStartOffset() && lastBlockEndOffset < blockRange.getEndOffset()) {
        parentIndent = Indent.getNoneIndent();
      } else {
        parentIndent = parentBlock.getIndent();
      }

      if (false && localResult.size() == 1 && localResult.get(0) instanceof GspTextSyntheticBlock) {
        ((GspTextSyntheticBlock) localResult.get(0)).setIndent(Indent.getNoneIndent());
      }

      result.add(new GspTextSyntheticBlock(parentBlock,
          fromIndex,
          parentIndent,
          localResult));
      return true;

    } else {
      TextRange intersection = blockRange.intersection(textRange);
      if (intersection != null) {
        result.add(new ReadonlyWhitespaceBlock(
            intersection,
            null,
            null,
            Indent.getNoneIndent()
        ));
      }

      return true;
    }

  }

  public Spacing getSpacing(Block child1, Block child2) {
    if (myBaseLanguageBlock != null) {
      return myBaseLanguageBlock.getSpacing(child1, child2);
    } else {
      return null;
    }
  }

  @NotNull
  public TextRange getTextRange() {
    return super.getTextRange();
  }


  public Indent getIndent() {
    return Indent.getNoneIndent();
  }

  @Override
  @NotNull
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    if (newChildIndex > 0 && getSubBlocks().get(newChildIndex - 1) instanceof GspTextSyntheticBlock) {
      return ChildAttributes.DELEGATE_TO_PREV_CHILD;
    }

    if (newChildIndex == 0 && getSubBlocks().get(0) instanceof GspTextSyntheticBlock) {
      return ChildAttributes.DELEGATE_TO_NEXT_CHILD;
    }

    if (myBaseLanguageBlock != null) {
      return myBaseLanguageBlock.getChildAttributes(newChildIndex);
    } else {
      return new ChildAttributes(null, null);
    }
  }

  public String toString() {
    return myNode.getText();
  }

  public boolean isHeadIncomplete() {
    return myHeadIncomplete;
  }

  public boolean isTailIncomplete() {
    return myTailIncomplete;
  }

  public static Pair<PsiElement, Language> findPsiRootAt(final ASTNode child) {
    final FileViewProvider viewProvider = child.getPsi().getContainingFile().getViewProvider();
    final PsiFile file = viewProvider.getPsi(GspFileType.GSP_FILE_TYPE.getLanguage());
    if (!(file instanceof GspFile)) return null;
    final int startOffset = child.getTextRange().getStartOffset();
    PsiElement psi = child.getPsi();
    if (psi instanceof GspOuterGroovyElement) {
      Language groovyLanguage = GroovyFileType.GROOVY_FILE_TYPE.getLanguage();
      final PsiElement declElement = file.getViewProvider().findElementAt(startOffset, groovyLanguage);
      if (declElement == null) return null;
      return new Pair<PsiElement, Language>(TreeUtil.getFileElement((TreeElement) declElement.getNode()).getPsi(), groovyLanguage);
    }
    return null;
  }


}
