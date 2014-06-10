package com.intellij.hibernate;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.ServiceManager;

/**
 * @author Gregory.Shrago
 */
public abstract class HibernateManager {

  public static HibernateManager getInstance(Project project) {
    return ServiceManager.getService(project, HibernateManager.class);
  }

  public abstract HibernateConvertersRegistry getConvertersRegistry();

}
