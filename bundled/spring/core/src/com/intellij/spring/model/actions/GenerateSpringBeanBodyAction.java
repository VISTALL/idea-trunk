package com.intellij.spring.model.actions;

import com.intellij.spring.SpringIcons;
import com.intellij.util.xml.ui.actions.generate.GenerateDomElementProvider;

import javax.swing.*;

public class GenerateSpringBeanBodyAction extends GenerateSpringDomElementAction {
  public GenerateSpringBeanBodyAction(final GenerateDomElementProvider provider) {
    this(provider, SpringIcons.SPRING_BEAN_PROPERTY_ICON);
  }

  public GenerateSpringBeanBodyAction(final GenerateDomElementProvider provider, Icon icon) {
    super(provider, icon);
  }
}
