package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.jobs.*;

import java.util.HashMap;
import java.util.Map;

public class LinkChangeListToJobsAction extends AnAction {
  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setText(PerforceBundle.message("action.link.changelist.to.jobs"));
    if (! enabled(e)) {
      e.getPresentation().setVisible(false);
      e.getPresentation().setEnabled(false);
      return;
    }
    e.getPresentation().setVisible(true);
    e.getPresentation().setEnabled(true);
  }

  private boolean enabled(final AnActionEvent e) {
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null || project.isDefault()) return false;
    if (! PerforceSettings.getSettings(project).USE_PERFORCE_JOBS) return false;
    final ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);
    if (lists == null || (lists.length != 1)) {
      return false;
    }
    if (((LocalChangeList) lists[0]).hasDefaultName()) {
      return false;
    }
    if (lists[0].getChanges().isEmpty()) return false;
    return true;
  }

  public void actionPerformed(AnActionEvent e) {
    if (! enabled(e)) {
      return;
    }
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    final ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);
    final LocalChangeList list = (LocalChangeList) lists[0];

    final JobDetailsLoader loader = new JobDetailsLoader(project);
    final Map<ConnectionKey, P4JobsLogicConn> connMap = new HashMap<ConnectionKey, P4JobsLogicConn>();
    final Map<ConnectionKey, java.util.List<PerforceJob>> perforceJobs = new HashMap<ConnectionKey, java.util.List<PerforceJob>>();
    loader.loadJobsForList(list, connMap, perforceJobs);

    new EditChangelistJobsDialog(project, list, false, connMap, perforceJobs).show();
  }
}
