package com.intellij.struts.facet.ui;

import com.intellij.facet.ui.DefaultFacetSettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.struts.facet.StrutsValidationConfiguration;

import javax.swing.*;

/**
 * @author nik
 */
public class StrutsFacetDefaultSettingsEditor extends DefaultFacetSettingsEditor {
  private final StrutsValidationConfiguration myConfiguration;
  private final StrutsFacetCommonSettingsPanel mySettingsPanel;

  public StrutsFacetDefaultSettingsEditor(final StrutsValidationConfiguration validationConfiguration) {
    myConfiguration = validationConfiguration;
    mySettingsPanel = new StrutsFacetCommonSettingsPanel();
    mySettingsPanel.getDisablePropertyKeysValidationCheckBox().setThirdStateEnabled(false);
  }

  public StrutsFacetCommonSettingsPanel getSettingsPanel() {
    return mySettingsPanel;
  }

  public JComponent createComponent() {
    return mySettingsPanel.getMainPanel();
  }

  public boolean isModified() {
    return
      myConfiguration.mySuppressPropertiesValidation != mySettingsPanel.getDisablePropertyKeysValidationCheckBox().isSelected();
  }

  public void apply() throws ConfigurationException {
    myConfiguration.mySuppressPropertiesValidation = mySettingsPanel.getDisablePropertyKeysValidationCheckBox().isSelected();
  }

  public void reset() {
    mySettingsPanel.getDisablePropertyKeysValidationCheckBox().setSelected(myConfiguration.mySuppressPropertiesValidation);
  }

  public void disposeUIResources() {
  }
}
