package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.changes.ChangeListEditHandler;

public class PerforceChangeListEditHandler implements ChangeListEditHandler {
  public String changeCommentOnChangeName(final String name, final String comment) {
    final int idx = comment.indexOf('\n');
    if (idx >= 0) {
      return name + comment.substring(idx);
    } else {
      return name;
    }
  }

  public String changeNameOnChangeComment(final String name, final String comment) {
    final int idx = comment.indexOf('\n');
    if (idx >= 0) {
      return comment.substring(0, idx);
    } else {
      return comment;
    }
  }

  public String correctCommentWhenInstalled(final String name, final String comment) {
    if (! comment.startsWith(name)) {
      return name + '\n' + comment;
    } else {
      return comment;
    }
  }
}
