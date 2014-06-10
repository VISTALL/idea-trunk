package org.jetbrains.idea.perforce.application;

import org.jetbrains.annotations.NotNull;

public class PerforceNameCommentConvertor {
  private final String myNativeDescription;
  private final String myIdeaName;
  private final String myIdeaComment;

  private PerforceNameCommentConvertor(@NotNull final String nativeDescription, @NotNull final String ideaName, @NotNull final String ideaComment) {
    myNativeDescription = nativeDescription;
    myIdeaName = ideaName;
    myIdeaComment = ideaComment;
  }

  public static PerforceNameCommentConvertor fromNative(@NotNull final String nativeDescription) {
    final String trimmed = nativeDescription.trim();
    int pos = trimmed.indexOf("\n");
    final String name;
    final String comment;
    if (pos >= 0) {
      name = trimmed.substring(0, pos).trim() + "...";
      comment = trimmed;
    }
    else {
      name = trimmed;
      comment = "";
    }

    return new PerforceNameCommentConvertor(nativeDescription, name, comment);
  }

  /*public static PerforceNameCommentConvertor fromIdea(@NotNull final String name, @NotNull final String comment) {

  }*/

  public String getNativeDescription() {
    return myNativeDescription;
  }

  public String getIdeaName() {
    return myIdeaName;
  }

  public String getIdeaComment() {
    return myIdeaComment;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PerforceNameCommentConvertor convertor = (PerforceNameCommentConvertor)o;

    if (!myIdeaComment.equals(convertor.myIdeaComment)) return false;
    if (!myIdeaName.equals(convertor.myIdeaName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myIdeaName.hashCode();
    result = 31 * result + myIdeaComment.hashCode();
    return result;
  }
}
