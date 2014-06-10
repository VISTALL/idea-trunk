package com.intellij.hibernate.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.hibernate.facet.ui.HibernateGeneralEditorTab;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.util.descriptors.ConfigFileInfoSet;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class HibernateFacetConfiguration implements PersistenceFacetConfiguration {
  private final ConfigFileInfoSet myDescriptorsConfiguration;
  private final Map<String, String> myUnitToDataSourceMap = new HashMap<String, String>();
  private static final String VALIDATION_ENABLED = "validation-enabled";

  public HibernateFacetConfiguration(final ConfigFileInfoSet descriptorsConfiguration) {
    myDescriptorsConfiguration = descriptorsConfiguration;
  }

  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {
    final FacetLibrariesValidator validator =
      FacetEditorsFactory.getInstance().createLibrariesValidator(HibernateVersion.Hibernate_3_2_0.getJars(), new HibernateLibrariesValidatorDescription(), editorContext,
                                                                 validatorsManager);
    validatorsManager.registerValidator(validator);
    final HibernateGeneralEditorTab generalEditor = new HibernateGeneralEditorTab(this, editorContext, validator);
    return new FacetEditorTab[] {
      generalEditor
    };
  }

  public void readExternal(Element element) throws InvalidDataException {
    JDOMExternalizer.readMap(element, myUnitToDataSourceMap, "datasource-map", "unit-entry");
    myDescriptorsConfiguration.readExternal(element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    JDOMExternalizer.writeMap(element, myUnitToDataSourceMap, "datasource-map", "unit-entry");
    myDescriptorsConfiguration.writeExternal(element);
  }

  public ConfigFileInfoSet getDescriptorsConfiguration() {
    return myDescriptorsConfiguration;
  }

  public Map<String, String> getUnitToDataSourceMap() {
    return myUnitToDataSourceMap;
  }
}
