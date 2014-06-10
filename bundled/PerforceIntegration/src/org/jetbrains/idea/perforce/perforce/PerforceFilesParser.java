package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class PerforceFilesParser {
  private final List<String> myLines;
  private final PerforceClient myClient;
  private final List<FilePath> myPaths;

  public PerforceFilesParser(List<String> lines, PerforceClient client) {
    myLines = lines;
    myClient = client;
    myPaths = new LinkedList<FilePath>();
  }

  public void go() throws VcsException {
    for (String line : myLines) {
      final File file = PerforceManager.getFileByDepotName(line, myClient);
      if (file != null) {
        myPaths.add(FilePathImpl.createForDeletedFile(file, false));
      }
    }
  }

  public List<FilePath> getPaths() {
    return myPaths;
  }
}
