/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby.refactoring.introduceVariable;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.tree.IElementType;
import com.intellij.refactoring.HelpID;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorComboBoxRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.StringComboboxEditor;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.TokenBNF;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.*;
import java.util.EventListener;
import java.util.Set;

public class RubyIntroduceVariableDialog extends DialogWrapper implements RubyIntroduceVariableSettings {

    private Project myProject;
    private final int myOccurrencesCount;
    private final IntroduceVariableValidator myValidator;
    private EventListenerList myListenerList = new EventListenerList();

    private static final String NAME = RBundle.message("refactoring.introduce.variable.dialog.title");

    private JPanel myContentPane;
    private ComboBox myNameComboBox;
    private JCheckBox myCheckBox;
    private JLabel myNameLabel;

    private static Set<String> RESWORDS;
    static {
        RESWORDS = new HashSet<String>();
        for (IElementType type : TokenBNF.kALL_RESWORDS.getTypes()) {
            RESWORDS.add(type.toString());
        }
    }
    public RubyIntroduceVariableDialog(@NotNull final Project project,
                                       final int occurrencesCount,
                                       @NotNull final IntroduceVariableValidator validator,
                                       final String[] possibleNames) {
        super(project, true);
        myProject = project;
        myOccurrencesCount = occurrencesCount;
        myValidator = validator;
        setUpNameComboBox(possibleNames);

        setModal(true);
        setTitle(NAME);
        init();
        setUpDialog();
        updateOkStatus();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return myContentPane;
    }

    @Nullable
    public String getName() {
        final Object s = myNameComboBox.getEditor().getItem();
        if (s instanceof String && ((String) s).length() > 0) {
            return (String) s;
        } else {
            return null;
        }
    }

    public boolean doReplaceAllOccurrences() {
        return myCheckBox.isSelected();
    }

    private void setUpDialog() {

        myCheckBox.setMnemonic(KeyEvent.VK_A);
        myNameLabel.setLabelFor(myNameComboBox);


        // Replace occurences
        if (myOccurrencesCount > 1) {
            myCheckBox.setSelected(false);
            myCheckBox.setEnabled(true);
            myCheckBox.setText(myCheckBox.getText() + " (" + myOccurrencesCount + " occurrences)");
        } else {
            myCheckBox.setSelected(false);
            myCheckBox.setEnabled(false);
        }
    }

    private void setUpNameComboBox(final String[] possibleNames) {
        final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, RubyFileType.RUBY);

        myNameComboBox.setEditor(comboEditor);
        myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));
        myNameComboBox.setEditable(true);
        myNameComboBox.setMaximumRowCount(8);
        myListenerList.add(DataChangedListener.class, new DataChangedListener());

        myNameComboBox.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        fireNameDataChanged();
                    }
                }
        );

        ((EditorTextField) myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
            public void beforeDocumentChange(DocumentEvent event) {
            }

            public void documentChanged(DocumentEvent event) {
                fireNameDataChanged();
            }
        }
        );

        myContentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myNameComboBox.requestFocus();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        for (String possibleName : possibleNames) {
            myNameComboBox.addItem(possibleName);
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return myNameComboBox;
    }

    protected Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    protected void doOKAction() {
        final String result = myValidator.check(this);
        if (result!=null){
            Messages.showErrorDialog(myContentPane, result, RBundle.message("refactoring.introduce.variable.dialog.title"));
            doCancelAction();
            return;
        }
        super.doOKAction();
    }

    protected void doHelpAction() {
        HelpManager.getInstance().invokeHelp(HelpID.INTRODUCE_VARIABLE);
    }

    class DataChangedListener implements EventListener {
        void dataChanged() {
            updateOkStatus();
        }
    }

    private void updateOkStatus() {
        String text = getName();
        setOKActionEnabled(checkName(text));
    }

    private boolean checkName(String text) {
        return text != null && TextUtil.isIdentifier(text) && !RESWORDS.contains(text);
    }

    private void fireNameDataChanged() {
        for (Object object : myListenerList.getListenerList()) {
            if (object instanceof DataChangedListener) {
                ((DataChangedListener) object).dataChanged();
            }
        }
    }

}
