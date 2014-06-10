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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.DefaultRepositoryLocation;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ParticularConnectionPerforceIntegratePanel implements PerforcePanel{
  private JComboBox myBranches;
  private JComboBox myChangeLists;
  private JButton myNewChangeListButton;
  private JPanel myPanel;
  private final ChangeListChooser myChangeListChooser;
  private JCheckBox myUsingChangeList;
  private JCheckBox myIsReverse;
  private JTextArea myBranchSpecDescription;

  private JLabel myIntegrateWithChangeListLabel;
  private final P4Connection myConnection;
  private final PerforceRunner myRunner;

  public ParticularConnectionPerforceIntegratePanel(final Project project, final P4Connection connection) {
    myChangeListChooser = new ChangeListChooser(myChangeLists, myNewChangeListButton, project, connection);
    myConnection = connection;
    myRunner = PerforceRunner.getInstance(project);

    myUsingChangeList.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if (myUsingChangeList.isSelected()) {
          PerforceChangeList selectedChangeList = AbstractVcsHelper.getInstance(project).
            chooseCommittedChangeList(PerforceVcs.getInstance(project).getCommittedChangesProvider(),
                                      new DefaultRepositoryLocation(project.getBaseDir().getPresentableUrl()));

          if (selectedChangeList != null) {
            myIntegrateWithChangeListLabel.setText(String.valueOf(selectedChangeList.getNumber()));
          } else {
            myIntegrateWithChangeListLabel.setText("");
            myUsingChangeList.setSelected(false);
          }
        } else {
          myIntegrateWithChangeListLabel.setText("");
        }
      }
    });

    myBranches.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String selectedBranchName = (String)myBranches.getSelectedItem();
        if (selectedBranchName == null) {
          myBranchSpecDescription.setText(PerforceBundle.message("no.branch.spec.selected.label.text"));
        } else {
          try {
            BranchSpec branchSpec = myRunner.loadBranchSpec(selectedBranchName, connection);
            myBranchSpecDescription.setText(composeBranchSpecText(branchSpec));
          }
          catch (VcsException e1) {
            myBranchSpecDescription.setText(PerforceBundle.message("cannot.load.branch.spec.description.error.text", e1.getLocalizedMessage()));
          }
        }
      }

    });
  }

  private static String composeBranchSpecText(final BranchSpec branchSpec) {
    final StringBuffer result = new StringBuffer();
    result.append(branchSpec.getDescription());
    result.append(" (");
    result.append(PerforceBundle.message("configure.integrate.branch.spec.description.part.created.by", branchSpec.getOwner()));
    result.append(")");
    result.append("\n");
    for (String view : branchSpec.getViews()) {
      result.append("\n");
      result.append(view);
    }

    return result.toString();
  }


  public void updateFrom(PerforceSettings settings) {

    myBranches.removeAllItems();

    final ParticularConnectionSettings connectionSettings = settings.getSettings(myConnection);


    try {
      List<String> branches = myRunner.getBranches(myConnection);

      for (final String branch : branches) {
        myBranches.addItem(branch);
      }

      myBranches.setSelectedItem(connectionSettings.INTEGRATE_BRANCH_NAME);

      if (myBranches.getSelectedIndex() < 0 && myBranches.getItemCount() > 0) {
        myBranches.setSelectedIndex(0);
      }

    }
    catch (VcsException e) {
      Messages.showErrorDialog(PerforceBundle.message("integrate.configurable.cannot.load.branches.error.message", e.getLocalizedMessage()),
                               PerforceBundle.message("integrate.configurable.load.branches.dialog.title"));
    }

    try {
      myChangeListChooser.fillChangeLists(myRunner.getPendingChangeLists(myConnection), 0);
    }
    catch (VcsException e1) {
      Messages.showErrorDialog(PerforceBundle.message("message.text.cannot.load.changes", e1.getLocalizedMessage()), PerforceBundle.message("message.title.refresh.chnages"));
    }


    myUsingChangeList.setSelected(false);
    myIsReverse.setSelected(connectionSettings.INTEGRATE_REVERSE);

    myIntegrateWithChangeListLabel.setText("");
  }

  public void applyTo(PerforceSettings settings) throws ConfigurationException {
    if (myBranches.getSelectedItem() == null) {
      throw new ConfigurationException(PerforceBundle.message("configure.integrate.branchspec.none.selected"));
    }
    final ParticularConnectionSettings connectionSettings = settings.getSettings(myConnection);

    connectionSettings.INTEGRATE_BRANCH_NAME = (String)myBranches.getSelectedItem();
    connectionSettings.INTEGRATE_TO_CHANGELIST_NUM = myChangeListChooser.getChangeListNumber();

    connectionSettings.INTEGRATED_CHANGE_LIST_NUMBER = myIntegrateWithChangeListLabel.getText();
    connectionSettings.INTEGRATE_CHANGE_LIST = myUsingChangeList.isSelected();
    connectionSettings.INTEGRATE_REVERSE = myIsReverse.isSelected();

  }

  public boolean isModified(PerforceSettings settings) {
    final ParticularConnectionSettings connectionSettings = settings.getSettings(myConnection);

    return !Comparing.equal(connectionSettings.INTEGRATE_BRANCH_NAME, (String)myBranches.getSelectedItem()) ||
           connectionSettings.INTEGRATE_TO_CHANGELIST_NUM != myChangeListChooser.getChangeListNumber() ||
           !Comparing.equal(connectionSettings.INTEGRATED_CHANGE_LIST_NUMBER, myIntegrateWithChangeListLabel.getText()) ||
           connectionSettings.INTEGRATE_CHANGE_LIST != myUsingChangeList.isSelected() ||
           connectionSettings.INTEGRATE_REVERSE != myIsReverse.isSelected();
  }

  public JPanel getPanel() {
    return myPanel;
  }
}
