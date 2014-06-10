/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface SpringIcons {
  Icon SPRING_ICON = IconLoader.getIcon("/resources/icons/spring.png");
  
  Icon SPRING_BEAN_ICON = IconLoader.getIcon("/resources/icons/springBean.png");
  Icon SPRING_JAVA_BEAN_ICON = IconLoader.getIcon("/resources/icons/springJavaBean.png");
  Icon SPRING_ALIAS_ICON = IconLoader.getIcon("/resources/icons/spring.png");
  Icon SPRING_BEANS_ICON = IconLoader.getIcon("/resources/icons/beans.png");
  Icon SPRING_BEAN_SCOPE_ICON = IconLoader.getIcon("/resources/icons/springBeanScope.png");
  Icon SPRING_BEAN_PROPERTY_ICON = IconLoader.getIcon("/resources/icons/springProperty.png");

  Icon PARENT_GUTTER = IconLoader.getIcon("/resources/icons/parentBeanGutter.png");
  Icon CHILD_GUTTER = IconLoader.getIcon("/resources/icons/childBeanGutter.png");

  Icon CONFIG_FILE = IconLoader.getIcon("/resources/icons/springConfig.png");
  Icon JAVA_CONFIG_FILE = IconLoader.getIcon("/resources/icons/springJavaConfig.png");

  Icon FILESET = IconLoader.getIcon("/resources/icons/fileSet.png");
  Icon DEPENDENCY = IconLoader.getIcon("/resources/icons/dependency.png");

  Icon SPRING_BEAN_INSTANTIATED_BY_FACORY = IconLoader.getIcon("/resources/icons/factoryMethodBean.png");
  Icon SPRING_BEAN_INSTANTIATED_BY_FACORY_METHOD = IconLoader.getIcon("/resources/icons/factoryMethodBean.png");
  Icon SPRING_DEPENDENCIES_GRAPH_GROUP_BEANS = IconLoader.getIcon("/resources/icons/groupBeans.png");
  Icon SPRING_DEPENDENCIES_GRAPH_SHOW_AUTOWIRED = IconLoader.getIcon("/resources/icons/showAutowiredDependencies.png");
}