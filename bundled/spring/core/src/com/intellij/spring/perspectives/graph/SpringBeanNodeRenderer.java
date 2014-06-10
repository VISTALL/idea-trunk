/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.builder.renderer.GradientFilledPanel;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SpringBeanNodeRenderer extends AbstractColoredNodeCellRenderer {

  GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> myBuilder;
  private final Color myBackgroundColor = new Color(252, 250, 209);
  private final Color myCaptionBackgroundColor = new Color(215, 213, 172);

  public SpringBeanNodeRenderer(@NotNull GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder,
                                ModificationTracker modificationTracker) {
    super(modificationTracker);
    myBuilder = builder;
  }

  public void tuneNode(final NodeRealizer realizer, final JPanel wrapper) {
    wrapper.removeAll();

    final Node node = realizer.getNode();
    final SpringBaseBeanPointer pointer = myBuilder.getNodeObject(node);
    if (pointer != null && pointer.isValid()) {
      final CommonSpringBean springBean = pointer.getSpringBean();
      if (!springBean.isValid()) return;

      JLabel nameLabel = new JLabel(getNodeTitle(pointer), pointer.getBeanIcon(), JLabel.HORIZONTAL);

      nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
      nameLabel.setBorder(IdeBorderFactory.createEmptyBorder(3, 3, 3, 3));
      nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

      GradientFilledPanel namePanel = new GradientFilledPanel(myCaptionBackgroundColor);
      namePanel.setLayout(new BorderLayout());
      namePanel.add(nameLabel, BorderLayout.CENTER);
      namePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
      nameLabel.setForeground(Color.BLACK);
      wrapper.add(namePanel, BorderLayout.NORTH);

      final JPanel propertiesPanel = new JPanel(new GridBagLayout());
      propertiesPanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 5, 2, 5));
      propertiesPanel.setBackground(myBackgroundColor);

      if (isGenerateProperties() && springBean instanceof DomSpringBean) {
        final List<SpringPropertyDefinition> properties = SpringUtils.getProperties(springBean);
        if (!properties.isEmpty()) {
          for (SpringPropertyDefinition property : properties) {
            final String propertyName = property.getPropertyName();
            if (propertyName != null) {
              final JLabel propertyNameLabel = new JLabel(propertyName, SpringIcons.SPRING_BEAN_PROPERTY_ICON, JLabel.HORIZONTAL);
              PsiType propertyType = null;

              if (property instanceof SpringProperty) {
                final SpringProperty springProperty = (SpringProperty)property;
                final List<BeanProperty> value = springProperty.getName().getValue();
                if (value != null && !value.isEmpty()) {
                  propertyType = value.get(0).getPropertyType();
                }
              }
              if (propertyType == null) {
                PsiType[] types = property.getTypesByValue();
                if (types != null && types.length > 0) {
                  propertyType = types[0];
                }
              }
              final JLabel typeLabel = new JLabel(propertyType == null ? "" : propertyType.getPresentableText());
              propertyNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
              typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

              propertiesPanel.add(propertyNameLabel,
                                  new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.LINE_START,
                                                         GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
              propertiesPanel.add(typeLabel, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.LINE_END,
                                                                    GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
            }
          }
          Dimension preferredSize = propertiesPanel.getPreferredSize();
          propertiesPanel.setPreferredSize(new Dimension((int)preferredSize.getWidth() + 20, (int)preferredSize.getHeight()));
        }
        else {
          Dimension preferredSize = nameLabel.getPreferredSize();
          nameLabel.setPreferredSize(new Dimension((int)preferredSize.getWidth() + 25, (int)preferredSize.getHeight()));
        }
      }
      wrapper.add(propertiesPanel, BorderLayout.CENTER);
    }
  }

  protected String getNodeTitle(SpringBaseBeanPointer pointer) {
    return myBuilder.getNodeName(pointer);
  }

  protected boolean isGenerateProperties() {
    return true;
  }

  protected int getSelectionBorderWidth() {
    return 2;
  }
}
