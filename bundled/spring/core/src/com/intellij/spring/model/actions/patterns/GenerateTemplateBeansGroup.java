package com.intellij.spring.model.actions.patterns;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.patterns.aop.AopPatternsGroup;
import com.intellij.spring.model.actions.patterns.dataAccess.GenerateDataAccessPatternsGroup;
import com.intellij.spring.model.actions.patterns.factoryBeans.GenerateCommonBeansPatternsGroup;
import com.intellij.spring.model.actions.patterns.integration.GenerateIntegrationPatternsGroup;
import com.intellij.spring.model.actions.patterns.integration.GenerateSchedulersPatternsGroup;
import com.intellij.spring.model.actions.patterns.osgi.GenerateOsgiPatternsGroup;
import com.intellij.spring.model.actions.patterns.webflow.GenerateWebflowPatternsGroup;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GenerateTemplateBeansGroup extends AbstarctSpringConfigActionGroup {

  public GenerateTemplateBeansGroup() {
    super();
    add(new GenerateCommonBeansPatternsGroup());
    add(new GenerateDataAccessPatternsGroup());
    add(new GenerateIntegrationPatternsGroup());
    add(new GenerateSchedulersPatternsGroup());
    add(new AopPatternsGroup());
    add(new GenerateWebflowPatternsGroup());
    add(new GenerateOsgiPatternsGroup());
  }

  protected String getDescription() {
    return SpringBundle.message("spring.template.beans");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.SPRING_BEANS_ICON;
  }
}