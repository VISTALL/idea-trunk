package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateSessionFactoryProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class SpringSessionFactoryProvider implements HibernateSessionFactoryProvider {

  @NotNull
  public List<PersistencePackage> getSessionFactories(final HibernateFacet facet) {
    final SpringModel springModel = SpringManager.getInstance(facet.getModule().getProject()).getCombinedModel(facet.getModule());
    if (springModel != null) {
      final ArrayList<PersistencePackage> result = new ArrayList<PersistencePackage>();
      for (SpringBaseBeanPointer bean : springModel.getAllDomBeans()) {
        final Function<Pair<SpringBean, HibernateFacet>, PersistencePackage> provider =
          IntegrationUtil.getProvider(bean, IntegrationUtil.myProviders, true);
        if (provider != null) {
          final PersistencePackage unit = provider.fun(Pair.create((SpringBean)bean.getSpringBean(), facet));
          if (!bean.isAbstract()) {
            result.add(unit);
          }
        }
      }
      return result;
    }
    return Collections.emptyList();
  }

}
