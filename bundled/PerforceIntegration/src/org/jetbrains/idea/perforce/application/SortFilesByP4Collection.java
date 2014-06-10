package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SortFilesByP4Collection {
  private final Map<ConnectionId, Collection<FilePath>> mySortedFiles;
  private final Map<ConnectionId, P4Connection> myConnectionShortcut;
  private final Collection<FilePath> myExternal;
  private PerforceConnectionManager myConnectionManager;

  public SortFilesByP4Collection(final Project project) {
    mySortedFiles = new HashMap<ConnectionId, Collection<FilePath>>();
    myConnectionShortcut = new HashMap<ConnectionId, P4Connection>();
    myExternal = new LinkedList<FilePath>();
    myConnectionManager = PerforceConnectionManager.getInstance(project);
  }

  public void sort(final Collection<FilePath> paths) {
    for (FilePath path : paths) {
      final VirtualFile vf = path.getVirtualFile();
      final P4Connection connection;
      if (vf != null) {
        connection = myConnectionManager.getConnectionForFile(vf);
      } else {
        connection = myConnectionManager.getConnectionForFile(path.getIOFile());
      }
      if (connection == null) {
        myExternal.add(path);
        continue;
      }
      final ConnectionId id = connection.getId();
      Collection<FilePath> files = mySortedFiles.get(id);
      if (files == null) {
        files = new LinkedList<FilePath>();
        mySortedFiles.put(id, files);
        myConnectionShortcut.put(id, connection);
      }
      files.add(path);
    }
  }

  public Map<ConnectionId, Collection<FilePath>> getSortedFiles() {
    return mySortedFiles;
  }

  public Map<ConnectionId, P4Connection> getConnectionShortcut() {
    return myConnectionShortcut;
  }

  public Collection<FilePath> getExternal() {
    return myExternal;
  }
}
