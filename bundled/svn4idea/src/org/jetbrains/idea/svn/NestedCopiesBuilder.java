package org.jetbrains.idea.svn;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import java.util.*;

public class NestedCopiesBuilder implements StatusReceiver {
  private final Set<MyPointInfo> mySet;

  public NestedCopiesBuilder() {
    mySet = new HashSet<MyPointInfo>();
  }

  public void process(final FilePath path, final SVNStatus status, final boolean isInnerCopyRoot) throws SVNException {
    if ((path.getVirtualFile() != null) && SVNStatusType.STATUS_EXTERNAL.equals(status.getContentsStatus())) {
      final MyPointInfo info = new MyPointInfo(path.getVirtualFile(), null, WorkingCopyFormat.UNKNOWN, NestedCopyType.external);
      mySet.add(info);
      return;
    }
    if ((path.getVirtualFile() == null) || (status.getURL() == null)) return;

    final SVNStatusType contentsStatus = status.getContentsStatus();
    final NestedCopyType type;
    if (SVNStatusType.STATUS_UNVERSIONED.equals(contentsStatus)) {
      return;
    } else if (status.isSwitched()) {
      type = NestedCopyType.switched;
    } else if (isInnerCopyRoot) {
      // will not be changed or modified; can't be switched or external
      type = NestedCopyType.inner;
    } else {
      return;
    }
    final MyPointInfo info = new MyPointInfo(path.getVirtualFile(), status.getURL(),
                                             WorkingCopyFormat.getInstance(status.getWorkingCopyFormat()), type);
    mySet.add(info);
  }

  public void processIgnored(final VirtualFile vFile) {
  }

  public void processUnversioned(final VirtualFile vFile) {
  }

  static class MyPointInfo {
    private final VirtualFile myFile;
    private SVNURL myUrl;
    private WorkingCopyFormat myFormat;
    private final NestedCopyType myType;

    MyPointInfo(@NotNull final VirtualFile file, final SVNURL url, final WorkingCopyFormat format, final NestedCopyType type) {
      myFile = file;
      myUrl = url;
      myFormat = format;
      myType = type;
    }

    public void setUrl(SVNURL url) {
      myUrl = url;
    }

    public void setFormat(WorkingCopyFormat format) {
      myFormat = format;
    }

    public VirtualFile getFile() {
      return myFile;
    }

    public SVNURL getUrl() {
      return myUrl;
    }

    public WorkingCopyFormat getFormat() {
      return myFormat;
    }

    public NestedCopyType getType() {
      return myType;
    }

    private String key(final VirtualFile file) {
      return file.getPath();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyPointInfo info = (MyPointInfo)o;

      if (! key(myFile).equals(key(info.myFile))) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return key(myFile).hashCode();
    }
  }

  public Set<MyPointInfo> getSet() {
    return mySet;
  }
}
