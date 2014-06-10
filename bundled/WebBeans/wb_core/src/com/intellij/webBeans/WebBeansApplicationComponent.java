package com.intellij.webBeans;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.ui.libraries.versions.LibrariesConfigurationManager;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Function;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.webBeans.facet.WebBeansFacetType;
import com.intellij.webBeans.highlighting.*;
import com.intellij.webBeans.jam.NamedWebBean;
import com.intellij.webBeans.resources.WebBeansBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class WebBeansApplicationComponent
  implements ApplicationComponent, FileTemplateGroupDescriptorFactory, InspectionToolProvider, Disposable {

  private Map<LibraryVersionInfo, List<LibraryInfo>> myLibraries;

  public static WebBeansApplicationComponent getInstance() {
    return ApplicationManager.getApplication().getComponent(WebBeansApplicationComponent.class);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(WebBeansFacetType.INSTANCE);

    registerPresentation();

  }

  private static void registerPresentation() {
    ElementPresentationManager.registerNameProvider(new Function<Object, String>() {
      public String fun(Object o) {
        if (o instanceof NamedWebBean) {
          return ((NamedWebBean)o).getName();
        }
        return null;
      }
    });
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{WebBeanInitializerInspection.class, WebBeanInjectionInspection.class, SimpleWebBeanInconsistencyInspection.class,
      WebBeanStereotypeRestrictionsInspection.class, WebBeanObservesInspection.class, WebBeanDecoratorInspection.class,
      WebBeanSpecializesInspection.class};
  }

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor groupDescriptor =
      new FileTemplateGroupDescriptor(WebBeansBundle.message("webBeans.framework.name"), WebBeansIcons.WEB_BEANS_ICON);

    //groupDescriptor.addTemplate(new FileTemplateDescriptor(WebBeansCommonConstants.FILE_TEMPLATE_NAME_WEB_BEANS_1_0_0, WebBeansIcons.WEB_BEANS_ICON));

    return groupDescriptor;
  }

  public Map<LibraryVersionInfo, List<LibraryInfo>> getLibraries() {
    if (myLibraries == null) {
      myLibraries = LibrariesConfigurationManager.getLibraries(getLibrariesUrl("/resources/versions/libraries_jboss.xml"),
                                                               getLibrariesUrl("/resources/versions/libraries_apache.xml"));
    }
    return myLibraries;
  }

  private static URL getLibrariesUrl(String url) {
    return WebBeansApplicationComponent.class.getResource(url);
  }
}

