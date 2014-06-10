package com.intellij.spring.model.actions;

import com.intellij.spring.SpringBundle;

public class GenerateSpringBeanSetterDependencyAction extends GenerateSpringBeanDependencyAction {
  public GenerateSpringBeanSetterDependencyAction() {
    super(new GenerateSpringBeanDependenciesActionHandler(true),
          SpringBundle.message("action.Spring.Beans.Generate.Setter.Dependency.Action.text"));
  }
}
