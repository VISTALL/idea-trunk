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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class ConfigPanel {
  private JCheckBox m_useP4Conf;
  private JTextField m_port;
  private JTextField m_client;
  private JTextField m_user;
  private JPasswordField m_passwd;
  private JCheckBox m_showCmds;

  private TextFieldWithBrowseButton m_pathToExec;
  private JPanel myPanel;
  private JLabel myOutputFileLabel;
  private JComboBox myCharset;
  private JButton myTestConnectionButton;

  private final Project myProject;
  private JCheckBox myShowBranchingHistory;
  private JCheckBox myIsEnabled;
  private JCheckBox myUseLogin;
  private JCheckBox myLoginSilently;
  private JTextField myServerTimeoutField;
  private TextFieldWithBrowseButton myP4VPathField;
  private JCheckBox myUsePerforceJobs;
  private JCheckBox myShowIntegratedChangelistsInCheckBox;

  @NonNls private static final String CHARSET_ISO8859_1 = "iso8859-1";
  @NonNls private static final String CHARSET_ISO8859_15 = "iso8859-15";
  @NonNls private static final String CHARSET_eucjp = "eucjp";
  @NonNls private static final String CHARSET_shiftjis = "shiftjis";
  @NonNls private static final String CHARSET_winansi = "winansi";
  @NonNls private static final String CHARSET_macosroman = "macosroman";
  @NonNls private static final String CHARSET_utf8 = "utf8";
  private static final String CLIENT_UNKNOWN = "Client unknown.";

  public ConfigPanel(final Project project) {
    myProject  = project;
    m_useP4Conf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reenableConnectPane();
      }
    });
    myTestConnectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final PerforceSettings settings = new PerforceSettings(myProject);
        applyTo(settings);
        PerforceConnectionManager.getInstance(myProject).refreshConnections(settings);
        final Ref<VcsException> refEx = new Ref<VcsException>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
          public void run() {
            try {
              final List<P4Connection> allConnections = settings.getAllConnections();
              for (P4Connection connection : allConnections) {
                testConnection(connection, settings);
              }
            }
            catch (VcsException e1) {
              refEx.set(e1);
            }
            finally {
              PerforceConnectionManager.getInstance(myProject).refreshConnections(settings);
            }
          }
        }, PerforceBundle.message("dialog.title.test.connection"), false, myProject);
        if (!refEx.isNull()) {
          Messages.showErrorDialog(PerforceBundle.message("message.text.connection.failed", refEx.get().getLocalizedMessage()),
                                   PerforceBundle.message("dialog.title.test.connection"));
        }
        else {
          Messages.showInfoMessage(PerforceBundle.message("message.text.connection.successful"), PerforceBundle.message("dialog.title.test.connection"));
        }
      }
    });

    myUseLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myLoginSilently.setEnabled(myUseLogin.isSelected());
      }
    });

    m_pathToExec.addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.exe"),
                                         PerforceBundle.message("dialog.description.path.to.p4.exe"),
                                         project,
                                         new FileChooserDescriptor(true, false, false, false, false, false));

    myP4VPathField.addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.exe"),
                                           PerforceBundle.message("dialog.description.path.to.p4v.exe"),
                                           project,
                                           new FileChooserDescriptor(true, false, false, false, false, false));
    reenableConnectPane();
  }

  private void testConnection(final P4Connection connection, final PerforceSettings settings) throws VcsException {
    PerforceRunner runner = new PerforceRunner(myProject, PerforceConnectionManager.getInstance(myProject), settings,
                                               PerforceManager.getInstance(myProject));
    final Map<String,List<String>> info = runner.getInfo(connection);
    final List<String> serverVersions = info.get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null) {
      throw new VcsException("Failed to find Perforce server version in 'p4 info' output");
    }
    checkClient(info, settings);

    final ServerVersion serverVersion = OutputMessageParser.parseServerVersion(serverVersions.get(0));
    if (!settings.useP4CONFIG && (serverVersion.getVersionYear() > 2004 || (serverVersion.getVersionYear() == 2004 && serverVersion.getVersionNum() >= 2))) {
      final String passwd = settings.getPasswd();
      if (passwd != null && passwd.length() != 0) {
        runner.performLogin(passwd, connection);
      }
    }
  }

  private static void checkClient(final Map<String,List<String>> info, final PerforceSettings settings) throws VcsException {
    final List<String> clientHostWrapper = info.get(PerforceRunner.CLIENT_HOST);
    if ((clientHostWrapper == null) || (clientHostWrapper.isEmpty())) {
      throw new VcsException("Failed to find client host in 'p4 info' output");
    }
    if (clientHostWrapper.get(0).contains(CLIENT_UNKNOWN)) {
      throw new VcsException("Client unknown: " + settings.client);
    }

    // compare client names
    final List<String> infoNameWrapper = info.get(PerforceRunner.CLIENT_NAME);
    if ((infoNameWrapper == null) || (infoNameWrapper.isEmpty())) {
      throw new VcsException("Failed to find client name in 'p4 info' output");
    }
    final String infoName = infoNameWrapper.get(0).trim();
    final String settingsName = settings.client.trim();
    if (! Comparing.equal(infoName, settingsName)) {
      throw new VcsException("Client unknown: 'p4 info' returns 'Client name: " + infoName + "'");
    }
  }

  private void reenableConnectPane() {
    boolean useP4Conf = m_useP4Conf.isSelected();
    m_port.setEnabled(!useP4Conf);
    m_client.setEnabled(!useP4Conf);
    m_user.setEnabled(!useP4Conf);
    m_passwd.setEnabled(!useP4Conf);
  }

  public void resetFrom(PerforceSettings settings) {
    m_useP4Conf.setSelected(settings.useP4CONFIG);
    m_port.setText(settings.port);
    m_client.setText(settings.client);
    m_user.setText(settings.user);
    m_passwd.setText(settings.getPasswd());
    m_showCmds.setSelected(settings.showCmds);
    m_pathToExec.setText(settings.pathToExec);
    myP4VPathField.setText(settings.PATH_TO_P4V);
    myOutputFileLabel.setText("'" + PerforceRunner.getDumpFile().getAbsolutePath() + "'");
    myShowBranchingHistory.setSelected(settings.SHOW_BRANCHES_HISTORY);
    myUseLogin.setSelected(settings.USE_LOGIN);
    myLoginSilently.setSelected(settings.LOGIN_SILENTLY);
    myServerTimeoutField.setText(Integer.toString(settings.SERVER_TIMEOUT/1000));
    myUsePerforceJobs.setSelected(settings.USE_PERFORCE_JOBS);
    myShowIntegratedChangelistsInCheckBox.setSelected(settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES);

    myCharset.removeAllItems();
    myCharset.addItem(PerforceSettings.CHARSET_NONE);
    myCharset.addItem(CHARSET_ISO8859_1);
    myCharset.addItem(CHARSET_ISO8859_15);
    myCharset.addItem(CHARSET_eucjp);
    myCharset.addItem(CHARSET_shiftjis);
    myCharset.addItem(CHARSET_winansi);
    myCharset.addItem(CHARSET_macosroman);
    myCharset.addItem(CHARSET_utf8);

    myIsEnabled.setSelected(settings.ENABLED);

    myCharset.setSelectedItem(settings.CHARSET);

    reenableConnectPane();
    myLoginSilently.setEnabled(myUseLogin.isSelected());
  }

  public void applyTo(PerforceSettings settings) {
    settings.useP4CONFIG = m_useP4Conf.isSelected();
    settings.port = m_port.getText();
    settings.client = m_client.getText();
    settings.user = m_user.getText();
    settings.setPasswd(new String(m_passwd.getPassword()));
    settings.showCmds = m_showCmds.isSelected();
    settings.pathToExec = m_pathToExec.getText();
    settings.PATH_TO_P4V = myP4VPathField.getText();
    settings.CHARSET = (String)myCharset.getSelectedItem();
    settings.SHOW_BRANCHES_HISTORY = myShowBranchingHistory.isSelected();
    settings.ENABLED = myIsEnabled.isSelected();
    settings.USE_LOGIN = myUseLogin.isSelected();
    settings.LOGIN_SILENTLY = myLoginSilently.isSelected();
    try {
      settings.SERVER_TIMEOUT = Integer.parseInt(myServerTimeoutField.getText()) * 1000;
    }
    catch(NumberFormatException ex) {
      // ignore
    }
    settings.USE_PERFORCE_JOBS = myUsePerforceJobs.isSelected();
    settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES = myShowIntegratedChangelistsInCheckBox.isSelected();
  }

  public boolean equalsToSettings(PerforceSettings settings) {
    if (settings.useP4CONFIG != m_useP4Conf.isSelected()) return false;
    if (settings.showCmds != m_showCmds.isSelected()) return false;
    if (settings.SHOW_BRANCHES_HISTORY != myShowBranchingHistory.isSelected()) return false;
    if (settings.USE_LOGIN != myUseLogin.isSelected()) return false;

    if (settings.ENABLED != myIsEnabled.isSelected()) return false;
    if (settings.LOGIN_SILENTLY != myLoginSilently.isSelected()) return false;
    if (!Integer.toString(settings.SERVER_TIMEOUT / 1000).equals(myServerTimeoutField.getText())) return false;

    if (!Comparing.equal(settings.port, m_port.getText().trim())) return false;
    if (!Comparing.equal(settings.client, m_client.getText().trim())) return false;
    if (!Comparing.equal(settings.user, m_user.getText().trim())) return false;
    if (!Comparing.equal(settings.getPasswd(), new String(m_passwd.getPassword()))) return false;
    if (!Comparing.equal(settings.pathToExec, m_pathToExec.getText().trim())) return false;
    if (!Comparing.equal(settings.PATH_TO_P4V, myP4VPathField.getText().trim())) return false;
    if (! Comparing.equal(settings.USE_PERFORCE_JOBS, myUsePerforceJobs.isSelected())) return false;
    if (! Comparing.equal(settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES, myShowIntegratedChangelistsInCheckBox.isSelected())) return false;
    return Comparing.equal(settings.CHARSET, myCharset.getSelectedItem());
  }

  public JComponent getPanel() {
    return myPanel;
  }
}
