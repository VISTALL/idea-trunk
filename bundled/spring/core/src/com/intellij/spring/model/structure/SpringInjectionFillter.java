package com.intellij.spring.model.structure;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringInjectionFillter implements Filter {
  @NonNls public static final String ID = "SHOW_PROPERTIES";

  public boolean isVisible(TreeElement treeNode) {
    return !(treeNode instanceof SpringInjectionTreeElement);
  }

  @NotNull
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(SpringBundle.message("show.properties.and.constructor.args"), null, SpringIcons.SPRING_BEAN_PROPERTY_ICON);
  }

  @NotNull
  public String getName() {
    return ID;
  }

  public boolean isReverted() {
    return true;
  }
}
