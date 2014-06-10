package com.intellij.seam.graph.dnd;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.seam.resources.messages.PageflowBundle;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.Collections;
import java.util.List;

public class PageflowComponentNameDialog extends DialogWrapper {
  private JPanel myContentPane;
  private JTextField myName;
  private List<String> myExcludedNames;

  public PageflowComponentNameDialog() {
    this(Collections.<String>emptyList());
  }

  public PageflowComponentNameDialog(@NotNull List<String> excludedNames) {
    super(false);
    myExcludedNames = excludedNames;
    setModal(true);

    setTitle(PageflowBundle.message("seam.pageflow.new.pageflow.component.dialog.name"));

    myName.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        checkInput();
      }
    });

    getOKAction().setEnabled(false);
    init();
  }

  private void checkInput() {
    final String text = myName.getText().trim();
    getOKAction().setEnabled(text.length() > 0 && !myExcludedNames.contains(text));
  }

  public String getPageflowComponentName() {
    return myName.getText();
  }

  protected JComponent createCenterPanel() {
    return myContentPane;
  }
}
