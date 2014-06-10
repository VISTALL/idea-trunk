/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.PersistenceQuery;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.PersistentObject;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.NotNullFunction;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbmStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {
  @Nullable
  public StructureViewBuilder createStructureViewBuilder(@NotNull XmlFile file) {
    final DomFileElement<DomElement> fileElement = DomManager.getDomManager(file.getProject()).getFileElement(file, DomElement.class);
    if (fileElement == null || !ReflectionCache.isInstance(fileElement.getRootElement(), PersistenceMappings.class)) return null;
    return DomService.getInstance().createSimpleStructureViewBuilder(file, new NotNullFunction<DomElement, DomService.StructureViewMode>() {
      @NotNull
      public DomService.StructureViewMode fun(final DomElement domElement) {
        if (domElement instanceof PersistenceMappings ||
            domElement instanceof PersistentObject ||
            domElement instanceof PersistentAttribute ||
            domElement instanceof PersistenceQuery) return DomService.StructureViewMode.SHOW;
        return DomService.StructureViewMode.SKIP;
      }
    });

  }
}