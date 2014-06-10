/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.manipulators;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.jpa.util.JpaUtil;
import com.intellij.persistence.model.manipulators.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateFacetManipulator extends AbstractPersistenceManipulator<HibernateFacet> implements PersistenceFacetManipulator<HibernateFacet> {
  public HibernateFacetManipulator(final HibernateFacet target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    return Arrays.<PersistenceAction>asList(new AbstractPersistenceAction<HibernateFacetManipulator>(this, HibernateMessages.message(
      "action.name.create.session.factory"), HibernateMessages.message("type.hibernate.session.factory"), HibernateIcons.SESSION_FACTORY_ICON) {
      private HibernateConfiguration myRoot;

      public int getGroupId() {
        return GROUP_UNIT;
      }

      public boolean preInvoke(final UserResponse response) {
        final HibernateFacet facet = getManipulatorTarget();
        myRoot = JpaUtil.getOrChooseElement(facet.getModule(), facet.getDescriptorsContainer(), HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA, HibernateConfiguration.class,
                                            HibernateMessages.message("title.new.hibernate.cfg.xml"), true);
        return myRoot != null;
      }

      public void addAffectedElements(@NotNull final Collection<PsiElement> affectedElements) {
        affectedElements.add(myRoot.getIdentifyingPsiElement());
      }

      protected PsiElement getTargetElement() {
        return myRoot.getIdentifyingPsiElement();
      }

      public void invokeAction(@NotNull final Collection<PsiElement> result) {
        final SessionFactory seesionFactory = myRoot.getSessionFactory();
        result.add(seesionFactory.getIdentifyingPsiElement());
      }

    });
  }

}
