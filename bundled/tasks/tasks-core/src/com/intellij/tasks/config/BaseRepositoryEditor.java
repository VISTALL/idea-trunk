package com.intellij.tasks.config;

import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.ui.DocumentAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Dmitry Avdeev
 */
public abstract class BaseRepositoryEditor implements UnnamedConfigurable {

  protected JTextField myURLText;
  protected JTextField myUserNameText;
  protected JCheckBox myShareURL;
  protected JPasswordField myPasswordText;

  private JButton myTestButton;
  private JPanel myPanel;
  private boolean myApplying;

  public BaseRepositoryEditor(final Project project, final TaskRepository repository) {
    myTestButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TaskManager.getManager(project).testConnection(repository);
      }
    });

    installListener(myURLText);
    installListener(myUserNameText);
    installListener(myPasswordText);
    myShareURL.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doApply();
      }
    });
  }

  protected void installListener(JTextField textField) {
    textField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        doApply();
      }
    });
  }

  private void doApply() {
    if (!myApplying) {
      try {
        myApplying = true;
        apply();
      }
      finally {
        myApplying = false;
      }
    }
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public final void reset() {
    try {
      myApplying = true;
      doReset();
    }
    finally {
      myApplying = false;
    }
  }

  protected abstract void doReset();

  public abstract void apply();

  public boolean isModified() {
    return false;
  }

  public void disposeUIResources() {
  }
}
