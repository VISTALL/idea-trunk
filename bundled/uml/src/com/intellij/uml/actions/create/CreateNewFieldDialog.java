/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions.create;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class CreateNewFieldDialog extends DialogWrapper implements Disposable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uml.actions.create.CreateNewFieldDialog");
  private JPanel contentPane;

  private JCheckBox myFinal;
  private JComboBox myVisibility;
  private JCheckBox myStatic;
  private JPanel myTypePanel;
  private JPanel myNamePanel;
  private JPanel myInitializerPanel;
  private JLabel myPreviewText;
  private JPanel myPreview;
  private final EditorTextField myName;
  private final EditorTextField myType;
  private final EditorTextField myInitializer;
  private final PsiClass myClass;
  private final PsiField myField;
  final PsiTypeCodeFragment myTypeCodeFragment;
  PsiExpressionCodeFragment myInitializerCode;

  public CreateNewFieldDialog(PsiClass psiClass) throws IncorrectOperationException {
    super(psiClass.getProject(), true);    
    myClass = psiClass;
    final Project project = myClass.getProject();
    setTitle(UmlBundle.message("add.new.field.title", psiClass.getName()));

    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
    myField =  factory.createFieldFromText("private String field = null;", myClass);
    final PsiDocumentManager dm = PsiDocumentManager.getInstance(project);
    myTypeCodeFragment = factory.createTypeCodeFragment(myField.getTypeElement().getText(), myField.getTypeElement(), true, true);
    final Document typeDoc = dm.getDocument(myTypeCodeFragment);
    myType = new EditorTextField(typeDoc, project, StdFileTypes.JAVA);
    myTypePanel.add(myType);

    myName = new EditorTextField();
    myNamePanel.add(myName);

    myInitializerCode =
      factory.createExpressionCodeFragment(myField.getInitializer().getText(), myField, myField.getType(), true);

    myInitializer = new EditorTextField(dm.getDocument(myInitializerCode), project, StdFileTypes.JAVA);
    myInitializerPanel.add(myInitializer);
    init();
    myType.setText("");
    myInitializer.setText("");
    setSize(500, 200);
    myPreview.setBorder(IdeBorderFactory.createTitledBorder(UmlBundle.message("preview")));
    if (psiClass.isInterface()) {
      myVisibility.setSelectedIndex(3);
      myVisibility.setEnabled(false);
      myStatic.setSelected(true);
      myStatic.setEnabled(false);
      myFinal.setSelected(true);
      myFinal.setEnabled(false);
    }

    DocumentListener docListener = new DocumentAdapter() {
      @Override
      public void documentChanged(final DocumentEvent e) {
        onChange();
      }
    };
    myType.addDocumentListener(docListener);
    myName.addDocumentListener(docListener);
    myInitializer.addDocumentListener(docListener);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        onChange();
      }
    };
    myVisibility.addActionListener(actionListener);
    myStatic.addActionListener(actionListener);
    myFinal.addActionListener(actionListener);

    myType.addDocumentListener(new DocumentListener() {
      PsiType oldType = null;
      public void beforeDocumentChange(final DocumentEvent event) {
      }

      public void documentChanged(final DocumentEvent event) {
        try {
          final PsiType type = myTypeCodeFragment.getType();
          if (!type.equals(oldType)) {
            myInitializerCode.setExpectedType(type);
            final Document doc = PsiDocumentManager.getInstance(myClass.getProject()).getDocument(myInitializerCode);
            myInitializer.setNewDocumentAndFileType(StdFileTypes.JAVA, doc);
            oldType = type;
          }
        } catch (Exception e1) {//          
        }
      }
    });
    setModal(true);
  }

  private void onChange() {
    StringBuilder expr = new StringBuilder();
    if (!myClass.isInterface()) {
      expr.append(getModifier()).append(" ");
    }
    expr.append(myType.getText().trim()).append(" ").append(myName.getText().trim());
    String init = myInitializer.getText().trim();
    if (init.length() > 0) {
      if (!init.startsWith("=")) expr.append("=");
      expr.append(init);
    }
    expr.append(";");
    myPreviewText.setText(expr.toString());
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myType;
  }

  private void onOK() {
    String msg = checkForm();
    if (msg != null) {
      Messages.showErrorDialog(myClass.getProject(), msg, "Error");
      return;
    }
    final PsiElementFactory factory = JavaPsiFacade.getInstance(myClass.getProject()).getElementFactory();
    if (myClass.isInterface() && myInitializer.getText().trim().length() == 0) {
      Messages.showErrorDialog(myClass.getProject(),
                               UmlBundle.message("constant.in.interface.must.have.initializer"),
                               UmlBundle.message("initializer.is.empty"));
      myInitializer.requestFocus();
      return;
    }
    Runnable create = new Runnable() {
      public void run() {
        try {
          myField.setName(myName.getText().trim());
          final PsiTypeElement typeElement = myField.getTypeElement();
          typeElement.replace(factory.createTypeElement(myTypeCodeFragment.getType()));
          myField.setInitializer(myInitializerCode.getExpression());
          final PsiModifierList modifierList = myField.getModifierList();
          modifierList.setModifierProperty(PsiModifier.PRIVATE, false);
          if (!myClass.isInterface()) {
            PsiUtil.setModifierProperty(myField, MODIFIERS[myVisibility.getSelectedIndex()], true);
            if (myStatic.isSelected()) {
              PsiUtil.setModifierProperty(myField, PsiModifier.STATIC, true);
            }
            if (myFinal.isSelected()) {
              PsiUtil.setModifierProperty(myField, PsiModifier.FINAL, true);
            }
          }

          PsiField[] fields = myClass.getFields();
          myClass.addAfter(myField, fields.length == 0 ? myClass.getLBrace() : fields[fields.length - 1]);          
        } catch (Exception e1) {
          throw new IllegalArgumentException();
        }
      }
    };
    try {
      UmlPsiUtil.runWriteActionInCommandProcessor(create);
    } catch(Exception e) {
      Messages.showErrorDialog(myClass.getProject(), "Expression '" + myPreviewText.getText() + "' is invalid", "Error");
      return;
    }

    UmlPsiUtil.reformat(myClass);
    Disposer.dispose(this);
  }

  private String getModifier() {
    String modifier = MODIFIERS[myVisibility.getSelectedIndex()];
    if (modifier.equals(PsiModifier.PACKAGE_LOCAL)) {
      modifier = "";
    }
    if (myStatic.isSelected()) {
      modifier += " " + PsiModifier.STATIC;
    }

    if (myFinal.isSelected()) {
      modifier += " " + PsiModifier.FINAL;
    }
    return modifier;
  }

  private static final String[] MODIFIERS = {PsiModifier.PRIVATE, PsiModifier.PACKAGE_LOCAL, PsiModifier.PROTECTED, PsiModifier.PUBLIC};

  private void onCancel() {
    Disposer.dispose(this);
  }

  public void dispose() {
    super.dispose();
  }

  protected JComponent createCenterPanel() {
    return contentPane;
  }

  @Override
  protected void doOKAction() {
    onOK();
  }

  @Override
  public void doCancelAction() {
    onCancel();
  }

  @Nullable
  @NonNls
  private String checkForm() {
    if (myType.getText().trim().length() == 0) {
      return "Type is empty";
    }

    if (myName.getText().trim().length() == 0) {
      return "Name is empty";
    }

    return null;
  }
}
