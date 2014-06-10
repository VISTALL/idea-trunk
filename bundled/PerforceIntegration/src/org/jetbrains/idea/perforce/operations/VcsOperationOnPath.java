package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.VcsContextFactory;

import java.io.File;

/**
 * @author yole
 */
public abstract class VcsOperationOnPath extends VcsOperation {
  protected String myPath;

  protected VcsOperationOnPath() {
  }

  protected VcsOperationOnPath(final String changeList, final String path) {
    super(changeList);
    myPath = path;
  }

  public String getPath() {
    return myPath;
  }

  public void setPath(final String path) {
    myPath = path;
  }

  protected FilePath getFilePath() {
    return VcsContextFactory.SERVICE.getInstance().createFilePathOn(new File(myPath), false);
  }
}