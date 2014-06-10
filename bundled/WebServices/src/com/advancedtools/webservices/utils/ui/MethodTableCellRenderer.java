package com.advancedtools.webservices.utils.ui;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.PsiFormatUtil;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 17.04.2006
 * Time: 12:02:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class MethodTableCellRenderer extends DefaultTableCellRenderer {
  private Font myFont, myBoldFont;

  protected abstract String getProblemByMethod(PsiMethod method);

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    PsiMethod method = (PsiMethod) value;
    String s = PsiFormatUtil.formatMethod(
      method,
      PsiSubstitutor.EMPTY,
      PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_TYPE,
      PsiFormatUtil.SHOW_TYPE
    );

    Component tableCellRendererComponent = super.getTableCellRendererComponent(table, s, isSelected, hasFocus, row, column);
    String problem = getProblemByMethod(method);
    String toolTip;

    if (problem == null) {
      tableCellRendererComponent.setEnabled(true);
      if (myFont == null) myFont = tableCellRendererComponent.getFont();
      if (myBoldFont == null) myBoldFont = myFont.deriveFont(Font.BOLD);

      if (supportsDeployment()) {
        boolean deployed = getDeploymentStatus(method);
        tableCellRendererComponent.setFont( deployed ? myBoldFont:myFont );

        toolTip = deployed ? "Method deployed" : "Method not deployed";
      } else {
        toolTip = null;
      }
    } else {
      toolTip = problem;
      tableCellRendererComponent.setEnabled(false);
    }

    ((JComponent)tableCellRendererComponent).setToolTipText(toolTip);

    return tableCellRendererComponent;
  }

  protected abstract boolean getDeploymentStatus(PsiMethod method);
  protected abstract boolean supportsDeployment();
}
