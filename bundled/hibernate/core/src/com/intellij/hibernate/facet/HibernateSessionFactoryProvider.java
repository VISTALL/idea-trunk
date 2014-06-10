package com.intellij.hibernate.facet;

import com.intellij.persistence.model.PersistencePackage;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Gregory.Shrago
 */
public interface HibernateSessionFactoryProvider {
  ExtensionPointName<HibernateSessionFactoryProvider> EP_NAME = ExtensionPointName.create("com.intellij.hibernate.sessionFactoryProvider");

  @NotNull
  List<PersistencePackage> getSessionFactories(final HibernateFacet facet);
}
