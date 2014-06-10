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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.ActionWithTempFile;
import org.jetbrains.idea.perforce.CancelActionException;
import org.jetbrains.idea.perforce.PerforceBundle;
import static org.jetbrains.idea.perforce.PerforceBundle.message;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.List;

public final class ActionAdd extends ActionBaseFile {
  public void performAction(final VirtualFile vFile, final Project project, final boolean topLevel, final boolean alone, final ActionBaseFile.TemporarySettings tempSettings,
                            final List<VirtualFile> filesToPostProcess) throws CancelActionException, VcsException {

    final PerforceRunner runner = PerforceRunner.getInstance(project);

    if (vFile.isDirectory()) {
      final VirtualFile[] children = vFile.getChildren();
      for (final VirtualFile child : children) {
        performAction(child, project, false, alone && (children.length == 1), tempSettings, filesToPostProcess);
      }
    }
    else {
      final P4File p4File = P4File.create(vFile);

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
        if (alone) {
          final String msg = message("error.message.file.not.under.any.clientspec", p4File.getLocalPath());
          MessageManager.showMessageDialog(project, msg, message("dialog.title.cannot.add.file"), Messages.getErrorIcon());
        }
        else {
          final String msg =
            message("confirmation.file.not.under.any.clientspec.continue", p4File.getLocalPath());

          final int answer = MessageManager.showDialog(
            project,
            msg,
            message("dialog.title.cannot.add.file"),
            YES_NO_OPTIONS,
            1,
            Messages.getErrorIcon());
          if (answer != 0) {
            throw new CancelActionException();
          }
        }
      }
      else {
        if (p4FStat.status == FStat.STATUS_ONLY_ON_SERVER ||
            (p4FStat.local == FStat.LOCAL_CHECKED_IN && (p4FStat.status != FStat.STATUS_DELETED))) {
          final String msg =
            message("confirmation.file.already.in.perforce", p4File.getLocalPath(), p4FStat.depotFile);

          final int answer = MessageManager.showDialog(
            project,
            msg,
            message("confirmation.title.file.already.in.perforce"),
            YES_NO_CANCELREST_OPTIONS,
            1,
            Messages.getErrorIcon());
          if (answer == 1) {
            return;
          }
          else if (answer == 2 || answer == -1) {
            throw new CancelActionException();
          }
          else {
            runner.edit(p4File);
            return;
          }
        }
        else if (p4FStat.local == FStat.LOCAL_DELETING) {
          final String msg =
            message("confirmation.text.file.marked.for.deletion", p4File.getLocalPath(), p4FStat.depotFile);

          final int answer = MessageManager.showDialog(
            project,
            msg,
            message("confirmation.title.file.marked.for.deletion"),
            YES_NO_CANCELREST_OPTIONS,
            1,
            Messages.getErrorIcon());
          if (answer == 1) {
            return;
          }
          else if (answer == 2 || answer == -1) {
            throw new CancelActionException();
          }
          else {
            final String localPath = p4File.getLocalPath();
            final File file = new File(localPath);
            new ActionWithTempFile(file) {
              protected void executeInternal() throws VcsException {
                runner.revert(p4File, false);
                runner.edit(p4File);
              }
            }.execute();
            return;
          }

        }
        else if (p4FStat.status == FStat.STATUS_ONLY_LOCAL) {
          final String msg = message("message.text.file.already.being.added", p4File.getLocalPath());
          int answer = MessageManager.showDialog(project, msg, message("dialog.title.cannot.add.file"),
                                                 OK_CANCELREST_OPTIONS, 0, Messages.getErrorIcon());
          if (answer == 1 || answer == -1) {
            throw new CancelActionException();
          }
          return;
        }
        else if (p4FStat.status == FStat.STATUS_ON_SERVER_AND_LOCAL) {
          final String msg = message("message.text.file.already.being.edited", p4File.getLocalPath());
          int answer = MessageManager.showDialog(project, msg, message("dialog.title.cannot.add.file"),
                                                 OK_CANCELREST_OPTIONS, 0, Messages.getErrorIcon());
          if (answer == 1 || answer == -1) {
            throw new CancelActionException();
          }
          return;
        }
        filesToPostProcess.add(vFile);
      }
    }
  }

  @Override
  public void postProcessFiles(final Project project, final List<VirtualFile> filesToPostProcess) {
    final CheckinEnvironment checkinEnvironment = PerforceVcs.getInstance(project).getOfflineCheckinEnvironment();
    final List<VcsException> exceptions = checkinEnvironment.scheduleUnversionedFilesForAddition(filesToPostProcess);
    if (!exceptions.isEmpty()) {
      AbstractVcsHelper.getInstance(project).showErrors(exceptions, PerforceBundle.message("tab.title.error.adding.files"));
    }
  }
}
