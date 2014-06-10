/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.utils.ui;

import com.advancedtools.webservices.WSBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author maxim
 * Date: 19.12.2004
 * Time: 16:36:45
 */
public abstract class MyDialogWrapper extends DialogWrapper {
  protected static Logger LOG = Logger.getInstance("webservicesplugin.dialogwrapper");
  protected Project myProject;
  protected Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
  private Runnable myOkAction;

  private PsiClass myClass;
  protected static final String NO_CLASS_IN_SELECTED_TEXT_EDITOR = WSBundle.message("no.class.in.selected.text.editor.error.message");
  private PsiFile myFile;
  private boolean myDisposed;

  public MyDialogWrapper(Project project) {
    super(project,true);
    myProject = project;
    setModal(false);

    final ProjectManagerListener myProjectManagerListener = new ProjectManagerListener() {
      public void projectOpened(Project project) {
      }

      public boolean canCloseProject(Project project) {
        return true;
      }

      public void projectClosed(Project project) {
      }

      public void projectClosing(Project project) {
        if (project == myProject) dispose();
      }
    };

    ProjectManager.getInstance().addProjectManagerListener(myProjectManagerListener);
    Disposer.register(getDisposable(), new Disposable() {
      public void dispose() {
        ProjectManager.getInstance().removeProjectManagerListener(myProjectManagerListener);
      }
    });
  }

  public PsiClass getCurrentClass() {
    synchronized(this) {
      if (myClass != null && !myClass.isValid() && myFile != null) {
        final PsiClass[] psiClasses = ((PsiJavaFile) myFile).getClasses();
        myClass = psiClasses.length > 0 ? psiClasses[0]:null;
      }
      return myClass;
    }
  }

  protected boolean hasClassNameInUI() { return true; }

  public void setCurrentClass(PsiClass aClass) {
    synchronized(this) {
      myClass = aClass;
      myFile = myClass != null ? myClass.getContainingFile():null;
      setClassNameToUI(aClass);
    }
  }

  public void setCurrentFile(PsiFile aFile) {
    synchronized(this) {
      myFile = aFile;
    }
  }

  private boolean myInInit;

  private void setClassNameToUI(final PsiClass aClass) {
    if (!hasClassNameInUI()) return;

    JComponent className = getClassName();
    if (className != null) {
      final String text = aClass != null ? getClassNameTextToSet(aClass) : "*UNDEFINED*";

      doSetClassNameText(className, text);
    } else if (!myInInit) {
      myInInit = true;

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          setClassNameToUI(aClass);
        }
      });
    } else {
      int a = 1;
    }
  }

  protected void doSetClassNameText(JComponent className, String text) {
    if (className instanceof JLabel) ((JLabel)className).setText(text);
    else if (className instanceof JTextField) {
      ((JTextField)className).setText(text);
    } else {
      throw new UnsupportedOperationException();
    }
    validate();
  }

  protected String getClassNameTextToSet(PsiClass aClass) {
    return aClass.getName();
  }

  protected JComponent getClassName() {
    return null;
  }

  protected void startTrackingCurrentClassOrFile() {
    FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {

      public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
        evaluateClass(virtualFile);
      }

      public void fileClosed(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
        setCurrentClass(null);
        setCurrentFile(null);
        initiateValidation();
      }

      public void selectionChanged(FileEditorManagerEvent fileEditorManagerEvent) {
        evaluateClass(fileEditorManagerEvent.getNewFile());
      }
    };

    FileEditorManager.getInstance(myProject).addFileEditorManagerListener(fileEditorManagerListener, getDisposable());
  }

  protected void init() {
    super.init();
    if (getStatusTextField() != null) getStatusTextField().setDisplayedMnemonic('S');
    initiateValidation();
  }

  protected Action[] createActions() {
    Action[] actions = super.createActions();
    return new Action[] { actions[0], actions[1], getHelpAction() };
  }

  protected void doHelpAction() {
    final String helpId = getHelpId();
    HelpManager.getInstance().invokeHelp("webservices." + helpId.substring(0, helpId.lastIndexOf('.')));
  }

  public void doInitFor(JLabel textComponent, JComponent component, char mnemonic) {
    textComponent.setLabelFor(component);
    //textComponent.setFocusable(false);
    textComponent.setDisplayedMnemonic(mnemonic);

    if (component instanceof JTextField) {
      ((JTextField)component).getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          initiateValidation();
        }

        public void removeUpdate(DocumentEvent e) {
          initiateValidation();
        }

        public void changedUpdate(DocumentEvent e) {
          initiateValidation();
        }
      });
    } else if (component instanceof JComboBox) {
      JComboBox jComboBox = ((JComboBox) component);

      jComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          initiateValidation();
        }
      });

      if (jComboBox.isEditable()) {
        jComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
          public void keyTyped(KeyEvent e) {
            initiateValidation();
          }
        });
      }
    }
  }

  private void initiateValidation() { initiateValidation(500); }

  public abstract static class ValidationData {
    public final void acquire() {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            doAcquire();
          }
        });
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }

    protected abstract void doAcquire();
  }

  public class ValidationResult {
    public final JComponent component;
    public final String message;

    public ValidationResult(String _message, JComponent _component) {
      this(_message,_component, -1);
    }

    public ValidationResult(String _message, JComponent _component, int validationDelta) {
      message = _message;
      component = _component;
      if (validationDelta != -1) initiateValidation(validationDelta);
    }
  }

  protected abstract ValidationResult doValidate(ValidationData _data);
  protected abstract ValidationData createValidationData();
  protected abstract JLabel getStatusTextField();
  protected abstract JLabel getStatusField();

  public void initiateValidation(int delay) {
    if (myDisposed) return;
    myAlarm.cancelAllRequests();
    myAlarm.addRequest(
      new Runnable() {
        public void run() {
          final ValidationData validationData = createValidationData();
          if (validationData != null) validationData.acquire();
          ValidationResult result = ApplicationManager.getApplication().runReadAction(new Computable<ValidationResult>() {
            public ValidationResult compute() {
              return doValidate(validationData);
            }
          });
          reportProblem(result);
        }
      },
      delay
    );

    if (SwingUtilities.isEventDispatchThread()) {
      getOKAction().setEnabled(false);
    } else {
      try {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            getOKAction().setEnabled(false);
          }
        });
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  protected void reportProblem(ValidationResult result) {
    if (result == null) clearProblems();
    else reportProblem(result.message, result.component);
  }

  private void reportProblem(final String message, final JComponent comp) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        getStatusField().setText(message);
        getStatusField().setToolTipText(message);
        getStatusTextField().setLabelFor(comp);

        getOKAction().setEnabled(false);
        pack();
      }
    });
  }

  private void clearProblems() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        getStatusField().setText("");
        getStatusField().setToolTipText("");
        getStatusTextField().setLabelFor(null);
        getOKAction().setEnabled(true);
      }
    });
  }

  protected void dispose() {
    myAlarm.cancelAllRequests();
    super.dispose();
    myDisposed = true;
  }

  protected void doOKAction() {
    super.doOKAction();

    if (myOkAction != null) myOkAction.run();
  }

  @NotNull
  @NonNls
  protected abstract String getHelpId();

  public void setOkAction(Runnable myOkAction) {
    this.myOkAction = myOkAction;
  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    return virtualFile.getFileType() == StdFileTypes.JAVA;
  }

  protected void evaluateClass(VirtualFile newFile) {
    PsiClass newClass = null;
    PsiFile file = null;

    if (newFile != null && isAcceptableFile(newFile)) {
      file = PsiManager.getInstance(myProject).findFile(newFile);
      if (file instanceof PsiJavaFile) {
        PsiClass[] classes = ((PsiJavaFile) file).getClasses();
        if (classes.length > 0) newClass = classes[0];
      }
    }

    setCurrentClass(newClass);
    setCurrentFile(file);
    initiateValidation();
  }

  public void configureComboBox(JComboBox combo, List<String> lastValues) {
    combo.setModel(new DefaultComboBoxModel(lastValues.toArray(new String[lastValues.size()])));
    if (combo.getItemCount()!=0) {
      combo.setSelectedIndex(0);
      combo.getEditor().selectAll();
    }
  }

  public MyDialogWrapper.ValidationResult createValidationResult(String message, JComponent comp, int delta) {
    return new ValidationResult(message, comp, delta);
  }

  public Project getProject() {
    return myProject;
  }
}
