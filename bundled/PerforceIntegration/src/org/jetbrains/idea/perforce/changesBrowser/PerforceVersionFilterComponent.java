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
package org.jetbrains.idea.perforce.changesBrowser;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SelectFromListDialog;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.versionBrowser.StandardVersionFilterComponent;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class PerforceVersionFilterComponent extends StandardVersionFilterComponent<PerforceChangeBrowserSettings> {
  private TextFieldWithBrowseButton myUserField;
  private TextFieldWithBrowseButton myClientField;
  private JCheckBox myUseUserFilter;
  private JCheckBox myUseClientFilter;

  private JPanel myPanel;
  private final Project myProject;
  private JPanel myStandardPanel;
  private final P4Connection myConnection;

  public PerforceVersionFilterComponent(final Project project, P4Connection connection, final boolean showDateFilter) {
    super(showDateFilter);
    myProject = project;
    myStandardPanel.setLayout(new BorderLayout());
    myStandardPanel.add(super.getStandardPanel(), BorderLayout.CENTER);
    myConnection = connection;
    init(new PerforceChangeBrowserSettings());
  }

  protected void init(final PerforceChangeBrowserSettings settings) {
    super.init(settings);
    installBrowseClientsAction();
    installBrowseUsersAction();
  }

  private void installBrowseUsersAction() {
    myUserField.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          List<String> clients = PerforceRunner.getInstance(myProject).getUsers(myConnection);
          if (clients.isEmpty()) {
            Messages.showMessageDialog(PerforceBundle.message("message.text.cannot.find.user"),
                                       PerforceBundle.message("dialog.title.select.user"), Messages.getInformationIcon());
            return;
          }
          final SelectFromListDialog selectDialog =
            new SelectFromListDialog(myProject, clients.toArray(), new SelectFromListDialog.ToStringAspect() {
              public String getToStirng(Object obj) {
                return obj.toString();
              }
            }, PerforceBundle.message("dialog.title.select.user"), ListSelectionModel.SINGLE_SELECTION);
          selectDialog.show();
          if (selectDialog.isOK()) {
            myUserField.setText(selectDialog.getSelection()[0].toString());
          }
        }
        catch (VcsException e1) {
          Messages.showErrorDialog(PerforceBundle.message("message.text.cannot.load.users", e1.getLocalizedMessage()),
                                   PerforceBundle.message("dialog.title.select.user"));
        }
      }
    });
  }

  private void installBrowseClientsAction() {
    myClientField.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          List<String> clients = PerforceRunner.getInstance(myProject).getClients(myConnection);
          if (clients.isEmpty()) {
            Messages.showMessageDialog(PerforceBundle.message("message.text.cannot.find.client"),
                                       PerforceBundle.message("dialog.title.select.client"), Messages.getInformationIcon());
            return;
          }
          final SelectFromListDialog selectDialog =
            new SelectFromListDialog(myProject, clients.toArray(), new SelectFromListDialog.ToStringAspect() {
              public String getToStirng(Object obj) {
                return obj.toString();
              }
            }, PerforceBundle.message("dialog.title.select.client"), ListSelectionModel.SINGLE_SELECTION);
          selectDialog.show();
          if (selectDialog.isOK()) {
            myClientField.setText(selectDialog.getSelection()[0].toString());
          }
        }
        catch (VcsException e1) {
          Messages.showErrorDialog(PerforceBundle.message("message.text.cannot.load.clients", e1.getLocalizedMessage()),
                                   PerforceBundle.message("dialog.title.select.client"));
        }
      }
    });
  }

  protected void installCheckBoxListener(final ActionListener filterListener) {
    super.installCheckBoxListener(filterListener);
    myUseClientFilter.addActionListener(filterListener);
    myUseUserFilter.addActionListener(filterListener);
  }

  protected void initValues(PerforceChangeBrowserSettings settings) {
    super.initValues(settings);
    myUseClientFilter.setSelected(settings.USE_CLIENT_FILTER);
    myUseUserFilter.setSelected(settings.USE_USER_FILTER);
    myClientField.setText(settings.CLIENT);
    myUserField.setText(settings.USER);
  }

  public void saveValues(PerforceChangeBrowserSettings settings) {
    super.saveValues(settings);

    settings.USE_CLIENT_FILTER = myUseClientFilter.isSelected();
    settings.USE_USER_FILTER = myUseUserFilter.isSelected();

    settings.CLIENT = myClientField.getText();
    settings.USER = myUserField.getText();
  }

  protected void updateAllEnabled(ActionEvent e) {
    super.updateAllEnabled(e);
    updatePair(myUseUserFilter, myUserField, e);
    updatePair(myUseClientFilter, myClientField, e);
  }

  public JComponent getComponent() {
    return myPanel;
  }
}
