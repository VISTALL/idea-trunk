/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.actions;

import com.intellij.hibernate.engine.HibernateEngine;
import com.intellij.hibernate.engine.HibernateConsole;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.persistence.PersistenceDataKeys;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;

/**
 * @author Gregory.Shrago
 */
public class OpenHqlConsoleAction extends AnAction {


  public void update(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    final PersistenceFacetBase<PersistenceFacetConfiguration,PersistencePackage> facet =
      PersistenceDataKeys.PERSISTENCE_FACET.getData(dataContext);
    final PersistencePackage unit = PersistenceDataKeys.PERSISTENCE_UNIT.getData(dataContext);

    final boolean enable = project != null && facet != null && unit != null && unit.isValid()
                           && facet.getQlLanguage() != null
                           && HibernateEngine.isAvailable(facet.getModule());
    e.getPresentation().setEnabled(enable);
    e.getPresentation().setVisible(enable || !ActionPlaces.isPopupPlace(e.getPlace()));
  }

  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet =
      PersistenceDataKeys.PERSISTENCE_FACET.getData(dataContext);
    final PersistencePackage unit = PersistenceDataKeys.PERSISTENCE_UNIT.getData(dataContext);

    HibernateConsole.openHqlConsole(project, PlatformDataKeys.EDITOR.getData(dataContext), facet, unit, "", e);
  }

}