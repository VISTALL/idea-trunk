package com.intellij.webBeans.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.impl.ui.libraries.versions.VersionsComponent;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.webBeans.WebBeansApplicationComponent;
import com.intellij.webBeans.resources.WebBeansBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class WebBeansFeaturesEditor extends FacetEditorTab {

  private JPanel myMainPanel;
  private JPanel myVersionsPanel;
  private FacetLibrariesValidator myValidator;
  private VersionsComponent myComponent;

  public WebBeansFeaturesEditor(final FacetEditorContext editorContext, final FacetLibrariesValidator validator) {
    myValidator = validator;

    myComponent = new VersionsComponent(editorContext.getModule(), validator) {
      protected String getFacetDetectionClass(@NotNull String currentRI) {
        return getDetectionClass(currentRI);
      }

      @NotNull
      protected Map<LibraryVersionInfo, List<LibraryInfo>> getLibraries() {
        return WebBeansApplicationComponent.getInstance().getLibraries();
      }
    };

    myVersionsPanel.add(myComponent.getJComponent(), BorderLayout.CENTER);
  }


  @Nullable
  public LibraryVersionInfo getCurrentLibraryVersionInfo() {
    return myComponent.getCurrentLibraryVersionInfo();
  }

  @Nullable
  private static String getDetectionClass(String currentRI) {
    for (WebBeans_RI ri : WebBeans_RI.values()) {
      if (currentRI.equals(ri.getName())) {
        return ri.getFacetDetectionClass();
      }
    }
    return null;
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myValidator.onFacetInitialized(facet);
  }

  @Nls
  public String getDisplayName() {
    return WebBeansBundle.message("facet.editor.name");
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    return myValidator.isLibrariesAdded();
  }

  public void apply() throws ConfigurationException {
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {

  }
}
