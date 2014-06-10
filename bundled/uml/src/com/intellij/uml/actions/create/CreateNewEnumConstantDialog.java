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
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlPsiUtil;

import javax.swing.*;
import java.awt.event.*;

public class CreateNewEnumConstantDialog extends JDialog implements Disposable {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextArea myEnumConstants;
  private final PsiClass myClass;

  public CreateNewEnumConstantDialog(PsiClass psiClass) {
    myClass = psiClass;
    setContentPane(contentPane);
    setTitle(UmlBundle.message("add.new.enum.constant.title", psiClass.getName()));
    setModal(true);
    setSize(300, 200);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK() {
    final PsiElementFactory factory = JavaPsiFacade.getInstance(myClass.getProject()).getElementFactory();

    Runnable create = new Runnable() {
      public void run() {
        final String[] names = myEnumConstants.getText().split("\n");
        for (String name : names) {
          name = name.trim();
          if (name.length() != 0) {
            try {
              final PsiEnumConstant enumConstant = factory.createEnumConstantFromText(name, myClass);
              PsiField[] fields = myClass.getFields();
              myClass.addAfter(enumConstant, fields.length == 0 ? myClass.getLBrace() : fields[fields.length - 1]);
            } catch (Exception e) {//
            }
          }
        }
      }
    };
    try {
      UmlPsiUtil.runWriteActionInCommandProcessor(create);
    } catch(Exception e) {//
    }

    UmlPsiUtil.reformat(myClass);
    Disposer.dispose(this);
  }

  private void onCancel() {
    Disposer.dispose(this);
  }

}
