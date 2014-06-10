package com.intellij.seam.impl;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.seam.model.xml.PageflowModel;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowModelImpl extends DomModelImpl<PageflowDefinition> implements PageflowModel {
  private final Module myModule;


  public PageflowModelImpl(@NotNull Module module, @NotNull DomFileElement<PageflowDefinition> mergedModel, @NotNull Set<XmlFile> configFiles) {
    super(mergedModel, configFiles);
    myModule = module;
  }

  public Module getModule() {
    return myModule;
  }
}
