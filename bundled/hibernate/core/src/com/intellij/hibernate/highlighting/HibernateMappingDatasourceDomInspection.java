/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.highlighting;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import com.intellij.jpa.highlighting.JpaDataSourceORMDomInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gregory.Shrago
 */
public class HibernateMappingDatasourceDomInspection extends BasicDomElementsInspection<HbmHibernateMapping> {

  public HibernateMappingDatasourceDomInspection() {
    super(HbmHibernateMapping.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return HibernateConstants.HIBERNATE_INSPECTIONS_GROUP;
  }

  @NotNull
  public String getDisplayName() {
    return HibernateMessages.message("inspection.name.hibernate.datasource.mapping");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "HibernateMappingDatasourceDomInspection";
  }

  protected void checkDomElement(final DomElement element, final DomElementAnnotationHolder holder, final DomHighlightingHelper helper) {
    JpaDataSourceORMDomInspection.checkDataSourceRelatedValues(element, holder, helper);
  }
}