package org.jetbrains.idea.svn;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

public class SvnScopeZipper implements Runnable {
  private final VcsDirtyScope myIn;
  private final List<FilePath> myRecursiveDirs;
  // instead of set and heavy equals of file path
  private final Map<String, MyDirNonRecursive> myNonRecursiveDirs;
  // those alone in their immediate parent
  private final List<FilePath> mySingleFiles;

  public SvnScopeZipper(final VcsDirtyScope in) {
    myIn = in;
    myRecursiveDirs = new ArrayList<FilePath>(in.getRecursivelyDirtyDirectories());
    myNonRecursiveDirs = new HashMap<String, MyDirNonRecursive>();
    mySingleFiles = new ArrayList<FilePath>();
  }

  public void run() {
    // if put directly into dirty scope, to access a copy will be created every time
    final Set<FilePath> files = myIn.getDirtyFilesNoExpand();

    for (FilePath file : files) {
      if (file.isDirectory()) {
        final VirtualFile vFile = file.getVirtualFile();
        // todo take care about this 'not valid' - right now keeping things as they used to be
        if (vFile != null && vFile.isValid()) {
          final MyDirNonRecursive me = createOrGet(file);
          me.setInterestedInParent(true);
          for (VirtualFile child : vFile.getChildren()) {
            me.add(new FilePathImpl(child));
          }
        }
      } else {
        final FilePath parent = file.getParentPath();
        if (parent != null) {
          final MyDirNonRecursive item = createOrGet(parent);
          item.add(file);
        }
      }
    }

    // move alone files into a separate list
    /*for (Iterator<Map.Entry<String, MyDirNonRecursive>> iterator = myNonRecursiveDirs.entrySet().iterator(); iterator.hasNext();) {
      final Map.Entry<String, MyDirNonRecursive> entry = iterator.next();
      final MyDirNonRecursive item = entry.getValue();
      if ((! item.isInterestedInParent()) && (item.getChildrenList().size() == 1)) {
        iterator.remove();
        mySingleFiles.add(item.getChildrenList().iterator().next());
      }
    }*/
  }

  private MyDirNonRecursive createOrGet(final FilePath parent) {
    final String key = getKey(parent);
    final MyDirNonRecursive result = myNonRecursiveDirs.get(key);
    if (result != null) return result;
    final MyDirNonRecursive newItem = new MyDirNonRecursive(parent);
    myNonRecursiveDirs.put(key, newItem);
    return newItem;
  }

  public List<FilePath> getRecursiveDirs() {
    return myRecursiveDirs;
  }

  public Map<String, MyDirNonRecursive> getNonRecursiveDirs() {
    return myNonRecursiveDirs;
  }

  public List<FilePath> getSingleFiles() {
    return mySingleFiles;
  }

  static class MyDirNonRecursive {
    private boolean myInterestedInParent;
    private final FilePath myDir;
    // instead of set and heavy equals of file path
    private final Map<String, FilePath> myChildren;

    private MyDirNonRecursive(final FilePath dir) {
      myDir = dir;
      myChildren = new HashMap<String, FilePath>();
    }

    public boolean isInterestedInParent() {
      return myInterestedInParent;
    }

    public void setInterestedInParent(boolean interestedInParent) {
      myInterestedInParent = interestedInParent;
    }

    public void add(final FilePath path) {
      myChildren.put(getKey(path), path);
    }

    public Collection<FilePath> getChildrenList() {
      return myChildren.values();
    }

    public FilePath getDir() {
      return myDir;
    }
  }

  public static String getKey(final FilePath path) {
    return path.getPresentableUrl();
  }
}
