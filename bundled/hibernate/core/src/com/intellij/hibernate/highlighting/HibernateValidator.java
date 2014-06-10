package com.intellij.hibernate.highlighting;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.jpa.highlighting.JpaValidatorBase;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;

/**
 * @author Gregory.Shrago
 */
public class HibernateValidator extends JpaValidatorBase {

  public HibernateValidator() {
    super(HibernateMessages.message("persistence.validator.decription"));
  }

  protected boolean acceptsFacet(final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet) {
    return facet.getTypeId() == HibernateFacet.ID;
  }
}
