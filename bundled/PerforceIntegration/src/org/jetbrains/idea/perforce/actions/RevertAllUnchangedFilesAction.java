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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AsyncUpdateAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ChangeListSynchronizer;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.*;

class ActionContext {
  VcsContext context;
  Collection<VirtualFile> files;

  public ActionContext(final VcsContext context, final Collection<VirtualFile> files) {
    this.context = context;
    this.files = files;
  }
}

public class RevertAllUnchangedFilesAction extends AsyncUpdateAction<ActionContext> implements DumbAware {
  protected ActionContext prepareDataFromContext(final AnActionEvent e) {
    return new ActionContext(VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e), getSelectedFiles(e));
  }

  protected void performUpdate(final Presentation presentation, final ActionContext context) {
    final Project project = context.context.getProject();
    if (project == null) {
      presentation.setVisible(false);
      return;
    }

    presentation.setVisible(hasFilesUnderPerforce(context.files, project));
    presentation.setEnabled(PerforceSettings.getSettings(project).ENABLED);
  }


  @Override
  protected boolean forceSyncUpdate(final AnActionEvent e) {
    return true;
  }

  private static Collection<VirtualFile> getSelectedFiles(final AnActionEvent e) {
    final Object panel = e.getData(CheckinProjectPanel.PANEL_KEY);
    final Collection<VirtualFile> files;
    if (panel instanceof CheckinProjectPanel) {
      files = ((CheckinProjectPanel)panel).getRoots();
    }
    else {
      VirtualFile[] filesarray = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
      if (filesarray != null) {
        files = Arrays.asList(filesarray);
      }
      else {
        e.getPresentation().setVisible(false);
        files = Collections.emptyList();
      }
    }
    return files;
  }

  private static boolean hasFilesUnderPerforce(final Collection<VirtualFile> roots, Project project) {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);

    for (VirtualFile file : roots) {
      final AbstractVcs vcs = vcsManager.getVcsFor(file);
      //noinspection HardCodedStringLiteral
      if (vcs != null && vcs.getName().equals("Perforce")) {
        return true;
      }
    }

    return false;
  }

  public void actionPerformed(final AnActionEvent e) {
    final VcsContext context = VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
    final CheckinProjectPanel panel = (CheckinProjectPanel)e.getData(CheckinProjectPanel.PANEL_KEY);
    final Collection<VirtualFile> roots = getSelectedFiles(e);
    final Project project = context.getProject();


    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });

    revertUnchanged(project, roots, panel, context.getSelectedChangeLists());
  }

  public static void revertUnchanged(final Project project, final Collection<VirtualFile> roots, final CheckinProjectPanel panel,
                                     @Nullable final ChangeList[] selectedChangeLists) {
    final PerforceVcs vcs = PerforceVcs.getInstance(project);
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    final List<VcsException> exceptions = new ArrayList<VcsException>();
    final boolean[] containsDir = new boolean[]{false};
    final List<VirtualFile> actuallyReverted = new ArrayList<VirtualFile>();

    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        boolean revertedChangeLists = false;
        if (selectedChangeLists != null && selectedChangeLists.length > 0) {
          revertedChangeLists = revertChangeLists(project, selectedChangeLists, exceptions);
        }
        if (!revertedChangeLists) {
          containsDir [0] = revertFiles(roots, project, actuallyReverted, exceptions);
        }
        else {
          actuallyReverted.addAll(roots);
        }
      }
    }, PerforceBundle.message("message.title.revert.unchanged"), false, project);

    if (actuallyReverted.size() > 0) {
      if (containsDir[0]) {
        VirtualFileManager.getInstance().refresh(true, new Runnable() {
          public void run() {
            P4File.invalidateFstat(project);
            VcsDirtyScopeManager.getInstance(project).markEverythingDirty();
          }
        });
      }
      else {
        for (final VirtualFile vFile : roots) {
          if (vcsManager.getVcsFor(vFile) == PerforceVcs.getInstance(project)) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                vFile.refresh(false, false);
              }
            });
            P4File.invalidateFstat(vFile);
            if (vFile.isDirectory()) {
              VcsDirtyScopeManager.getInstance(project).dirDirtyRecursively(vFile, true);
            }
            else {
              VcsDirtyScopeManager.getInstance(project).fileDirty(vFile);
            }
          }
        }
      }

      if (panel != null) {
        panel.refresh();
      }
    }

    if (!exceptions.isEmpty()) {
      AbstractVcsHelper.getInstance(project).showErrors(exceptions, PerforceBundle.message("message.title.revert.unchanged.files"));
    }
  }

  private static boolean revertFiles(final Collection<VirtualFile> roots, final Project project, final List<VirtualFile> actuallyReverted,
                                     final List<VcsException> exceptions) {
    boolean containsDir = false;
    final PerforceVcs vcs = PerforceVcs.getInstance(project);
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    for (VirtualFile file : roots) {
      if (vcsManager.getVcsFor(file) == vcs) {
        try {
          if (file.isDirectory()) containsDir = true;
          if (PerforceRunner.getInstance(project).revertUnchanged(P4File.create(file))) {
            actuallyReverted.add(file);
          }
        }
        catch (VcsException e1) {
          exceptions.add(e1);
        }
      }
    }
    return containsDir;
  }

  private static boolean revertChangeLists(final Project project, final ChangeList[] selectedChangeLists, final List<VcsException> exceptions) {
    final PerforceSettings perfSettings = PerforceSettings.getSettings(project);
    boolean foundAll = true;
    for(ChangeList changeList: selectedChangeLists) {
      for(P4Connection connection: perfSettings.getAllConnections()) {
        Long number = ChangeListSynchronizer.getInstance(project).getChangeListNumber(connection, changeList);
        if (number != null) {
          try {
            PerforceRunner.getInstance(project).revertUnchanged(connection, number.longValue());
          }
          catch (VcsException e1) {
            exceptions.add(e1);
          }
        }
        else {
          foundAll = false;
        }
      }
    }
    return foundAll;
  }
}
