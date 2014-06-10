package com.intellij.seam.fileEditor;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.resources.messages.PagesBundle;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class PagesGraphFileEditor extends PerspectiveFileEditor {

  private PagesGraphComponent myComponent;
  private final XmlFile myXmlFile;

  public PagesGraphFileEditor(final Project project, final VirtualFile file) {
    super(project, file);

    final PsiFile psiFile = getPsiFile();
    assert psiFile instanceof XmlFile;

    myXmlFile = (XmlFile)psiFile;
  }


  @Nullable
  protected DomElement getSelectedDomElement() {
    final List<DomElement> selectedDomElements = getPagesGraphComponent().getSelectedDomElements();

    return selectedDomElements.size() > 0 ? selectedDomElements.get(0) : null;
  }

  protected void setSelectedDomElement(final DomElement domElement) {
      getPagesGraphComponent().setSelectedDomElement(domElement);
  }

  @NotNull
  protected JComponent createCustomComponent() {
    return getPagesGraphComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
   return ((Graph2DView)getPagesGraphComponent().getBuilder().getGraph().getCurrentView()).getJComponent();
  }

  public void commit() {
  }

  public void reset() {
    getPagesGraphComponent().getBuilder().queueUpdate();
  }

  @NotNull
  public String getName() {
    return PagesBundle.message("seam.pages.graph");
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return GraphViewUtil.createStructureViewBuilder(getPagesGraphComponent().getOverview());
  }

  private PagesGraphComponent getPagesGraphComponent() {
    if (myComponent == null) {
      myComponent = new PagesGraphComponent(myXmlFile);
      Disposer.register(this, myComponent);
    }
    return myComponent;
  }
}

