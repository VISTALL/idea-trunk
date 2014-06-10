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
package org.jetbrains.idea.perforce.actions;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AsyncUpdateAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.CancelActionException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.P4File;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionBaseFile extends AsyncUpdateAction<VcsContext> implements DumbAware {

  public static class TemporarySettings {
    public boolean DO_FOR_ALL = false;
    private final int myFileCount;

    public TemporarySettings(final int fileCount) {
      myFileCount = fileCount;
    }

    public int getFileCount() {
      return myFileCount;
    }
  }

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.actions.ActionBaseFile");

  protected final static String[] YES_NO_OPTIONS = {CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()};
  protected final static String[] YES_NO_CANCELREST_OPTIONS = {CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText(),
    PerforceBundle.message("button.text.cancel.rest")};
  protected final static String[] OK_CANCELREST_OPTIONS = {CommonBundle.getOkButtonText(),
    PerforceBundle.message("button.text.cancel.rest")};


  protected static void log(@NonNls final String msg) {
    LOG.debug(msg);
  }

  protected VcsContext prepareDataFromContext(final AnActionEvent e) {
    return VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
  }


  protected void performUpdate(final Presentation presentation, final VcsContext context) {
    final Project project = context.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(isEnabled(context));
  }

  protected boolean isEnabled(final VcsContext context) {
    return true;
  }

  @Override
  protected boolean forceSyncUpdate(AnActionEvent e) {
    return true;
  }

  protected void performAction(final VirtualFile vFile,
                               Project project,
                               boolean topLevel,
                               final boolean alone,
                               final ActionBaseFile.TemporarySettings tempSettings, final List<VirtualFile> filesToPostProcess) throws CancelActionException, VcsException {
  }

  public void actionPerformed(final AnActionEvent event) {
    final Project project = event.getData(PlatformDataKeys.PROJECT);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });

    final VirtualFile[] vFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

    if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(PerforceVcs.getInstance(project), vFiles)) {
      return;
    }

    processFiles(project, vFiles);
  }

  public void processFiles(final Project project, final VirtualFile[] vFiles) {
    boolean containsDirectory = false;
    if (vFiles != null && vFiles.length > 0) {
      try {
        final TemporarySettings tempSettings = new TemporarySettings(vFiles.length);
        List<VirtualFile> filesToPostProcess = new ArrayList<VirtualFile>();
        for (final VirtualFile vFile : vFiles) {
          final boolean[] cancelled = new boolean[1];
          cancelled[0] = false;
          try {
            try {
              performAction(vFile, project, true, (vFiles.length == 1), tempSettings, filesToPostProcess);
              if (vFile.isDirectory()) {
                containsDirectory = true;
              }
            }
            catch (VcsException e) {
              AbstractVcsHelper.getInstance(project).showError(e, PerforceBundle.message("dialog.title.perforce"));
            }
          }
          catch (CancelActionException e) {
            cancelled[0] = true;
          }
          if (cancelled[0]) {
            break;
          }
        }
        postProcessFiles(project, filesToPostProcess);
      }
      finally {
        if (containsDirectory) {
          VirtualFileManager.getInstance().refresh(true, new Runnable() {
            public void run() {
              P4File.invalidateFstat(project);
              for(VirtualFile file: vFiles) {
                if (file.isDirectory()) {
                  VcsDirtyScopeManager.getInstance(project).dirDirtyRecursively(file, true);
                }
                else {
                  VcsDirtyScopeManager.getInstance(project).fileDirty(file);
                }
              }
            }
          });
        }
        else {
          for (final VirtualFile vFile : vFiles) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                vFile.refresh(false, false);
              }
            });
            P4File.invalidateFstat(vFile);
            VcsDirtyScopeManager.getInstance(project).fileDirty(vFile);
          }
        }
      }
    }
  }

  public void postProcessFiles(final Project project, final List<VirtualFile> filesToPostProcess) {
  }

  protected static boolean checkFilename(final P4File p4File, final Project project) {
    final String complaint = PerforceVcs.getFileNameComplaint(p4File);
    if (complaint != null) {
      log(complaint);
      MessageManager.showMessageDialog(project,
                                       PerforceBundle.message("message.text.filename.non.acceptable", complaint),
                                       PerforceBundle.message("message.title.filename.non.acceptable"),
                                       Messages.getWarningIcon());
      return false;
    }
    return true;
  }
}
