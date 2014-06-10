package com.intellij.spring.model.actions.patterns.osgi;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.actions.patterns.PatternIcons;

public class GenerateOsgiPatternsGroup extends DefaultActionGroup {

  public GenerateOsgiPatternsGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.service"), "osgi_simple_service"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.multiple.service"), "osgi_multiple_service"), SpringIcons.SPRING_BEAN_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.ref"), "osgi_ref"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.multiple.ref"), "osgi_multi_ref"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.ref.with.listener"), "osgi_ref_listener"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.ref.with.bean"), "osgi_ref_with_bean"), SpringIcons.SPRING_BEAN_ICON)) ;

    addSeparator();

    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.list"), "osgi_list"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.list.comparator"), "osgi_list_comparator"), SpringIcons.SPRING_BEAN_ICON)) ;

    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.set"), "osgi_set"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.osgi.set.comparator"), "osgi_set_comparator"), SpringIcons.SPRING_BEAN_ICON)) ;

    setPopup(true);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(SpringBundle.message("spring.patterns.osgi.group.name"));
    e.getPresentation().setIcon(PatternIcons.FACTORY_BEAN_ICON);
  }

}
