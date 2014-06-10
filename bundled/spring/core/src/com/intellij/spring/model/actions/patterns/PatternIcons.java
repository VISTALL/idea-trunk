package com.intellij.spring.model.actions.patterns;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.EmptyIcon;

import javax.swing.*;

public interface PatternIcons {
  Icon EMPTY_ICON = new EmptyIcon(18, 18);

  Icon SPRING_BEANS_ICON = IconLoader.getIcon("/resources/icons/patterns/beans.png");

  Icon SPRING_PATTERNS_ICON = IconLoader.getIcon("/resources/icons/patterns/patterns.png");

  Icon HIBERNATE_ICON = IconLoader.getIcon("/resources/icons/patterns/hibernate.png");

  Icon DATASOURCE_ICON = IconLoader.getIcon("/resources/icons/patterns/datasource.png");

  Icon JDO_ICON = IconLoader.getIcon("/resources/icons/patterns/jdo.png");

  Icon TOPLINK_ICON = IconLoader.getIcon("/resources/icons/patterns/toplink.png");

  Icon IBATIS_ICON = IconLoader.getIcon("/resources/icons/patterns/ibatis.png");

  Icon JPA_ICON = IconLoader.getIcon("/resources/icons/patterns/jpa.png");

  Icon TRANSACTION_MANAGER_ICON = IconLoader.getIcon("/resources/icons/patterns/transactionManager.png");

  Icon DATA_ACCESS_GROUP_ICON = IconLoader.getIcon("/resources/icons/patterns/dataAccess.png");
  Icon INTEGRATION_GROUP_ICON = IconLoader.getIcon("/resources/icons/patterns/integration.png");

  Icon FACTORY_BEAN_ICON = IconLoader.getIcon("/resources/icons/patterns/factoryBean.png");

  Icon SCHEDULER_ICON = IconLoader.getIcon("/resources/icons/patterns/scheduler.png");
  Icon EJB_ICON = IconLoader.getIcon("/resources/icons/patterns/ejb.png");
  Icon JDK_ICON = IconLoader.getIcon("/resources/icons/patterns/jdk.png");

  Icon SPRING_WEBFLOW_ICON = IconLoader.getIcon("/resources/icons/springBean.png");
}
