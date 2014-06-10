package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import javax.swing.*;
import java.awt.*;

/**
 * @author yole
 */
public class PerforceCheckinOptionsPanel implements RefreshableOnComponent {
  private final JPanel myPanel;
  private final JCheckBox myChkRevertUnchanged;
  private final Project myProject;

  public PerforceCheckinOptionsPanel(Project project) {
    myProject = project;
    myPanel = new JPanel(new BorderLayout());
    myChkRevertUnchanged = new JCheckBox("Revert unchanged files");
    myPanel.add(myChkRevertUnchanged, BorderLayout.CENTER);
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public void refresh() {
  }

  public void saveState() {
    PerforceSettings.getSettings(myProject).REVERT_UNCHANGED_FILES_CHECKIN = myChkRevertUnchanged.isSelected();
  }

  public void restoreState() {
    myChkRevertUnchanged.setSelected(PerforceSettings.getSettings(myProject).REVERT_UNCHANGED_FILES_CHECKIN);
  }
}
