package org.jetbrains.idea.perforce.perforce.jobs;

import java.util.List;
import java.util.LinkedList;

public class StringCutter {
  private final int myPiece;
  private final String myIn;
  private String myResult;
  private int myNumLines;

  public StringCutter(final String in, final int piece) {
    myIn = in;
    myPiece = piece;
  }

  public String cutString() {
    final List<String> result = new LinkedList<String>();
    final String[] words = myIn.split(" ");

    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if ((sb.length() + word.length() + 1) >= myPiece) {
        if (sb.length() == 0) {
          sb.append(word);
          result.add(sb.toString());
          sb = new StringBuilder();
        } else {
          result.add(sb.toString());
          sb = new StringBuilder();
          sb.append(word);
        }
      } else {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(word);
      }
    }
    if (sb.length() > 0) {
      result.add(sb.toString());
    }

    sb = new StringBuilder();
    for (String sRes : result) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(sRes);
    }
    return sb.toString();
  }

  public String getResult() {
    return myResult;
  }

  public int getNumLines() {
    return myNumLines;
  }
}
