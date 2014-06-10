/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.openapi.module.Module;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.model.manipulators.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateFacetManipulator extends AbstractPersistenceManipulator<HibernateFacet> implements PersistenceFacetManipulator<HibernateFacet> {
  public HibernateFacetManipulator(final HibernateFacet target) {
    super(target);
  }

  public List<PersistenceAction> getCreateActions() {
    final Module module = getManipulatorTarget().getModule();
    final THashSet<XmlFile> files = new THashSet<XmlFile>();
    final List<SpringModel> springModels = SpringManager.getInstance(module.getProject()).getAllModels(module);
    for (SpringModel springModel : springModels) {
      files.addAll(springModel.getConfigFiles());
    }
    List<PersistenceAction> result = new ArrayList<PersistenceAction>();
    final DomManager domManager = DomManager.getDomManager(module.getProject());
    final ManipulatorsRegistry manipulatorsRegistry = PersistenceHelper.getHelper().getManipulatorsRegistry();
    for (XmlFile file : files) {
      final DomFileElement<Beans> fileElement = domManager.getFileElement(file, Beans.class);
      if (fileElement != null) {
        final PersistenceManipulator<Beans> manipulator = manipulatorsRegistry.getManipulator(fileElement.getRootElement(), PersistenceManipulator.class);
        if (manipulator != null) {
          result.addAll(manipulator.getCreateActions());
        }
      }
    }
    return result;
  }

}
