package com.intellij.seam;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.seam.inspections.PagesFileModelInspection;
import com.intellij.seam.inspections.PagesModelInspection;
import com.intellij.seam.model.xml.pages.Page;
import com.intellij.seam.model.xml.pages.PagesException;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PagesApplicationComponent implements ApplicationComponent, InspectionToolProvider, Disposable {

  private static final Logger LOG = Logger.getInstance(PagesApplicationComponent.class.getName());

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    registerPresentations();
  }

  private static void registerPresentations() {
    ElementPresentationManager.registerIcons(Page.class, PagesIcons.PAGE);
    ElementPresentationManager.registerIcons(PagesException.class, PagesIcons.EXCEPTION);
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{PagesModelInspection.class, PagesFileModelInspection.class};
  }

}
