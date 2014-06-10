package com.intellij.spring.perspectives.graph.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.actions.AbstractGraphToggleAction;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.perspectives.graph.SpringBeanDependencyInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractSpringGraphToggleAction extends AbstractGraphToggleAction {
  private GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> myBuilder;

 public AbstractSpringGraphToggleAction(final Icon icon) {
    super(icon);
  }

  public AbstractSpringGraphToggleAction(final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder, final Icon icon) {
    super(builder.getGraph(), icon);
    myBuilder = builder;
  }

   @Nullable
   protected GraphBuilder getBuilder(final AnActionEvent e) {
    return myBuilder == null ? super.getBuilder(e) : myBuilder;
  }
}
