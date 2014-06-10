package com.intellij.velocity.editorActions;

import com.intellij.lang.Commenter;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 05.06.2008
 */
public class VtlCommenter implements Commenter {
    public String getLineCommentPrefix() {
        return "##";
    }

    public String getBlockCommentPrefix() {
        return "#*";
    }

    public String getBlockCommentSuffix() {
        return "*#";
    }

  public String getCommentedBlockCommentPrefix() {
    return null;
  }

  public String getCommentedBlockCommentSuffix() {
    return null;
  }
}
