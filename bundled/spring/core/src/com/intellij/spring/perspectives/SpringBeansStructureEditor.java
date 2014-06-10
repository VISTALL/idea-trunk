/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.perspectives.graph.SpringBeansDependencyGraphComponent;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SpringBeansStructureEditor extends PerspectiveFileEditor {

  private SpringBeansDependencyGraphComponent myComponent;
  private final XmlFile myXmlFile;

  public SpringBeansStructureEditor(final Project project, final VirtualFile file) {
    super(project, file);

    final PsiFile psiFile = getPsiFile();
    assert psiFile instanceof XmlFile;

    myXmlFile = (XmlFile)psiFile;
  }


  @Nullable
  protected DomElement getSelectedDomElement() {
    final List<SpringBeanPointer> selectedBeans = getDependenciesComponent().getSelectedBeans();

    if(selectedBeans.size() > 0 && selectedBeans.get(0).getSpringBean() instanceof DomSpringBean) {
       return (DomElement)selectedBeans.get(0).getSpringBean();
    }
    return null;
  }

  protected void setSelectedDomElement(final DomElement domElement) {
    getDependenciesComponent().setSelectedDomElement(domElement);
  }

  @NotNull
  protected JComponent createCustomComponent() {
    return getDependenciesComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
  return getDependenciesComponent().getBuilder().getView().getJComponent();
  }

  public void commit() {
  }

  public void reset() {
    getDependenciesComponent().getBuilder().queueUpdate();
  }

  @NotNull
  public String getName() {
    return SpringBundle.message("spring.beans.dependencies");
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return GraphViewUtil.createStructureViewBuilder(getDependenciesComponent().getOverview());
  }

  private SpringBeansDependencyGraphComponent getDependenciesComponent() {
    if (myComponent == null) {
      myComponent = createGraphComponent();
      Disposer.register(this, myComponent);
    }
    return myComponent;
  }

  private SpringBeansDependencyGraphComponent createGraphComponent() {
    final SpringBeansDependencyGraphComponent[] graphComponent = {null};
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
          graphComponent[0] = new SpringBeansDependencyGraphComponent(myXmlFile);
      }
    }, SpringBundle.message("generating.bean.dependencies.diagramm"), false, myXmlFile.getProject());


    return graphComponent[0];
  }

}
