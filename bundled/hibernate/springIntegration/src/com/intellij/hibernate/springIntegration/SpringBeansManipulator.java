/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.PersistenceDataKeys;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.manipulators.*;
import com.intellij.psi.PsiElement;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class SpringBeansManipulator extends AbstractPersistenceManipulator<Beans> implements PersistenceManipulator<Beans> {
  private String myName;

  public SpringBeansManipulator(final Beans target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {

    return Arrays.<PersistenceAction>asList(new AbstractPersistenceAction<SpringBeansManipulator>(
      this, "Session Factory Bean", "Session Factory Bean", SpringIntegrationIcons.SESSION_FACTORY_ICON) {
      private PersistenceFacetBase<? extends PersistenceFacetConfiguration, ? extends PersistencePackage> myFacet;

      public int getGroupId() {
        return GROUP_UNIT;
      }

      public void update(final AnActionEvent e) {
        super.update(e);
        myFacet = PersistenceDataKeys.PERSISTENCE_FACET.getData(e.getDataContext());
        getPresentation().setEnabled(myFacet != null);
      }

      public boolean preInvoke(final UserResponse response) {
        myName = myFacet == null ? null : response.getPersistenceUnitName(myFacet);
        return StringUtil.isNotEmpty(myName);
      }

      public void addAffectedElements(@NotNull final Collection<PsiElement> affectedElements) {
      }

      protected PsiElement getTargetElement() {
        return getManipulator().getManipulatorTarget().getXmlElement();
      }

      public void invokeAction(@NotNull final Collection<PsiElement> result) {
        result.add(createSessionFactory(myName, myFacet).getIdentifyingPsiElement());
      }

    });
  }


  public PersistencePackage createSessionFactory(final String unitName, final PersistenceFacetBase facet) {
    final Beans beans = getManipulatorTarget();
    final SpringBean bean = beans.addBean();
    bean.getId().setValue(unitName);
    bean.getClazz().setStringValue(SpringBeanPersistenceUnit.ANNO_SESSION_FACTORY);
    assert facet instanceof HibernateFacet;
    return IntegrationUtil.getPersistencePackage(bean, (HibernateFacet)facet, true);
  }
}
