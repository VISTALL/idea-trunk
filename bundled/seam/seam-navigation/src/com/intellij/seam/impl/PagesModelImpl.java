package com.intellij.seam.impl;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.PagesModel;
import com.intellij.seam.model.xml.pages.Pages;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class PagesModelImpl extends DomModelImpl<Pages> implements PagesModel {
  private final Module myModule;


  public PagesModelImpl(@NotNull Module module, @NotNull DomFileElement<Pages> mergedModel, @NotNull Set<XmlFile> configFiles) {
    super(mergedModel, configFiles);
    myModule = module;
  }

  public Module getModule() {
    return myModule;
  }
}

