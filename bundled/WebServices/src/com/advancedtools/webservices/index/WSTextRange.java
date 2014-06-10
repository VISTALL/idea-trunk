package com.advancedtools.webservices.index;

import com.intellij.openapi.util.TextRange;

import java.io.Serializable;

/**
 * @by Konstantin Bulenkov
 */
public class WSTextRange implements Serializable {
  private final int myStartOffset;
  private final int myEndOffset;

  public WSTextRange(int startOffset, int endOffset) {
    myStartOffset = startOffset;
    myEndOffset = endOffset;
  }

  public WSTextRange(TextRange range) {
    myStartOffset = range.getStartOffset();
    myEndOffset = range.getEndOffset();
  }

  public final int getStartOffset() {
    return myStartOffset;
  }

  public final int getEndOffset() {
    return myEndOffset;
  }

  public final TextRange getTextRange() {
    return new TextRange(myStartOffset, myEndOffset);
  }
}
