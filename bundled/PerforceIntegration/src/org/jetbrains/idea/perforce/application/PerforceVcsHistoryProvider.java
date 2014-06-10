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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.history.*;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.vcsUtil.VcsRunnable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PerforceVcsHistoryProvider implements VcsHistoryProvider {
  private final PerforceVcs myVcs;
  private static final ColumnInfo<VcsFileRevision, String> REVISION = new ColumnInfo<VcsFileRevision, String>(
    PerforceBundle.message("file.history.revision.column.name")) {
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return String.valueOf(((PerforceFileRevision) vcsFileRevision).getVersionNumber());
    }

    public Comparator<VcsFileRevision> getComparator() {
      return new Comparator<VcsFileRevision>() {
        public int compare(VcsFileRevision r1, VcsFileRevision r2) {
          if (!(r1 instanceof PerforceFileRevision)) return 1;
          if (!(r2 instanceof PerforceFileRevision)) return -1;
          return (int)(((PerforceFileRevision) r1).getVersionNumber() - ((PerforceFileRevision) r2).getVersionNumber());
        }
      };
    }
  };

  private static final ColumnInfo<VcsFileRevision, String> ACTION = new ColumnInfo<VcsFileRevision, String>(
    PerforceBundle.message("file.history.action.column.name")) {
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return ((PerforceFileRevision) vcsFileRevision).getAction();
    }
  };

  private static final ColumnInfo<VcsFileRevision, String> CLIENT = new ColumnInfo<VcsFileRevision, String>(
    PerforceBundle.message("file.history.client.column.name")) {
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return ((PerforceFileRevision) vcsFileRevision).getClient();
    }
  };
  private final PerforceRunner myRunner;


  public PerforceVcsHistoryProvider(PerforceVcs vcs) {
    myVcs = vcs;
    myRunner = PerforceRunner.getInstance(vcs.getProject());
  }

  public VcsDependentHistoryComponents getUICustomization(final VcsHistorySession session, JComponent forShortcutRegistration) {
    return VcsDependentHistoryComponents.createOnlyColumns(new ColumnInfo[]{
      REVISION, ACTION, CLIENT
    });


  }

  public AnAction[] getAdditionalActions(final FileHistoryPanel panel) {
    return new AnAction[]{new ShowBranchesAction(panel), new ShowAllSubmittedFilesAction()};
  }

  public boolean isDateOmittable() {
    return false;
  }

  @Nullable
  public String getHelpId() {
    return null;
  }

  public VcsHistorySession createSessionFor(FilePath filePath) throws VcsException {
    filePath = ChangesUtil.getCommittedPath(myVcs.getProject(), filePath);

    final P4Connection connection = myVcs.getSettings().getConnectionForFile(filePath.getIOFile());
    final P4File p4File = P4File.create(filePath);
    p4File.invalidateFstat();
    final List<VcsFileRevision> revisions = new ArrayList<VcsFileRevision>();
    final Ref<VcsRevisionNumber> currentRevisionNumber = new Ref<VcsRevisionNumber>();
    VcsRunnable runnable = new VcsRunnable() {
      public void run() throws VcsException {
        PerforceRunner runner = PerforceRunner.getInstance(myVcs.getProject());
        P4Revision[] p4Revisions = runner.filelog(p4File, PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY);
        for (P4Revision p4Revision : p4Revisions) {
          revisions.add(new PerforceFileRevision(p4Revision,
                                                 myVcs.getSettings(),
                                                 connection));
        }
        currentRevisionNumber.set(getCurrentRevision(p4File));
      }
    };
    /*if (ApplicationManager.getApplication().isDispatchThread()) {
      VcsUtil.runVcsProcessWithProgress(runnable, PerforceBundle.message("loading.file.history.progress"), false, myVcs.getProject());
    }
    else {*/
      // async history refresh
      runnable.run();
    /*}*/

    return new VcsHistorySession(revisions, currentRevisionNumber.get()) {
      @Nullable
      public VcsRevisionNumber calcCurrentRevisionNumber() {
        return getCurrentRevision(p4File);
      }

      @Override
      public boolean isCurrentRevision(VcsRevisionNumber rev) {
        if (!(rev instanceof PerforceVcsRevisionNumber)) return false;
        PerforceVcsRevisionNumber p4rev = (PerforceVcsRevisionNumber) rev;
        PerforceVcsRevisionNumber currentRev = (PerforceVcsRevisionNumber) getCachedRevision();
        return currentRev != null && p4rev.getRevisionNumber() == currentRev.getRevisionNumber() && !p4rev.isBranched();
      }

      @Override
      public boolean allowAsyncRefresh() {
        return true;
      }

      @Override
      public boolean refresh() {
        final VcsRevisionNumber oldValue = getCachedRevision();
        final VcsRevisionNumber newNumber = calcCurrentRevisionNumber();
        setCachedRevision(newNumber);
        return oldValue == null || (((PerforceVcsRevisionNumber) oldValue).getRevisionNumber() !=
                                    ((PerforceVcsRevisionNumber) newNumber).getRevisionNumber());
      }
    };
  }

  private VcsRevisionNumber getCurrentRevision(final P4File p4File) {
    try {
      final long curRev = myRunner.haveRevision(p4File);
      // cached
      FStat fstat = p4File.getFstat(myVcs.getProject(), false);
      final long cachedRev = Long.parseLong(fstat.haveRev);
      if (cachedRev != curRev) {
        // go for head change also
        fstat = p4File.getFstat(myVcs.getProject(), true);
      }
      return new PerforceVcsRevisionNumber(Long.parseLong(fstat.haveRev) ,Long.parseLong(fstat.headChange), false);
    }
    catch (VcsException e) {
      return null;
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Nullable
  public HistoryAsTreeProvider getTreeHistoryProvider() {
    return null;
  }

  public boolean supportsHistoryForDirectories() {
    return false;
  }

  class ShowBranchesAction extends ToggleAction implements DumbAware {
    private final FileHistoryPanel myPanel;

    public ShowBranchesAction(final FileHistoryPanel panel) {
      super(PerforceBundle.message("action.name.show.branches"), null, IconLoader.getIcon("/icons/showBranches.png"));
      myPanel = panel;
    }

    public boolean isSelected(AnActionEvent e) {
      return PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY;
    }

    public void setSelected(AnActionEvent e, boolean state) {
      PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY= state;
      if (myPanel != null) {
        myPanel.refresh();
      }
    }
  }
}
