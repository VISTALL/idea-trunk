package org.jetbrains.plugins.groovy.intentions.style.parameterToEntry;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorComboBoxRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.StringComboboxEditor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.intentions.GroovyIntentionsBundle;
import org.jetbrains.plugins.groovy.refactoring.GroovyNamesUtil;
import org.jetbrains.plugins.groovy.settings.GroovyApplicationSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.event.*;
import java.util.EventListener;

public class GroovyMapParameterDialog extends DialogWrapper {
  private JPanel contentPane;
  private ComboBox myNameComboBox;
  private JLabel myNameLabel;
  private JCheckBox myCbTypeSpec;
  private JCheckBox myCreateNew;
  private final Project myProject;
  private final EventListenerList myListenerList = new EventListenerList();

  public GroovyMapParameterDialog(Project project, final String[] possibleNames, final boolean createNew) {
    super(true);
    myProject = project;
    setUpDialog(possibleNames, createNew);
    init();
  }

  private void setUpDialog(final String[] possibleNames, boolean createNew) {
    setTitle(GroovyIntentionsBundle.message("convert.param.to.map.entry"));

    if (GroovyApplicationSettings.getInstance().CONVERT_PARAM_CREATE_NEW_PARAM != null) {
      myCreateNew.setSelected(createNew = GroovyApplicationSettings.getInstance().CONVERT_PARAM_CREATE_NEW_PARAM.booleanValue());
    } else {
      myCreateNew.setSelected(createNew);
    }

    myNameLabel.setLabelFor(myNameComboBox);
    myCbTypeSpec.setMnemonic(KeyEvent.VK_T);
    myCbTypeSpec.setFocusable(false);
    setUpNameComboBox(possibleNames);
    setModal(true);
    if (GroovyApplicationSettings.getInstance().CONVERT_PARAM_SPECIFY_MAP_TYPE != null) {
      myCbTypeSpec.setSelected(GroovyApplicationSettings.getInstance().CONVERT_PARAM_SPECIFY_MAP_TYPE.booleanValue());
    } else {
      myCbTypeSpec.setSelected(true);
    }

    myCbTypeSpec.setEnabled(createNew);
    myNameComboBox.setEnabled(createNew);
    myNameLabel.setEnabled(createNew);

    myCreateNew.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        final boolean flag = myCreateNew.isSelected();
        myCbTypeSpec.setEnabled(flag);
        myNameComboBox.setEnabled(flag);
        myNameLabel.setEnabled(flag);
      }
    });
  }

  public boolean createNewFirst() {
    return myCreateNew.isSelected();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  public boolean specifyTypeExplicitly() {
    return myCbTypeSpec.isSelected();
  }

  @Override
  protected void doOKAction() {
    if (myCbTypeSpec.isEnabled()) {
      GroovyApplicationSettings.getInstance().CONVERT_PARAM_SPECIFY_MAP_TYPE = myCbTypeSpec.isSelected();
    }
    GroovyApplicationSettings.getInstance().CONVERT_PARAM_CREATE_NEW_PARAM = myCreateNew.isSelected();
    super.doOKAction();
  }

  @Override
  public void doCancelAction() {
    super.doCancelAction();
  }

  protected JComponent createCenterPanel() {
    return contentPane;
  }

  public JComponent getContentPane() {
    return contentPane;
  }

  @Nullable
  protected String getEnteredName() {
    if (myNameComboBox.getEditor().getItem() instanceof String && ((String)myNameComboBox.getEditor().getItem()).length() > 0) {
      return (String)myNameComboBox.getEditor().getItem();
    } else {
      return null;
    }
  }

  private void setUpNameComboBox(String[] possibleNames) {

    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, GroovyFileType.GROOVY_FILE_TYPE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);
    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));

    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);
    myListenerList.add(DataChangedListener.class, new DataChangedListener());

    myNameComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fireNameDataChanged();
      }
    });

    ((EditorTextField)myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
      public void beforeDocumentChange(DocumentEvent event) {
      }

      public void documentChanged(DocumentEvent event) {
        fireNameDataChanged();
      }
    });

    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myNameComboBox.requestFocus();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

    for (String possibleName : possibleNames) {
      myNameComboBox.addItem(possibleName);
    }
  }

  private void fireNameDataChanged() {
    Object[] list = myListenerList.getListenerList();
    for (Object aList : list) {
      if (aList instanceof DataChangedListener) {
        ((DataChangedListener)aList).dataChanged();
      }
    }
  }

  class DataChangedListener implements EventListener {
    void dataChanged() {
      updateOkStatus();
    }
  }

  private void updateOkStatus() {
    String text = getEnteredName();
    setOKActionEnabled(GroovyNamesUtil.isIdentifier(text));
  }

}
