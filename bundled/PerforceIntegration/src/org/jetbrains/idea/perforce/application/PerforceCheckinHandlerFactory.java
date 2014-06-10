package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.actions.RevertAllUnchangedFilesAction;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

/**
 * @author yole
 */
public class PerforceCheckinHandlerFactory extends CheckinHandlerFactory {
  @NotNull
  public CheckinHandler createHandler(final CheckinProjectPanel panel) {
    return new CheckinHandler() {
      @Nullable
      public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        if (panel.getAffectedVcses().contains(PerforceVcs.getInstance(panel.getProject()))) {
          return new PerforceCheckinOptionsPanel(panel.getProject());
        }
        else {
          return null;
        }
      }

      public ReturnResult beforeCheckin() {
        final Project project = panel.getProject();
        if (PerforceSettings.getSettings(project).REVERT_UNCHANGED_FILES_CHECKIN) {
          RevertAllUnchangedFilesAction.revertUnchanged(project, panel.getVirtualFiles(), panel, null);
        }
        return super.beforeCheckin();
      }
    };
  }
}