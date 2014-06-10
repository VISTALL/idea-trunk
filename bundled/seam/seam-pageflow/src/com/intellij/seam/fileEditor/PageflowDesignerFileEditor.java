package com.intellij.seam.fileEditor;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.resources.messages.PageflowBundle;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class PageflowDesignerFileEditor extends PerspectiveFileEditor {

  private PageflowDesignerComponent myComponent;
  private final XmlFile myXmlFile;

  public PageflowDesignerFileEditor(final Project project, final VirtualFile file) {
    super(project, file);

    final PsiFile psiFile = getPsiFile();
    assert psiFile instanceof XmlFile;

    myXmlFile = (XmlFile)psiFile;
  }


  @Nullable
  protected DomElement getSelectedDomElement() {
    final List<DomElement> selectedDomElements = getPageflowDesignerComponent().getSelectedDomElements();

    return selectedDomElements.size() > 0 ? selectedDomElements.get(0) : null;
  }

  protected void setSelectedDomElement(final DomElement domElement) {
      getPageflowDesignerComponent().setSelectedDomElement(domElement);
  }

  @NotNull
  protected JComponent createCustomComponent() {
    return getPageflowDesignerComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
   return ((Graph2DView)getPageflowDesignerComponent().getBuilder().getGraph().getCurrentView()).getJComponent();
  }

  public void commit() {
  }

  public void reset() {
    getPageflowDesignerComponent().getBuilder().queueUpdate();
  }

  @NotNull
  public String getName() {
    return PageflowBundle.message("seam.pageflow.designer");
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return GraphViewUtil.createStructureViewBuilder(getPageflowDesignerComponent().getOverview());
  }

  private PageflowDesignerComponent getPageflowDesignerComponent() {
    if (myComponent == null) {
      myComponent = new PageflowDesignerComponent(myXmlFile);
      Disposer.register(this, myComponent);
    }
    return myComponent;
  }
}

