package com.intellij.spring.model.actions.patterns.aop;

import com.intellij.aop.jam.AopConstants;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;

public class AopPatternsGroup extends DefaultActionGroup {

  public AopPatternsGroup() {
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.aop.auto.proxy.creator"), "aop-auto-proxy-creator"),
      AopConstants.INTRODUCTION_ICON));
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.aop.auto.proxy"), "aop-auto-proxy"),
      AopConstants.INTRODUCTION_ICON));

    setPopup(true);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(SpringBundle.message("spring.patterns.aop.group.name"));
    e.getPresentation().setIcon(AopConstants.INTRODUCTION_ICON);
  }

}