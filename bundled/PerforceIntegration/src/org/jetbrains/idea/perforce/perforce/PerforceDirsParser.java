package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PerforceDirsParser {
  private final static String ourNoSuchFiles = "no such file(s).";

  private final List<String> myLines;
  // local -> remote
  private final Map<String, String> myChildren;
  private final List<String> myNotExists;
  private final PerforceClient myClient;

  public PerforceDirsParser(final List<String> lines, final PerforceClient client) {
    myLines = lines;
    myClient = client;
    myChildren = new LinkedHashMap<String, String>();
    myNotExists = new LinkedList<String>();
  }

  public void go() throws VcsException {
    for (String line : myLines) {
      final String trimmed = line.trim();
      // todo check that correct local root will be used (not 'c:/')
      if (trimmed.endsWith(ourNoSuchFiles)) {
        noSuchFile(trimmed);
      } else {
        final File file = PerforceManager.getFileByDepotName(trimmed, myClient);
        if (file != null) {
          myChildren.put(file.getAbsolutePath(), trimmed);
        }
      }
    }
  }

  private void noSuchFile(final String trimmed) throws VcsException {
    final int idx = trimmed.indexOf("/* ");
    if (idx > 0) {
      final File file = PerforceManager.getFileByDepotName(trimmed.substring(0, idx), myClient);
      if (file != null) {
        myNotExists.add(file.getAbsolutePath());
      }
    }
  }

  public Map<String, String> getChildren() {
    return myChildren;
  }

  public List<String> getNotExists() {
    return myNotExists;
  }
}
