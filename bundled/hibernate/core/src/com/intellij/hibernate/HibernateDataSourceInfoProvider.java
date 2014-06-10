/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate;

import com.intellij.facet.FacetManager;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.persistence.DataSourceInfoProvider;
import com.intellij.persistence.JdbcPropertiesDataSourceInfoProvider;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class HibernateDataSourceInfoProvider extends DataSourceInfoProvider {
  public Collection<Pair<PsiElement,DataSourceInfo>> getDataSources(final Project project) {
    final ArrayList<Pair<PsiElement, DataSourceInfo>> result = new ArrayList<Pair<PsiElement, DataSourceInfo>>();
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      final Collection<HibernateFacet> facets = FacetManager.getInstance(module).getFacetsByType(HibernateFacet.ID);
      for (HibernateFacet facet : facets) {
        for (PersistencePackage persistencePackage : facet.getPersistenceUnits()) {
          if (persistencePackage instanceof SessionFactory) {
            final SessionFactory factory = (SessionFactory)persistencePackage;
            final Map<String, String> map = HibernateUtil.getSessionFactoryProperties(factory);
            final String driverClass = map.get(HibernatePropertiesConstants.DRIVER);
            final String url = map.get(HibernatePropertiesConstants.URL);
            final String user = map.get(HibernatePropertiesConstants.USER);
            final String password = map.get(HibernatePropertiesConstants.PASS);
            final DataSourceInfo dataSourceInfo = getInfo("hibernate", factory.getName().getValue(), driverClass, url, user, password);
            if (dataSourceInfo != null) {
              result.add(Pair.<PsiElement, DataSourceInfo>create(factory.getXmlElement(), dataSourceInfo));
            }
          }
        }
      }
    }
    return result;
  }

  @Nullable
  public static DataSourceInfo getInfo(@NonNls final String type,
                                       @NonNls final String name,
                                       @NonNls final Collection<String> drivers,
                                       @NonNls final Collection<String> urls,
                                       @NonNls final Collection<String> userNames,
                                       @NonNls final Collection<String> passwords) {
    return JdbcPropertiesDataSourceInfoProvider.getInfo(type, name, drivers, urls, userNames, passwords);
  }

  @Nullable
  public static DataSourceInfo getInfo(@NonNls final String type,
                                       @NonNls final String name,
                                       @NonNls final String driver,
                                       @NonNls final String url,
                                       @NonNls final String userName,
                                       @NonNls final String password) {
    return JdbcPropertiesDataSourceInfoProvider.getInfo(type, name, driver, url, userName, password);
  }

}
