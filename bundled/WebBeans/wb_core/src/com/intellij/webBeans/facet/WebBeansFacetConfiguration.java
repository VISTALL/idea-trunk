package com.intellij.webBeans.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class WebBeansFacetConfiguration implements FacetConfiguration {

  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {
    final FacetLibrariesValidator validator = FacetEditorsFactory.getInstance()
      .createLibrariesValidator(LibraryInfo.EMPTY_ARRAY, new FacetLibrariesValidatorDescription("webBeans"), editorContext, validatorsManager);
    validatorsManager.registerValidator(validator);

    return new FacetEditorTab[]{new WebBeansFeaturesEditor(editorContext, validator)};
  }

  public void readExternal(Element element) throws InvalidDataException {
  }

  public void writeExternal(Element element) throws WriteExternalException {
  }
}

