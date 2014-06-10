package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.javaee.CommonModelManager;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.jam.view.JamDeleteHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Gregory.Shrago
 */
public class SpringIntegrationApplicationComponent implements ApplicationComponent {
  @NonNls
  @NotNull
  public String getComponentName() {
    return "SpringIntegrationApplicationComponent";
  }

  public void initComponent() {
    ElementPresentationManager.registerIcon(SpringBeanPersistenceUnit.class, SpringIntegrationIcons.SESSION_FACTORY_ICON);

    PersistenceHelper.getHelper().getManipulatorsRegistry().registerManipulator(SpringBeanPersistenceUnit.class, SpringBeanPersistenceUnitManipulator.class);
    PersistenceHelper.getHelper().getManipulatorsRegistry().registerManipulator(HibernateFacet.class, HibernateFacetManipulator.class);
    PersistenceHelper.getHelper().getManipulatorsRegistry().registerManipulator(Beans.class, SpringBeansManipulator.class);

    CommonModelManager.getInstance().registerDeleteHandler(new JamDeleteHandler() {
      public void addModelElements(final CommonModelElement element, final Collection<CommonModelElement> result) {
        if (element instanceof SpringBeanPersistenceUnit) {
          result.add(((SpringBeanPersistenceUnit)element).getBean());
        }
      }
    });
  }

  public void disposeComponent() {
  }
}
