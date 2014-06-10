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

package com.intellij.tasks.actions;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileChooser.ex.FileTextFieldImpl;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListScrollingUtil;
import com.intellij.ui.SortedListModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public abstract class SearchSupport<T> {

  private JTextComponent myTextField;
  private JBPopup myCurrentPopup;
  private JList myList = new JList();
  protected boolean myCancelled;

  private ActionListener myCancelAction = new ActionListener() {
    public void actionPerformed(final ActionEvent e) {
      if (myCurrentPopup != null) {
        myCancelled = true;
        hideCurrentPopup();
      }
    }
  };
  private T myResult;
  private final SortedListModel<T> myListModel;
  private boolean myAutoPopup;

  public SearchSupport(JTextComponent textField) {

    myTextField = textField;
    myTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        onTextChanged();
      }
    });

    myTextField.addKeyListener(new KeyAdapter() {
      public void keyPressed(final KeyEvent e) {
          processListSelection(e);
      }
    });
    myList.setVisibleRowCount(10);
    myListModel = new SortedListModel<T>(null);
    myList.setModel(myListModel);
  }

  public void setAutoPopup(boolean autoPopup) {
    myAutoPopup = autoPopup;
  }

  public void setListRenderer(ListCellRenderer renderer) {
    myList.setCellRenderer(renderer);
  }

  protected String getText() {
    return myTextField.getText();
  }

  protected void onTextChanged() {
    if (myCancelled) {
      return;
    }
    if (myResult != null && !myResult.toString().equals(myTextField.getText())) {
      myResult = null;
    }
    if (isPopupShowing() || myAutoPopup) {
      showPopup(false);
    } else {
      hideCurrentPopup();
    }
  }

  protected abstract List<T> getItems(String text);

  private void processListSelection(final KeyEvent e) {
    if (togglePopup(e)) return;

    if (!isPopupShowing()) return;

    final InputMap map = myTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    if (map != null) {
      final Object object = map.get(KeyStroke.getKeyStrokeForEvent(e));
      if (object instanceof Action) {
        final Action action = (Action)object;
        if (action.isEnabled()) {
          action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "action"));
          e.consume();
          return;
        }
      }
    }

    final Object action = getAction(e, myList);

    if ("selectNextRow".equals(action)) {
      if (ensureSelectionExists()) {
        ListScrollingUtil.moveDown(myList, e.getModifiersEx());
      }
    }
    else if ("selectPreviousRow".equals(action)) {
      ListScrollingUtil.moveUp(myList, e.getModifiersEx());
    }
    else if ("scrollDown".equals(action)) {
      ListScrollingUtil.movePageDown(myList);
    }
    else if ("scrollUp".equals(action)) {
      ListScrollingUtil.movePageUp(myList);
    }
    else if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) && e.getModifiers() == 0) {
      hideCurrentPopup();
      e.consume();
      myCancelled = true;
      processChosenFromCompletion();
    }
  }

  @Nullable
  public T getResult() {

    return myResult;
  }

  private boolean togglePopup(KeyEvent e) {
    final KeyStroke stroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
    final Object action = ((InputMap)UIManager.get("ComboBox.ancestorInputMap")).get(stroke);
    if ("selectNext".equals(action)) {
      if (!isPopupShowing() && myAutoPopup) {
        showPopup(true);
        return true;
      }
      else {
        return false;
      }
    }
    else if ("togglePopup".equals(action)) {
      if (isPopupShowing()) {
        hideCurrentPopup();
      }
      else {
        showPopup(true);
      }
      return true;
    }
    else {
      final Keymap active = KeymapManager.getInstance().getActiveKeymap();
      final String[] ids = active.getActionIds(stroke);
      if (ids.length > 0 && "CodeCompletion".equals(ids[0])) {
        showPopup(true);
      }
    }

    return false;
  }

  private void showPopup(boolean explicit) {

    myCancelled = false;

    List<T> list = getItems(myTextField.getText());
    myListModel.clear();
    myListModel.addAll(list);

    if (list.isEmpty()) {
      if (explicit) {
        showNoSuggestions();
      } else {
        hideCurrentPopup();
      }
      return;
    }

    ensureSelectionExists();
    
    myList.setPrototypeCellValue(null);
    if (isPopupShowing()) {
      Dimension size = myList.getPreferredSize();
      int cellHeight = myList.getCellRenderer().getListCellRendererComponent(myList, myList.getModel().getElementAt(0), 0, false, false)
        .getPreferredSize().height;
      int height = Math.min(size.height, Math.min(myList.getModel().getSize(), 10) * cellHeight) ;
      myCurrentPopup.setSize(new Dimension(size.width + 20, height + 12));
      return;
    }

    hideCurrentPopup();

    final PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(myList);
    builder.addListener(new JBPopupListener() {
      public void beforeShown(LightweightWindowEvent event) {
        myTextField
          .registerKeyboardAction(myCancelAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
      }

      public void onClosed(LightweightWindowEvent event) {
        myTextField.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
      }
    });
    myCurrentPopup =
      builder.setRequestFocus(false).setAutoSelectIfEmpty(false).setResizable(false).setCancelCalllback(new Computable<Boolean>() {
        public Boolean compute() {
          final int caret = myTextField.getCaretPosition();
          myTextField.setSelectionStart(caret);
          myTextField.setSelectionEnd(caret);
          myTextField.setFocusTraversalKeysEnabled(true);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              myTextField.requestFocus();
            }
          });
          return Boolean.TRUE;
        }
      }).setItemChoosenCallback(new Runnable() {
        public void run() {
          processChosenFromCompletion();
        }
      }).setCancelKeyEnabled(false).setAlpha(0.1f).setFocusOwners(new Component[]{myTextField}).
          createPopup();

     showPopup();
  }

  private void showPopup() {
    Point point = FileTextFieldImpl.getLocationForCaret(myTextField);
    myCurrentPopup.showInScreenCoordinates(myTextField, point);
  }

  private void showNoSuggestions() {
    hideCurrentPopup();
    final JLabel message = HintUtil.createErrorLabel(IdeBundle.message("file.chooser.completion.no.suggestions"));
    final ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(message, message);
    builder.setRequestFocus(false).setResizable(false).setAlpha(0.1f).
      setFocusOwners(new Component[] {myTextField});
    myCurrentPopup = builder.createPopup();
    showPopup();
  }


  private void processChosenFromCompletion() {
    myResult = (T)myList.getSelectedValue();
    if (myResult != null) {
      onItemChosen(myResult);
    }
    hideCurrentPopup();
  }

  protected void onItemChosen(T result) {    
    myTextField.setText(result.toString());
  }

  private void hideCurrentPopup() {
    if (myCurrentPopup != null) {
      myCurrentPopup.cancel();
      myCurrentPopup = null;
    }
  }

  private boolean ensureSelectionExists() {
    if (myList.getSelectedIndex() < 0 || myList.getSelectedIndex() >= myList.getModel().getSize()) {
      if (myList.getModel().getSize() >= 0) {
        myList.setSelectedIndex(0);
        return false;
      }
    }

    return true;
  }

  private boolean isPopupShowing() {
    return myCurrentPopup != null && myList != null && myList.isShowing();
  }

  private static Object getAction(final KeyEvent e, final JComponent comp) {
    final KeyStroke stroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
    return comp.getInputMap().get(stroke);
  }

}
