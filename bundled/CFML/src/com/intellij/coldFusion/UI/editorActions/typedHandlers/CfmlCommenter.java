package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.lang.Commenter;

/**
 * Created by Lera Nikolaenko
 * Date: 28.10.2008
 */
public class CfmlCommenter implements Commenter {

  public String getLineCommentPrefix() {
    return null;
  }

  public String getBlockCommentPrefix() {
    return "<!---";
  }

  public String getBlockCommentSuffix() {
    return "--->";
  }

  public String getCommentedBlockCommentPrefix() {
    return "&lt;!&mdash;";
  }

  public String getCommentedBlockCommentSuffix() {
    return "&mdash;&gt;";
  }
}
