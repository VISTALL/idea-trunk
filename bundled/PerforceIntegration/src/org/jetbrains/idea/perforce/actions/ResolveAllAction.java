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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.Pair;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class ResolveAllAction extends AsyncUpdateAction<VcsContext> implements DumbAware {
  protected VcsContext prepareDataFromContext(final AnActionEvent e) {
    return VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
  }

  protected void performUpdate(final Presentation presentation, final VcsContext context) {
    final Project project = context.getProject();
    presentation.setVisible(project != null);
    presentation.setEnabled(project != null);

    final PerforceSettings settings = PerforceSettings.getSettings(project);
    final List<P4Connection> allConnections = settings.getAllConnections();
    for (P4Connection connection : allConnections) {
      if (settings.getServerVersion(connection) < 2004){
        presentation.setVisible(false);
        presentation.setEnabled(false);
        return;
      }
    }
  }

  protected boolean forceSyncUpdate(final AnActionEvent e) {
    return true;
  }

  public void actionPerformed(AnActionEvent e) {
    final VcsContext context = VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
    try {
      final List<VirtualFile> filesToResolveUnderProject = new ArrayList<VirtualFile>();
      final PerforceVcs vcs = PerforceVcs.getInstance(context.getProject());
      final PerforceRunner runner = PerforceRunner.getInstance(context.getProject());
      final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(context.getProject());

      VcsUtil.runVcsProcessWithProgress(new VcsRunnable() {
        public void run() throws VcsException {
          final Collection<Pair<P4Connection,Collection<VirtualFile>>> rootsByConnections = vcs.getRootsByConnections();
          for (Pair<P4Connection, Collection<VirtualFile>> pair : rootsByConnections) {
            for (VirtualFile root : pair.getSecond()) {
              final List<VirtualFile> files = runner.getResolvedWithConflicts(pair.getFirst(), root);
              for(VirtualFile file: files) {
                if (vcsManager.getVcsFor(file) == vcs) {
                  filesToResolveUnderProject.add(file);
                }
              }
            }
          }
        }
      }, PerforceBundle.message("message.searching.for.files.to.resolve"), false, context.getProject());
      if (filesToResolveUnderProject.size() == 0) {
        Messages.showInfoMessage(PerforceBundle.message("message.text.no.files.to.resolve"), PerforceBundle.message("message.title.resolve"));
      } else {
        final VirtualFile[] fileArray = filesToResolveUnderProject.toArray(new VirtualFile[filesToResolveUnderProject.size()]);
        final ReadonlyStatusHandler.OperationStatus operationStatus =
          ReadonlyStatusHandler.getInstance(context.getProject()).ensureFilesWritable(fileArray);
        if (!operationStatus.hasReadonlyFiles()) {
          AbstractVcsHelper.getInstance(context.getProject()).showMergeDialog(filesToResolveUnderProject,
                                                                              new PerforceMergeProvider(context.getProject()));
        }
      }
    }
    catch (VcsException e1) {
      Messages.showErrorDialog(e1.getLocalizedMessage(), PerforceBundle.message("message.title.resolve"));
    }
  }
}
