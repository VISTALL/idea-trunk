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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.ActionWithTempFile;
import org.jetbrains.idea.perforce.CancelActionException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.*;

import java.io.File;
import java.util.List;

public final class ActionEdit extends ActionBaseFile {
  @SuppressWarnings({"unchecked"})
  protected void performAction(final VirtualFile vFile,
                               final Project project,
                               final boolean topLevel,
                               final boolean alone,
                               final ActionBaseFile.TemporarySettings tempSettings,
                               final List<VirtualFile> filesToPostProcess) throws CancelActionException, VcsException {
    final P4File p4File = P4File.create(vFile);
    final PerforceRunnerI runner = PerforceRunner.getInstance(project).getProxy();

    log("Action edit on file: " + p4File.getLocalPath());

    if (vFile.isDirectory()) {
      final VirtualFile[] children = (VirtualFile[])ApplicationManager.getApplication().runReadAction(new Computable() {
        public Object compute() {
          return vFile.getChildren();
        }
      });
      for (final VirtualFile child : children) {
        performAction(child, project, false, alone && (children.length == 1), tempSettings, filesToPostProcess);
      }
      if (topLevel) {
        if (!checkFilename(p4File, project)) {
          return;
        }

        runner.edit(p4File);
      }
    }
    else {
      if (!checkFilename(p4File, project)) {
        return;
      }

      if (!PerforceSettings.getSettings(project).ENABLED) {
        filesToPostProcess.add(vFile);
        return;
      }

      // check whether it will be under any clientspec
      final FStat p4FStat = p4File.getFstat(project, true);
      if (p4FStat.status == FStat.STATUS_NOT_IN_CLIENTSPEC ||
          p4FStat.status == FStat.STATUS_UNKNOWN) {
        if (vFile.isWritable()) {
          return;
        }
        if (alone) {
          final String msg = PerforceBundle.message("error.message.file.not.under.any.clientspec", p4File.getLocalPath());
          MessageManager.showMessageDialog(project, msg, PerforceBundle.message("message.title.cannot.edit"), Messages.getErrorIcon());
        }
        else {
          final String msg =
            PerforceBundle.message("confirmation.text.file.not.under.any.clientspec.continue.checkout", p4File.getLocalPath());

          final int answer = MessageManager.showDialog(project,
                                                       msg,
                                                       PerforceBundle.message("message.title.cannot.edit"),
                                                       YES_NO_OPTIONS,
                                                       1,
                                                       Messages.getErrorIcon());
          if (answer != 0) {
            throw new CancelActionException();
          }
        }
      }
      else {
        if (p4FStat.status == FStat.STATUS_NOT_ADDED ||
            p4FStat.status == FStat.STATUS_ONLY_LOCAL) {
          if (alone) {
            MessageManager.showMessageDialog(project,
                                             PerforceBundle.message("message.text.file.not.on.server", p4File.getLocalPath()),
                                             PerforceBundle.message("message.title.cannot.edit"),
                                             Messages.getErrorIcon());
          }
        }
        else if (p4FStat.status == FStat.STATUS_DELETED) {
          if (alone) {
            MessageManager.showMessageDialog(project,
                                             PerforceBundle.message("message.text.file.deleted.from.server",
                                                                                  p4File.getLocalPath()),
                                             PerforceBundle.message("message.title.cannot.edit"),
                                             Messages.getErrorIcon());
          }
        }
        else if (p4FStat.status == FStat.STATUS_ONLY_ON_SERVER) {
          final String msg =
              PerforceBundle.message("confirmation.text.file.registered.as.only.on.server.replace.it", p4File.getLocalPath(), p4FStat.depotFile);

          final int answer = MessageManager.showDialog(project,
                                                       msg,
                                                       PerforceBundle.message("confirmation.title.file.already.in.perforce"),
                                                       YES_NO_CANCELREST_OPTIONS,
                                                       1,
                                                       Messages.getErrorIcon());
          if (answer == 1) {
            //return
          }
          else if (answer == 2 || answer == -1) {
            throw new CancelActionException();
          }
          else {
            final String localPath = p4File.getLocalPath();
            final File file = new File(localPath);
            new ActionWithTempFile(file){
              protected void executeInternal() throws VcsException {
                runner.revert(p4File, false);
                runner.sync(p4File, false);
                runner.edit(p4File);
              }
            }.execute();

          }
        }
        else if (p4FStat.local == FStat.LOCAL_DELETING) {
          final String msg =
              PerforceBundle.message("confirmation.text.file.marked.for.deletion.revert.and.replace", p4File.getLocalPath(), p4FStat.depotFile);

          final int answer = MessageManager.showDialog(project,
                                                       msg,
                                                       PerforceBundle.message("confirmation.title.file.already.in.perforce"),
                                                       YES_NO_CANCELREST_OPTIONS,
                                                       1,
                                                       Messages.getErrorIcon());
          if (answer == 1) {
            //return;
          }
          else if (answer == 2 || answer == -1) {
            throw new CancelActionException();
          }
          else {
            final String localPath = p4File.getLocalPath();
            final File file = new File(localPath);
            new ActionWithTempFile(file){
              protected void executeInternal() throws VcsException {
                runner.revert(p4File, false);
                runner.edit(p4File);
              }
            }.execute();
          }

        }
        else if (p4FStat.local != FStat.LOCAL_CHECKED_IN && p4FStat.local != FStat.LOCAL_INTEGRATING) {
          if (alone) {
            final String msg =
              PerforceBundle.message("message.text.file.already.being.checked.out.or.added", p4File.getLocalPath());
            MessageManager.showMessageDialog(project, msg, PerforceBundle.message("message.title.no.reason.to.edit"), Messages.getInformationIcon());
          }
        }
        else {
          filesToPostProcess.add(vFile);
        }
      }
    }
  }

  @Override
  public void postProcessFiles(final Project project, final List<VirtualFile> filesToPostProcess) {
    final VirtualFile[] fileArray = filesToPostProcess.toArray(new VirtualFile[filesToPostProcess.size()]);
    try {
      PerforceVcs.getInstance(project).getEditFileProvider().editFiles(fileArray);
    }
    catch (VcsException e) {
      AbstractVcsHelper.getInstance(project).showError(e, "");
    }
  }
}
