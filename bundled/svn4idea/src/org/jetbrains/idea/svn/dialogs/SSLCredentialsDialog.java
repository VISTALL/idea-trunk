package org.jetbrains.idea.svn.dialogs;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.idea.svn.SvnBundle;

import javax.swing.*;
import java.awt.*;

public class SSLCredentialsDialog extends DialogWrapper {
  private JPanel myPanel;
  private TextFieldWithBrowseButton myCertificatePath;
  private JPasswordField myCertificatePassword;
  private JCheckBox mySaveAuth;

  public SSLCredentialsDialog(final Project project, final String authRealm, final boolean authSaveAllowed) {
    super(project, true);
    setResizable(true);
    initUI(authRealm, authSaveAllowed);
    init();
  }

  protected String getDimensionServiceKey() {
    return "svn.sslCredentialsDialog";
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  private void initUI(final String authRealm, final boolean authSaveAllowed) {
    myPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints gb =
      new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
    gb.fill = GridBagConstraints.HORIZONTAL;

    gb.gridwidth = 2;
    gb.weightx = 0;
    JLabel label = new JLabel(SvnBundle.message("label.auth.authentication.realm", authRealm));
    myPanel.add(label, gb);

    ++ gb.gridy;
    gb.gridwidth = 1;
    gb.weightx = 0;
    final JLabel certificatePath = new JLabel(SvnBundle.message("label.ssl.certificate.path"));
    myPanel.add(certificatePath, gb);

    myCertificatePath = new TextFieldWithBrowseButton();

    myCertificatePath.addBrowseFolderListener(
        SvnBundle.message("dialog.edit.http.proxies.settings.dialog.select.ssl.client.certificate.path.title"),
        null, null, new FileChooserDescriptor(true, false, false, false, false, false));

    gb.weightx = 1;
    ++ gb.gridx;
    myPanel.add(myCertificatePath, gb);

    gb.gridx = 0;
    gb.weightx = 0;
    ++ gb.gridy;
    final JLabel certificatePassword = new JLabel(SvnBundle.message("label.ssl.certificate.password"));
    myPanel.add(certificatePassword, gb);

    ++ gb.gridx;
    gb.weightx = 1;
    myCertificatePassword = new JPasswordField();
    myPanel.add(myCertificatePassword, gb);

    gb.gridx = 0;
    ++ gb.gridy;
    gb.weightx = 0;
    mySaveAuth = new JCheckBox(SvnBundle.message("checkbox.ssl.keep.for.current.session"), authSaveAllowed);
    myPanel.add(mySaveAuth, gb);
  }

  public String getCertificatePath() {
    return myCertificatePath.getText();
  }

  public char[] getCertificatePassword() {
    return myCertificatePassword.getPassword();
  }

  public boolean getSaveAuth() {
    return mySaveAuth.isSelected();
  }

  @Override
  protected JComponent createNorthPanel() {
    return myPanel;
  }

  protected JComponent createCenterPanel() {
    return null;
  }
}
