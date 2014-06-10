package com.intellij.spring.model.actions;

import com.intellij.spring.SpringBundle;

public class GenerateSpringBeanConstructorDependencyAction extends GenerateSpringBeanDependencyAction {

  public GenerateSpringBeanConstructorDependencyAction() {
    super(new GenerateSpringBeanDependenciesActionHandler(false),
          SpringBundle.message("action.Spring.Beans.Generate.Constructor.Dependency.Action.text"));
  }
}
