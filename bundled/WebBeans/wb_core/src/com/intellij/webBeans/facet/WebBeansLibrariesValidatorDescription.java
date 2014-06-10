package com.intellij.webBeans.facet;

import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.webBeans.resources.WebBeansBundle;
import org.jetbrains.annotations.NonNls;

public class WebBeansLibrariesValidatorDescription extends FacetLibrariesValidatorDescription {
  private final WebBeansFeaturesEditor myEditor;

  public WebBeansLibrariesValidatorDescription(WebBeansFeaturesEditor editor) {
    super(WebBeansBundle.message("webBeans.framework.name"));
    myEditor = editor;
  }

  @NonNls
  public String getDefaultLibraryName() {
    LibraryVersionInfo libraryVersionInfo = myEditor.getCurrentLibraryVersionInfo();
    if (libraryVersionInfo != null) {
      String ri = libraryVersionInfo.getRI();
      String version = libraryVersionInfo.getVersion();

      return StringUtil.isEmptyOrSpaces(ri) ? version : ri +"." + version;
    }

    return super.getDefaultLibraryName();
  }
}

