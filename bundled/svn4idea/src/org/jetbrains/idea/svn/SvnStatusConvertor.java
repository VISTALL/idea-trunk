package org.jetbrains.idea.svn;

import com.intellij.openapi.vcs.FileStatus;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.SVNException;

public class SvnStatusConvertor {
  private SvnStatusConvertor() {
  }

  public static FileStatus convertStatus(final SVNStatus status) throws SVNException {
    if (status == null) {
      return FileStatus.UNKNOWN;
    }
    if (status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED) {
      return FileStatus.UNKNOWN;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_MISSING) {
      return FileStatus.DELETED_FROM_FS;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_EXTERNAL) {
      return SvnFileStatus.EXTERNAL;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_OBSTRUCTED) {
      return SvnFileStatus.OBSTRUCTED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_IGNORED) {
      return FileStatus.IGNORED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_ADDED) {
      return FileStatus.ADDED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_DELETED) {
      return FileStatus.DELETED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_REPLACED) {
      return SvnFileStatus.REPLACED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED ||
             status.getPropertiesStatus() == SVNStatusType.STATUS_CONFLICTED) {
      if (status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED &&
        status.getPropertiesStatus() == SVNStatusType.STATUS_CONFLICTED) {
        return FileStatus.MERGED_WITH_BOTH_CONFLICTS;
      } else if (status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED) {
        return FileStatus.MERGED_WITH_CONFLICTS;
      }
      return FileStatus.MERGED_WITH_PROPERTY_CONFLICTS;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_MODIFIED ||
             status.getPropertiesStatus() == SVNStatusType.STATUS_MODIFIED) {
      return FileStatus.MODIFIED;
    }
    else if (status.isSwitched()) {
      return FileStatus.SWITCHED;
    }
    else if (status.isCopied()) {
      return FileStatus.ADDED;
    }
    return FileStatus.NOT_CHANGED;
  }
}
