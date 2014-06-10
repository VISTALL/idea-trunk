package com.intellij.spring.model.actions;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.actions.generate.SpringConstructorDependenciesGenerateProvider;
import com.intellij.spring.model.actions.generate.SpringPropertiesGenerateProvider;
import com.intellij.spring.model.actions.generate.SpringSetterDependenciesGenerateProvider;

public class GenerateSpringDomElementActionGroup extends DefaultActionGroup {

  public GenerateSpringDomElementActionGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.bean"), "spring-bean"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider((SpringBundle.message("spring.bean.instantiation.by.factory")), "spring-bean-with-factory-method"), SpringIcons.SPRING_BEAN_ICON));
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider((SpringBundle.message("spring.bean.instantiation.using.factory.method")), "spring-bean-with-factory-bean"), SpringIcons.SPRING_BEAN_ICON));

    //add(new GenerateSpringDomElementAction(new SpringAliasGenerateProvider(), SpringIcons.SPRING_ALIAS_ICON ));
    //add(new GenerateSpringDomElementAction(new SpringImportGenerateProvider(), SpringIcons.CONFIG_FILE));

    add(Separator.getInstance());

    add(new GenerateSpringBeanBodyAction(new SpringPropertiesGenerateProvider()));
    add(new GenerateSpringBeanBodyAction(new SpringSetterDependenciesGenerateProvider(), SpringIcons.SPRING_BEAN_ICON));
    add(new GenerateSpringBeanBodyAction(new SpringConstructorDependenciesGenerateProvider(), SpringIcons.SPRING_BEAN_ICON));
  }

}

