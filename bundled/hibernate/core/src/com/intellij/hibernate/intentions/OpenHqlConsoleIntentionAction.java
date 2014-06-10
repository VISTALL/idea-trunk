/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.intentions;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.engine.HibernateConsole;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gregory.Shrago
 */
public class OpenHqlConsoleIntentionAction extends QueryIntentionActionBase {

  @NotNull
  public String getFamilyName() {
    return HibernateMessages.message("intention.execute.hql.query.family");
  }

  protected void invokeInner(final Project project, final Editor editor, final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> curFacet,
                             final PersistencePackage curUnit,
                             final String query) {
    HibernateConsole.openHqlConsole(project, editor, curFacet, curUnit, query, null);
  }

}