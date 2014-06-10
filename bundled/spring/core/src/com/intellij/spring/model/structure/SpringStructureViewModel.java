package com.intellij.spring.model.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.Disposable;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomElementsNavigationManager;
import org.jetbrains.annotations.NotNull;

public class SpringStructureViewModel extends XmlStructureViewTreeModel implements Disposable {

  private final SpringModelTreeElement myRoot;

  public SpringStructureViewModel(@NotNull XmlFile xmlFile) {
     this(xmlFile, DomElementsNavigationManager.getManager(xmlFile.getProject()).getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME),
          false);
  }

  public SpringStructureViewModel(@NotNull XmlFile xmlFile, final DomElementNavigationProvider navigationProvider, boolean showBeanStructure) {

    super(xmlFile);
    myRoot = new SpringModelTreeElement((XmlFile)getPsiFile(), navigationProvider, showBeanStructure);
  }

  @NotNull
  public StructureViewTreeElement getRoot() {
    return myRoot;
  }

  @NotNull
  public Sorter[] getSorters() {
     return new Sorter[]{Sorter.ALPHA_SORTER};
  }
}
