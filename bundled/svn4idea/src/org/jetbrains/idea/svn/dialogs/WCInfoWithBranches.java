package org.jetbrains.idea.svn.dialogs;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.WorkingCopyFormat;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;

import java.util.List;

public class WCInfoWithBranches extends WCInfo {
  private final List<Branch> myBranches;
  private final VirtualFile myRoot;
  private final String myTrunkRoot;

  public WCInfoWithBranches(final String path, final SVNURL url, final WorkingCopyFormat format, final String repositoryRoot, final boolean isWcRoot,
                            final List<Branch> branches,
                            final VirtualFile root,
                            final String trunkToot) {
    super(path, url, format, repositoryRoot, isWcRoot);
    myBranches = branches;
    myRoot = root;
    myTrunkRoot = trunkToot;
  }

  // to be used in combo
  @Override
  public String toString() {
    return getPath();
  }

  @Override
  public VirtualFile getVcsRoot() {
    return myRoot;
  }

  public List<Branch> getBranches() {
    return myBranches;
  }

  public VirtualFile getRoot() {
    return myRoot;
  }

  public String getTrunkRoot() {
    return myTrunkRoot;
  }

  public static class Branch {
    private final String myName;
    private final String myUrl;

    public Branch(final String url) {
      myName = SVNPathUtil.tail(url);
      myUrl = url;
    }

    public String getName() {
      return myName;
    }

    public String getUrl() {
      return myUrl;
    }

    @Override
    public String toString() {
      return myName;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final Branch branch = (Branch)o;

      if (myUrl != null ? !myUrl.equals(branch.myUrl) : branch.myUrl != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return (myUrl != null ? myUrl.hashCode() : 0);
    }
  }
}
