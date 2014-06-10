package com.intellij.hibernate.view;

import com.intellij.util.JavaeeIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Gregory.Shrago
 */
public interface HibernateIcons {
  Icon HIBERNATE_ICON = IconLoader.getIcon("/resources/icons/addHibernateSupport.png");

  Icon SESSION_FACTORY_ICON = JavaeeIcons.PERSISTENCE_UNIT_ICON;

  Icon ABSTRACT_CLASS_ICON = JavaeeIcons.MAPPED_SUPERCLASS_ICON;
  Icon CLASS_ICON = JavaeeIcons.ENTITY_ICON;
  Icon COMPONENT_ICON = JavaeeIcons.EMBEDDABLE_ICON;

  Icon ID_ATTRIBUTE_ICON = JavaeeIcons.ID_ATTRIBUTE_ICON;
  Icon ATTRIBUTE_ICON = JavaeeIcons.ATTRIBUTE_ICON;
  Icon RELATIONSHIP_ICON = JavaeeIcons.RELATIONSHIP_ICON;
  Icon ID_RELATIONSHIP_ICON = JavaeeIcons.ID_RELATIONSHIP_ICON;

  Icon HIBERNATE_MAPPING_ICON = IconLoader.getIcon("/resources/icons/hibernateXml.png");
  Icon HIBERNATE_CONFIG_ICON = IconLoader.getIcon("/resources/icons/hibernateXml.png");
}
