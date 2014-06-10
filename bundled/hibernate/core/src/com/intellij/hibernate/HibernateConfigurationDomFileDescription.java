/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.hibernate;

import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.impl.config.SessionFactoryImpl;
import com.intellij.hibernate.model.xml.impl.config.HibernateConfigurationImpl;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.highlighting.HibernateDomAnnotator;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.util.xml.DomFileDescription;
import com.intellij.util.xml.highlighting.DomElementsAnnotator;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.Collections;

/**
 * @author peter
*/
public class HibernateConfigurationDomFileDescription extends DomFileDescription<HibernateConfiguration> {
  public HibernateConfigurationDomFileDescription() {
    super(HibernateConfiguration.class, HibernateConstants.CFG_XML_ROOT_TAG);
  }

  protected void initializeFileDescription() {
    registerImplementation(CommonDomModelElement.class, BaseImpl.class);
    registerImplementation(HibernateConfiguration.class, HibernateConfigurationImpl.class);
    registerImplementation(SessionFactory.class, SessionFactoryImpl.class);
  }

  public DomElementsAnnotator createAnnotator() {
    return new HibernateDomAnnotator();
  }

  public boolean isMyFile(@NotNull XmlFile file, final Module module) {
    return HibernateUtil.isHibernateConfig(file, module);
  }

  @NotNull
    public Set<? extends Object> getDependencyItems(final XmlFile file) {
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    return Collections.singleton(module == null ? null : HibernateFacet.getInstance(module));
  }

  public boolean isAutomaticHighlightingEnabled() {
    return false;
  }
}
