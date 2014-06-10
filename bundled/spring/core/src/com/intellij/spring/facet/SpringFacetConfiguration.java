/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.spring.SpringManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringFacetConfiguration implements FacetConfiguration, ModificationTracker, Disposable {

  @NonNls private static final String FILESET = "fileset";
  @NonNls private static final String SET_ID = "id";
  @NonNls private static final String SET_NAME = "name";
  @NonNls private static final String SET_REMOVED = "removed";
  @NonNls private static final String FILE = "file";
  @NonNls private static final String DEPENDENCY = "dependency";

  private final Set<SpringFileSet> myFileSets = new LinkedHashSet<SpringFileSet>();
  private long myModificationCount;

  /**
   *
   * @return configured filesets
   * @see SpringManager#getAllSets(SpringFacet)
   */
  @NotNull
  public Set<SpringFileSet> getFileSets() {
    return myFileSets;
  }

  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {

    final FacetLibrariesValidator validator =
      FacetEditorsFactory.getInstance().createLibrariesValidator(LibraryInfo.EMPTY_ARRAY,
                                                                 new FacetLibrariesValidatorDescription("spring"),
                                                                 editorContext,
                                                                 validatorsManager);
    validatorsManager.registerValidator(validator);

    final SpringFeaturesEditor featuresEditor = new SpringFeaturesEditor(editorContext, validator);
    return new FacetEditorTab[]{new SpringConfigurationTab(this, editorContext), featuresEditor };
  }

  public void readExternal(Element element) throws InvalidDataException {
    for (Object setElement: element.getChildren(FILESET)) {
      final String setName = ((Element)setElement).getAttributeValue(SET_NAME);
      final String setId = ((Element)setElement).getAttributeValue(SET_ID);
      final String removed = ((Element)setElement).getAttributeValue(SET_REMOVED);
      if (setName != null && setId != null) {
        final SpringFileSet fileSet = new SpringFileSet(setId, setName, this);
        final List deps = ((Element)setElement).getChildren(DEPENDENCY);
        for (Object dep : deps) {
          fileSet.addDependency(((Element)dep).getText());
        }
        final List files = ((Element)setElement).getChildren(FILE);
        for (Object fileElement: files) {
          final String text = ((Element)fileElement).getText();
          fileSet.addFile(text);
        }
        fileSet.setRemoved(Boolean.valueOf(removed).booleanValue());
        myFileSets.add(fileSet);
      }
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    for (SpringFileSet fileSet: myFileSets) {
      final Element setElement = new Element(FILESET);
      setElement.setAttribute(SET_ID, fileSet.getId());
      setElement.setAttribute(SET_NAME, fileSet.getName());
      setElement.setAttribute(SET_REMOVED, Boolean.toString(fileSet.isRemoved()));
      element.addContent(setElement);
      for (String dep: fileSet.getDependencies()) {
        final Element depElement = new Element(DEPENDENCY);
        depElement.setText(dep);
        setElement.addContent(depElement);
      }
      for (VirtualFilePointer fileName: fileSet.getFiles()) {
        final Element fileElement = new Element(FILE);
        fileElement.setText(fileName.getUrl());
        setElement.addContent(fileElement);
      }
    }
  }

  public long getModificationCount() {
    return myModificationCount;
  }

  public void setModified() {
    myModificationCount++;
  }

  public void dispose() {

  }
}
