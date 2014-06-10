package com.intellij.spring.model.actions.patterns.integration;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.actions.patterns.PatternIcons;

public class GenerateSchedulersPatternsGroup  extends DefaultActionGroup {

  public GenerateSchedulersPatternsGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message(
      "spring.patterns.integration.opensymphony.job.detail.bean"), "quartz-job-detail"), PatternIcons.SCHEDULER_ICON)) ;
    add(new GenerateSpringDomElementAction(new MethodInvokingFactoryBean(SpringBundle.message("spring.patterns.integration.opensymphony.method.invoking.factory.bean")) {
      protected String getClassName() {
        return "org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean";
      }
    }, PatternIcons.SCHEDULER_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.integration.opensymphony.simple.trigger"), "quartz-simple-trigger"), PatternIcons.SCHEDULER_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.integration.opensymphony.cron.trigger"), "quartz-cron-trigger"), PatternIcons.SCHEDULER_ICON)) ;

    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.integration.opensymphony.scheduler"), "quartz-scheduler-factory"), PatternIcons.SCHEDULER_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.integration.jdk.scheduled.timer.task"), "jdk-scheduled-timer-task"), PatternIcons.JDK_ICON)) ;
    add(new GenerateSpringDomElementAction(new MethodInvokingFactoryBean(SpringBundle.message("spring.patterns.integration.jdk.method.invoking.factory.bean")) {
      protected String getClassName() {
        return "org.springframework.scheduling.timer.MethodInvokingTimerTaskFactoryBean";
      }
    }, PatternIcons.JDK_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.integration.jdk.timer.factory.bean"), "jdk-timer-factory-bean"), PatternIcons.JDK_ICON)) ;

    setPopup(true);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(SpringBundle.message("spring.patterns.scheduling.group.name"));
    e.getPresentation().setIcon(PatternIcons.SCHEDULER_ICON);
  }
}