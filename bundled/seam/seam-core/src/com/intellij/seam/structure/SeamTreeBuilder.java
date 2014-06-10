package com.intellij.seam.structure;

import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.javaee.module.view.JavaeeModuleAbstractTreeBuilder;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.model.xml.components.SeamComponents;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.util.xml.DomManager;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;

/**
 * User: Sergey.Vasiliev
 */
public class SeamTreeBuilder extends JavaeeModuleAbstractTreeBuilder {

  public SeamTreeBuilder(Project project, AbstractTreeBuilder parentBuilder) {
    super(parentBuilder, project);
    ProjectWideFacetListenersRegistry.getInstance(project).registerListener(SeamFacet.FACET_TYPE_ID, new ProjectWideFacetAdapter<SeamFacet>() {
    public void facetConfigurationChanged(final SeamFacet facet) {
      DefaultMutableTreeNode node = getParentBuilder().getNodeForElement(facet);
      if (node == null) {
        node = getParentBuilder().getRootNode();
      }
      addSubtreeToUpdate(node);
    }
  }, this);
  }

  protected Collection getObjectsToUpdate(PsiElement element) {
    return null;
  }

  protected void onChildrenChanged(PsiElement element, PsiElement child) {
    final PsiFile file = element.getContainingFile();

    if (JamCommonUtil.isPlainJavaFile(file)) {
      for (PsiClass psiClass : ((PsiJavaFile)file).getClasses()) {
        if (SeamCommonUtils.getSeamJamComponent(psiClass) != null) {

          addUpdateFromRoot();
          break;
        }
      }
    }
    else if (JamCommonUtil.isPlainXmlFile(file)) {
      if (DomManager.getDomManager(getProject()).getFileElement((XmlFile)file, SeamComponents.class) != null) {
        addUpdateFromRoot();
      }
    }
  }

}

