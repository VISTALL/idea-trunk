package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.IdeBorderFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.event.*;
import java.awt.*;

public abstract class TextFieldAction extends AnAction implements CustomComponentAction {
  protected JTextField myField;
  private final String myDescription;
  private final Icon myIcon;

  protected TextFieldAction(String text, String description, Icon icon) {
    super(text, description, icon);
    myDescription = description;
    myIcon = icon;
    myField = new JTextField(20);
  }

  public JComponent createCustomComponent(Presentation presentation) {
    // honestly borrowed from SearchTextField
    
    final JPanel panel = new JPanel(new BorderLayout());
    final JLabel label = new JLabel(myIcon);
    label.setOpaque(true);
    label.setBackground(myField.getBackground());
    myField.setOpaque(true);
    panel.add(myField, BorderLayout.WEST);
    panel.add(label, BorderLayout.EAST);
    myField.setToolTipText(myDescription);
    label.setToolTipText(myDescription);
    final Border originalBorder;
    if (SystemInfo.isMac) {
      originalBorder = BorderFactory.createLoweredBevelBorder();
    }
    else {
      originalBorder = myField.getBorder();
    }

    panel.setBorder(new CompoundBorder(IdeBorderFactory.createEmptyBorder(4, 0, 4, 0), originalBorder));

    myField.setOpaque(true);
    myField.setBorder(IdeBorderFactory.createEmptyBorder(0, 5, 0, 5));

    label.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        actionPerformed(null);
      }
    });
    myField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        if ((KeyEvent.VK_ENTER == e.getKeyCode()) || ('\n' == e.getKeyChar())) {
          actionPerformed(null);
        }
      }
    });
    return panel;
  }
}
