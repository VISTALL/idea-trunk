package org.jetbrains.idea.svn;

import com.intellij.openapi.diagnostic.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import java.io.File;
import java.util.*;

public class IgnoredFileInfo {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.IgnoredFileInfo");

  // directory for which properties are collected
  private final File myFile;
  private final List<String> myPatterns;
  private final Set<String> myFileNames;
  private final Set<String> myOldPatterns;

  public IgnoredFileInfo(final File file, final Set<String> oldPatterns) {
    myFile = file;
    myPatterns = new ArrayList<String>();
    myFileNames = new HashSet<String>();
    myOldPatterns = oldPatterns;
  }

  public void addFileName(final String name) {
    myFileNames.add(name);
  }

  public void addPattern(final String value) {
    myPatterns.add(value);
  }

  public void calculatePatterns(final SvnVcs vcs) {
    final List<String> names = new ArrayList<String>();
    try {
      vcs.createStatusClient().doStatus(myFile, SVNRevision.WORKING, SVNDepth.IMMEDIATES, false, true, true, false, new ISVNStatusHandler() {
        public void handleStatus(SVNStatus status) throws SVNException {
          if (SVNStatusType.STATUS_IGNORED == status.getContentsStatus()) {
            final String name = status.getFile().getName();
            if (! myFileNames.contains(name)) {
              names.add(name);
            }
          }
        }
      }, null);
    }
    catch (SVNException e) {
      LOG.info(e);
    }

    for (String pattern : myOldPatterns) {
      boolean usedSomewhereElse = false;
      for (String name : names) {
        if (DefaultSVNOptions.matches(pattern, name)) {
          usedSomewhereElse = true;
          break;
        }
      }

      if (! usedSomewhereElse) {
        for (String name : myFileNames) {
          if (DefaultSVNOptions.matches(pattern, name)) {
            myPatterns.add(pattern);
            break;
          }
        }
      }
    }
  }

  public File getFile() {
    return myFile;
  }

  public List<String> getPatterns() {
    return myPatterns;
  }

  public Set<String> getFileNames() {
    return myFileNames;
  }

  public Set<String> getOldPatterns() {
    return myOldPatterns;
  }
}
