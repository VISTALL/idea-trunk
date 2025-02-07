package com.intellij.seam.dependencies.beans;

import com.intellij.seam.SeamIcons;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public class  SeamDomComponentNodeInfo implements SeamComponentNodeInfo<SeamDomComponent> {
  private final SeamDomComponent myComponent;

  public SeamDomComponentNodeInfo(@NotNull final SeamDomComponent component) {
    myComponent = component;
  }

  @NonNls
  public String getName() {
    if (!myComponent.isValid()) return "";

    final String name = myComponent.getComponentName();

    return StringUtil.isEmptyOrSpaces(name) ? "Noname" : name;
  }

  public Icon getIcon() {
    return SeamIcons.SEAM_DOM_COMPONENT_ICON;
  }

  @NotNull
  public SeamDomComponent getIdentifyingElement() {
    return myComponent;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SeamDomComponentNodeInfo nodeInfo = (SeamDomComponentNodeInfo)o;

    if (myComponent != null && myComponent.isValid() && nodeInfo.myComponent.isValid() ? !myComponent.equals(nodeInfo.myComponent) : nodeInfo.myComponent != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (myComponent != null && myComponent.isValid() ? myComponent.hashCode() : 0);
  }
}
