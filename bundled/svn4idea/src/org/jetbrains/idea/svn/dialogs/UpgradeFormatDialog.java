package org.jetbrains.idea.svn.dialogs;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MultiLineLabelUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class UpgradeFormatDialog extends DialogWrapper  {
  private JRadioButton myUpgradeNoneButton;
  private JRadioButton myUpgradeAutoButton;
  private JRadioButton myUpgradeAuto15Button;
  private JRadioButton myUpgradeAuto16Button;

  protected File myPath;

  public UpgradeFormatDialog(Project project, File path, boolean canBeParent) {
    this(project, path, canBeParent, true);
  }

  protected UpgradeFormatDialog(Project project, File path, boolean canBeParent, final boolean initHere) {
    super(project, canBeParent);
    myPath = path;
    setResizable(false);
    setTitle(SvnBundle.message("dialog.upgrade.wcopy.format.title"));

    if (initHere) {
      init();
    }
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return "svn.upgradeDialog";
  }

  public void setData(final boolean display13format, final String selectedFormat) {
    if (SvnConfiguration.UPGRADE_AUTO_16.equals(selectedFormat)) {
      myUpgradeAuto16Button.setSelected(true);
    } else if (SvnConfiguration.UPGRADE_AUTO.equals(selectedFormat)) {
      myUpgradeAutoButton.setSelected(true);
    } else if (SvnConfiguration.UPGRADE_AUTO_15.equals(selectedFormat)) {
      myUpgradeAuto15Button.setSelected(true);
    } else {
      myUpgradeNoneButton.setSelected(true);
    }
    myUpgradeNoneButton.setVisible(display13format);
    if (myUpgradeNoneButton.isSelected() && (! display13format)) {
      myUpgradeAutoButton.setSelected(true);
    }
  }

  protected String getTopMessage(final String label) {
    return SvnBundle.message(new StringBuilder().append("label.configure.").append(label).append(".label").toString(),
                             ApplicationNamesInfo.getInstance().getFullProductName());
  }

  @Nullable
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    GridBagConstraints gb = new GridBagConstraints();


    // top label.
    gb.insets = new Insets(2, 2, 2, 2);
    gb.weightx = 1;
    gb.weighty = 0;
    gb.gridwidth = 2;
    gb.gridheight = 1;
    gb.gridx = 0;
    gb.gridy = 0;
    gb.anchor = GridBagConstraints.WEST;
    gb.fill = GridBagConstraints.HORIZONTAL;

    File adminPath = new File(myPath, SVNFileUtil.getAdminDirectoryName());
    final boolean adminPathIsDirectory = adminPath.isDirectory();
    final String label = getMiddlePartOfResourceKey(adminPathIsDirectory);

    JLabel topLabel = new JLabel(getTopMessage(label));
    topLabel.setUI(new MultiLineLabelUI());
    panel.add(topLabel, gb);
    gb.gridy += 1;

    myUpgradeNoneButton = new JRadioButton(SvnBundle.message(new StringBuilder().append("radio.configure.").append(label).append(".none").toString()));
    myUpgradeAutoButton = new JRadioButton(SvnBundle.message(new StringBuilder().append("radio.configure.").append(label).append(".auto").toString()));
    myUpgradeAuto15Button = new JRadioButton(SvnBundle.message(new StringBuilder().append("radio.configure.").append(label).append(".auto.15format").toString()));
    myUpgradeAuto16Button = new JRadioButton(SvnBundle.message(new StringBuilder().append("radio.configure.").append(label).append(".auto.16format").toString()));

    ButtonGroup group = new ButtonGroup();
    group.add(myUpgradeNoneButton);
    group.add(myUpgradeAutoButton);
    group.add(myUpgradeAuto15Button);
    group.add(myUpgradeAuto16Button);
    panel.add(myUpgradeNoneButton, gb);
    gb.gridy += 1;
    panel.add(myUpgradeAutoButton, gb);
    gb.gridy += 1;
    panel.add(myUpgradeAuto15Button, gb);
    gb.gridy += 1;
    panel.add(myUpgradeAuto16Button, gb);
    gb.gridy += 1;

    myUpgradeNoneButton.setSelected(true);

    final JPanel auxiliaryPanel = getBottomAuxiliaryPanel();
    if (auxiliaryPanel != null) {
      panel.add(auxiliaryPanel, gb);
      gb.gridy += 1;
    }

    return panel;
  }

  @Nullable
  protected JPanel getBottomAuxiliaryPanel() {
    return null;
  }

  protected String getMiddlePartOfResourceKey(final boolean adminPathIsDirectory) {
    return ! adminPathIsDirectory ? "create" : "upgrade";
  }

  protected boolean showHints() {
    return true;
  }

  @Nullable
  public String getUpgradeMode() {
    if (myUpgradeNoneButton.isSelected()) {
      return SvnConfiguration.UPGRADE_NONE;
    } else if (myUpgradeAutoButton.isSelected()) {
      return SvnConfiguration.UPGRADE_AUTO;
    } else if (myUpgradeAuto15Button.isSelected()) {
      return SvnConfiguration.UPGRADE_AUTO_15;
    } else if (myUpgradeAuto16Button.isSelected()) {
      return SvnConfiguration.UPGRADE_AUTO_16;
    }
    return null;
  }

}
