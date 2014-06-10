package com.intellij.spring.model.actions.patterns.frameworks;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.patterns.AbstarctSpringConfigActionGroup;
import com.intellij.spring.model.actions.patterns.PatternIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FrameworkIntegrationActionGroup extends AbstarctSpringConfigActionGroup {

  public FrameworkIntegrationActionGroup() {
    super();
    add(new AddHibernateAction());
    add(new AddJdoAction());
    add(new AddToplinkAction());
    add(new AddIbatisAction());

    addSeparator();

    add(new AddWebflowAction());

    addSeparator();

    add(new AddOpenSymphonyTimerAction());
    add(new AddJdkTimerAction());
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.SPRING_PATTERNS_ICON;
  }
}