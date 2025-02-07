/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2003-2004 QintSoft.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://subversion.tigris.org/license-1.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For exact contribution history, see the revision
 * history and logs, available at http://svnup.tigris.org/.
 * ====================================================================
 * @endcopyright
 */
/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnStatusUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.dialogs.SelectFilesDialog;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

public class MarkResolvedAction extends BasicAction {
  protected String getActionName(AbstractVcs vcs) {
    return SvnBundle.message("action.name.mark.resolved");
  }

  protected boolean needsAllFiles() {
    return false;
  }

  protected boolean isEnabled(Project project, @NotNull SvnVcs vcs, VirtualFile file) {
    if (file.isDirectory()) {
      return SvnStatusUtil.isUnderControl(project, file);
    }
    final FileStatus fStatus = FileStatusManager.getInstance(project).getStatus(file);
    return FileStatus.MERGED_WITH_CONFLICTS.equals(fStatus) || FileStatus.MERGED_WITH_BOTH_CONFLICTS.equals(fStatus) ||
           FileStatus.MERGED_WITH_PROPERTY_CONFLICTS.equals(fStatus);
  }

  protected boolean needsFiles() {
    return true;
  }

  protected void perform(Project project, SvnVcs activeVcs, VirtualFile file, DataContext context)
    throws VcsException {
    batchPerform(project, activeVcs, new VirtualFile[]{file}, context);
  }

  protected void batchPerform(Project project, SvnVcs activeVcs, VirtualFile[] files, DataContext context)
    throws VcsException {
    SvnVcs vcs = SvnVcs.getInstance(project);
    ApplicationManager.getApplication().saveAll();
    Collection<String> paths = collectResolvablePaths(vcs, files);
    if (paths.isEmpty()) {
      Messages.showInfoMessage(project, SvnBundle.message("message.text.no.conflicts.found"), SvnBundle.message("message.title.no.conflicts.found"));
      return;
    }
    String[] pathsArray = paths.toArray(new String[paths.size()]);
    SelectFilesDialog dialog = new SelectFilesDialog(project, SvnBundle.message("label.select.files.and.directories.to.mark.resolved"),
                                                     SvnBundle.message("dialog.title.mark.resolved"),
                                                     SvnBundle.message("action.name.mark.resolved"), pathsArray, "vcs.subversion.resolve"
                                                     );
    dialog.show();
    if (!dialog.isOK()) {
      return;
    }
    pathsArray = dialog.getSelectedPaths();
    try {
      SVNWCClient wcClient = vcs.createWCClient();
      for (String path : pathsArray) {
        File ioFile = new File(path);
        wcClient.doResolve(ioFile, false);
      }
    }
    catch (SVNException e) {
      throw new VcsException(e);
    }
    finally {
      for (VirtualFile file : files) {
        VcsDirtyScopeManager.getInstance(project).fileDirty(file);
        file.refresh(true, false);
        if (file.getParent() != null) {
          file.getParent().refresh(true, false);
        }
      }

    }
  }

  protected boolean isBatchAction() {
    return true;
  }

  private static Collection<String> collectResolvablePaths(final SvnVcs vcs, VirtualFile[] files) {
    final Collection<String> target = new TreeSet<String>();
    SVNStatusClient stClient = vcs.createStatusClient();
    for (VirtualFile file : files) {
      try {
        stClient.doStatus(new File(file.getPath()), true, false, false, false, new ISVNStatusHandler() {
          public void handleStatus(SVNStatus status) {
            if (status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED ||
                status.getPropertiesStatus() == SVNStatusType.STATUS_CONFLICTED) {
              target.add(status.getFile().getAbsolutePath());
            }
          }
        });
      }
      catch (SVNException e) {
        //
      }
    }
    return target;
  }
}
