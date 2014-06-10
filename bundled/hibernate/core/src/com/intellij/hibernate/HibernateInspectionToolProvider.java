package com.intellij.hibernate;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.hibernate.highlighting.HibernateConfigDomInspection;
import com.intellij.hibernate.highlighting.HibernateMappingDatasourceDomInspection;
import com.intellij.hibernate.highlighting.HibernateMappingDomInspection;
import com.intellij.hibernate.highlighting.HibernateConfigDomFacetInspection;

/**
 * @author Gregory.Shrago
 */
public class HibernateInspectionToolProvider implements InspectionToolProvider {

  public Class[] getInspectionClasses() {
    return new Class[]{
      HibernateConfigDomInspection.class,
      HibernateConfigDomFacetInspection.class,
      HibernateMappingDomInspection.class,
      HibernateMappingDatasourceDomInspection.class
      };
  }
}
