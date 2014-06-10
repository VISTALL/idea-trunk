package com.intellij.seam.structure;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.constants.SeamDataKeys;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.model.jam.SeamJamModel;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.utils.SeamConfigFileUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeamFacetNodeDescriptor extends JavaeeNodeDescriptor<SeamFacet> {
  public SeamFacetNodeDescriptor(Project project, SeamFacet facet, NodeDescriptor parentDescriptor, Object parameters) {
    super(project, parentDescriptor, parameters, facet);
  }

  protected String getNewNodeText() {
    return getElement().getModule().getName();
  }

  public JavaeeNodeDescriptor[] getChildren() {
    List<JavaeeNodeDescriptor> nodes = new ArrayList<JavaeeNodeDescriptor>();
    List<SeamJamComponent> compiledElements = new ArrayList<SeamJamComponent>();
    List<XmlFile> libElements = new ArrayList<XmlFile>();

    addJamNodes(nodes, compiledElements);
    addDomNodes(nodes, libElements);

    if (!compiledElements.isEmpty() || !libElements.isEmpty()) {
      nodes.add(new SeamModuleLibrariesNodeDescriptor(compiledElements, libElements, getProject(), this, getParameters(),
                                                      getElement().getModule()));
    }

    return nodes.toArray(new JavaeeNodeDescriptor[nodes.size()]);
  }

  private void addDomNodes(final List<JavaeeNodeDescriptor> nodes, final List<XmlFile> libFiles) {
    SeamDomModelManager modelManager = SeamDomModelManager.getInstance(getProject());

    Set<XmlFile> files = SeamConfigFileUtils.getConfigurationFiles(getElement().getModule());

    for (XmlFile file : files) {
      if (modelManager.isSeamComponents(file)) {
        if (isLibrary(file)) {
          libFiles.add(file);
        } else {
          nodes.add(new SeamDomModelNodeDescriptor(getProject(), this, getParameters(), file, getProject().getBaseDir()));
        }
      }
    }
  }

  private static boolean isLibrary(final XmlFile xmlFile) {
    VirtualFile file = xmlFile.getVirtualFile();
    assert file != null;
    return file.getFileSystem() instanceof JarFileSystem;
  }

  private void addJamNodes(final List<JavaeeNodeDescriptor> nodes, final List<SeamJamComponent> compiledElements) {
    final Module module = getElement().getModule();

    addJamNodes(nodes, compiledElements, module, true);

    for (Module moduleDependency : JamCommonUtil.getAllModuleDependencies(module)) {
      addJamNodes(nodes, compiledElements, moduleDependency, false);
    }
  }

  private void addJamNodes(final List<JavaeeNodeDescriptor> nodes, final List<SeamJamComponent> compiledElements, final Module module,
                           final boolean fromLibs) {
    for (SeamJamComponent component : SeamJamModel.getModel(module).getSeamComponents(false, fromLibs)) {
      if (component.getPsiElement() instanceof PsiCompiledElement) {
        compiledElements.add(component);
      }
      else {
        nodes.add(new SeamComponentNodeDescriptor(component, this, getParameters()));
      }
    }
  }

  protected Icon getNewOpenIcon() {
    return getElement().getModule().getModuleType().getNodeIcon(true);
  }

  protected Icon getNewClosedIcon() {
    return getElement().getModule().getModuleType().getNodeIcon(false);
  }

  public Object getData(final String dataId) {
    if (SeamDataKeys.SEAM_FACET.getName().equals(dataId)) {
       return getElement();
    }
    return super.getData(dataId);
  }
}