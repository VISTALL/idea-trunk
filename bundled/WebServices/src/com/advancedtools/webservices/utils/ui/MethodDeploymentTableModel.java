package com.advancedtools.webservices.utils.ui;

import com.intellij.psi.PsiMethod;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 17.04.2006
 * Time: 12:05:54
 * To change this template use File | Settings | File Templates.
 */
public class MethodDeploymentTableModel extends AbstractTableModel {
  private final Boolean[] skipDeploymentTable;
  private final List<PsiMethod> methodsAllowedForDeployment;
  private final List<PsiMethod> methodsNotAllowedForDeployment;

  public MethodDeploymentTableModel(List<PsiMethod> methodsAllowedForDeployment, List<PsiMethod> methodsNotAllowedForDeployment) {
    this.methodsAllowedForDeployment = methodsAllowedForDeployment;
    this.methodsNotAllowedForDeployment = methodsNotAllowedForDeployment;
    skipDeploymentTable = new Boolean[methodsAllowedForDeployment.size()];
  }

  public int getRowCount() {
    return methodsAllowedForDeployment.size() + methodsNotAllowedForDeployment.size();
  }

  public int getColumnCount() {
    return 2;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 1) {
      if (rowIndex < methodsAllowedForDeployment.size())
        return methodsAllowedForDeployment.get(rowIndex);
      return methodsNotAllowedForDeployment.get(rowIndex - methodsAllowedForDeployment.size());
    }

    if (rowIndex < methodsAllowedForDeployment.size()) {
      Boolean aBoolean = skipDeploymentTable[rowIndex];
      if (aBoolean == null) return Boolean.TRUE;
      return Boolean.valueOf(!aBoolean.booleanValue());
    } else {
      return Boolean.FALSE;
    }
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 0 && rowIndex < skipDeploymentTable.length) {
      skipDeploymentTable[rowIndex] = !Boolean.valueOf((Boolean) aValue);
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 0 && rowIndex < skipDeploymentTable.length;
  }

  public String getColumnName(int column) {
    if (column == 0) return "Add to deployment";
    return "Method to expose";
  }

  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 1) return PsiMethod.class;
    return Boolean.class;
  }

  public PsiMethod[] getSelectedMethods() {
    final List<PsiMethod> methods = new ArrayList<PsiMethod>(getRowCount());

    for (int i = 0; i < getRowCount(); ++i) {
      Boolean selected = (Boolean) getValueAt(i, 0);
      if (Boolean.TRUE.equals(selected)) {
        methods.add( (PsiMethod) getValueAt(i, 1));
      }
    }
    return methods.toArray(new PsiMethod[methods.size()]);
  }
}
