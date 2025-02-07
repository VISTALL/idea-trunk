package org.jetbrains.idea.maven.wizards;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.idea.maven.indices.ArchetypeInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MavenAddArchetypeDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JTextField myGroupIdField;
  private JTextField myArtifactIdField;
  private JTextField myVersionField;
  private JTextField myRepositoryField;

  public MavenAddArchetypeDialog(Component parent) {
    super(parent, false);
    setTitle("Add archetype");

    init();

    DocumentAdapter l =new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        doValidateInput();
      }
    };

    myGroupIdField.getDocument().addDocumentListener(l);
    myArtifactIdField.getDocument().addDocumentListener(l);
    myVersionField.getDocument().addDocumentListener(l);
    myRepositoryField.getDocument().addDocumentListener(l);

    doValidateInput();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myGroupIdField;
  }

  private void doValidateInput() {
    List<String> errors = new ArrayList<String>();
    if (StringUtil.isEmptyOrSpaces(myGroupIdField.getText())) errors.add("GroupId");
    if (StringUtil.isEmptyOrSpaces(myArtifactIdField.getText())) errors.add("ArtifactId");
    if (StringUtil.isEmptyOrSpaces(myVersionField.getText())) errors.add("Version");

    if (errors.isEmpty()) {
      setErrorText(null);
      getOKAction().setEnabled(true);
      return;
    }
    String message = "Please specify " + StringUtil.join(errors, ", ");
    setErrorText(message);
    getOKAction().setEnabled(false);
    getRootPane().revalidate();
  }

  public ArchetypeInfo getArchetype() {
    return new ArchetypeInfo(myGroupIdField.getText(),
                             myArtifactIdField.getText(),
                             myVersionField.getText(),
                             myRepositoryField.getText(),
                             null);
  }
}
