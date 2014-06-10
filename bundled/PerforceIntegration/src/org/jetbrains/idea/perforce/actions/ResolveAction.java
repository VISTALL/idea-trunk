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
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.util.Arrays;

public class ResolveAction extends AsyncUpdateAction<VcsContext> implements DumbAware {
  protected VcsContext prepareDataFromContext(final AnActionEvent e) {
    return VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
  }

  protected void performUpdate(final Presentation presentation, final VcsContext context) {
    final Project project = context.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }
    final VirtualFile[] selectedFiles = context.getSelectedFiles();
    final PerforceSettings perforceSettings = PerforceSettings.getSettings(project);
    if (selectedFiles.length == 0 || !perforceSettings.ENABLED) {
      presentation.setEnabled(false);
      return;
    }
    else {
      for (VirtualFile selectedFile : selectedFiles) {
        if (perforceSettings.getServerVersion(perforceSettings.getConnectionForFile(selectedFile)) < 2004){
          presentation.setEnabled(false);
          return;
        }

        if (selectedFile.isDirectory()) {
          presentation.setEnabled(false);
          return;
        }
      }
    }

    boolean haveMerged = false;
    for(VirtualFile file: selectedFiles) {
      if (FileStatusManager.getInstance(project).getStatus(file) == FileStatus.MERGE) {
        haveMerged = true;
        break;
      }
    }
    presentation.setEnabled(haveMerged);
  }

  public void actionPerformed(AnActionEvent e) {
    final VcsContext context = VcsContextFactory.SERVICE.getInstance().createCachedContextOn(e);
    final VirtualFile[] selectedFiles = context.getSelectedFiles();

    final Project project = context.getProject();
    final ReadonlyStatusHandler.OperationStatus operationStatus =
      ReadonlyStatusHandler.getInstance(context.getProject()).ensureFilesWritable(selectedFiles);
    if (!operationStatus.hasReadonlyFiles()) {
      AbstractVcsHelper.getInstance(project).showMergeDialog(Arrays.asList(selectedFiles),
                                                             new PerforceMergeProvider(project));
    }
  }
}
