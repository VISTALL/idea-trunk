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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.application.PerforceVcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.PerforceChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Date;

public class ShowAllSubmittedFilesAction extends AnAction implements DumbAware {
  public ShowAllSubmittedFilesAction() {
    super(PerforceBundle.message("action.text.show.all.submitted"), null, IconLoader.getIcon("/icons/allRevisions.png"));
  }

  public void update(AnActionEvent e) {
    super.update(e);
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) {
      e.getPresentation().setEnabled(false);
      return;
    }
    e.getPresentation().setEnabled(e.getData(VcsDataKeys.VCS_FILE_REVISION) != null);
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final VcsFileRevision revision = e.getData(VcsDataKeys.VCS_FILE_REVISION);
    if (revision != null) {
      final PerforceFileRevision perfRevision = ((PerforceFileRevision)revision);

      showAllSubmittedFiles(project, ((PerforceVcsRevisionNumber)perfRevision.getRevisionNumber()).getChangeNumber(),
                            perfRevision.getCommitMessage(),
                            perfRevision.getRevisionDate(),
                            perfRevision.getAuthor(),
                            perfRevision.getConnection());

    }
  }

  public static void showAllSubmittedFiles(final Project project,
                                           final long number,
                                           final String submitMessage,
                                           final Date date,
                                           final String user,
                                           final P4Connection connection) {
    final ChangeListData data = new ChangeListData();
    data.NUMBER = number;
    data.USER = user;
    data.DATE  = ChangeListData.DATE_FORMAT.format(date);
    data.DESCRIPTION = submitMessage;

    final PerforceChangeList changeList = new PerforceChangeList(data, project, connection);

    try {
      final boolean result = VcsUtil.runVcsProcessWithProgress(new VcsRunnable() {
        public void run() throws VcsException {
          changeList.loadChanges();
        }
      }, PerforceBundle.message("show.all.files.from.change.list.searching.for.changed.files.progress.title"), true, project);
      if (result) {
        if (changeList.getChanges().size() > 300) {
          Messages.showInfoMessage(PerforceBundle.message("show.all.files.from.change.list.too.many.files.affected.error.message"),
                                   getTitle(changeList));
        }
        else {
          AbstractVcsHelper.getInstance(project).showChangesListBrowser(changeList, getTitle(changeList));
        }
      }
    }
    catch(VcsException ex) {
      Messages
        .showErrorDialog(PerforceBundle.message("message.text.cannot.show.revisions", ex.getLocalizedMessage()), getTitle(changeList));
    }
  }

  private static String getTitle(final PerforceChangeList changeList) {
    return PerforceBundle.message("dialog.title.show.all.revisions.in.changelist", changeList.getNumber());
  }
}
