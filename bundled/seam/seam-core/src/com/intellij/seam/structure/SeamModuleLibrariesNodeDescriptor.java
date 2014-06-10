package com.intellij.seam.structure;

import com.intellij.ide.DeleteProvider;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.util.Icons;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeamModuleLibrariesNodeDescriptor extends JavaeeNodeDescriptor<Module> {
  private final List<SeamJamComponent> myCompiledElements;
  private final List<XmlFile> myModelFiles;

  public SeamModuleLibrariesNodeDescriptor(final List<SeamJamComponent> compiledElements,
                                           final List<XmlFile> modelFiles,
                                           final Project project,
                                           final NodeDescriptor parentDescriptor,
                                           final Object parameters,
                                           final Module element) {
    super(project, parentDescriptor, parameters, element);

    myCompiledElements = compiledElements;
    myModelFiles = modelFiles;
  }

  protected String getNewNodeText() {
    return SeamBundle.message("seam.j2ee.structure.module.libraries");
  }

  protected DeleteProvider getDeleteProvider() {
    return super.getDeleteProvider();
  }

  public JavaeeNodeDescriptor[] getChildren() {
    List<JavaeeNodeDescriptor> nodes = new ArrayList<JavaeeNodeDescriptor>();
    Map<VirtualFile, List<SeamJamComponent>> libs = new HashMap<VirtualFile, List<SeamJamComponent>>();
    Map<VirtualFile, List<XmlFile>> modelLibs = new HashMap<VirtualFile, List<XmlFile>>();

    List<SeamJamComponent> unknownLib = new ArrayList<SeamJamComponent>();

    for (SeamJamComponent compiledElement : myCompiledElements) {
      VirtualFile forJar = getJarFile(compiledElement.getPsiElement().getContainingFile().getVirtualFile());
      if (forJar != null) {
        if (libs.get(forJar) == null) libs.put(forJar, new ArrayList<SeamJamComponent>());
        libs.get(forJar).add(compiledElement);
      }
      else {
        unknownLib.add(compiledElement);
      }
    }

    for (XmlFile modelFile : myModelFiles) {
      VirtualFile forJar = getJarFile(modelFile.getVirtualFile());
      if (forJar != null) {
        if (modelLibs.get(forJar) == null) modelLibs.put(forJar, new ArrayList<XmlFile>());
        modelLibs.get(forJar).add(modelFile);
      }
    }

    for (VirtualFile file : libs.keySet()) {
      nodes.add(new ModuleLibNodeDescriptor(getProject(), this, getParameters(), file, libs.get(file), modelLibs.get(file)));
    }
    for (SeamJamComponent seamComponent : unknownLib) {
      nodes.add(new SeamComponentNodeDescriptor(seamComponent, this, getParameters()));
    }

    return nodes.toArray(new JavaeeNodeDescriptor[nodes.size()]);
  }

  private VirtualFile getJarFile(final VirtualFile file) {
    VirtualFile forJar = JarFileSystem.getInstance().getVirtualFileForJar(file);
    return forJar;
  }

  protected Icon getNewOpenIcon() {
    return Icons.LIBRARY_ICON;
  }

  protected Icon getNewClosedIcon() {
    return Icons.LIBRARY_ICON;
  }

  private static class ModuleLibNodeDescriptor extends JavaeeNodeDescriptor<VirtualFile> {
    private final List<SeamJamComponent> mySeamComponents;
    private final List<XmlFile> myXmlFiles;

    private ModuleLibNodeDescriptor(final Project project,
                                    final NodeDescriptor parentDescriptor,
                                    final Object parameters,
                                    final VirtualFile element,
                                    @Nullable final List<SeamJamComponent> seamComponents,
                                    @Nullable final List<XmlFile> xmlFiles) {
      super(project, parentDescriptor, parameters, element);
      mySeamComponents = seamComponents;
      myXmlFiles = xmlFiles;
    }

    protected String getNewNodeText() {
      return getElement().getName();
    }

    public JavaeeNodeDescriptor[] getChildren() {
      List<JavaeeNodeDescriptor> nodes = new ArrayList<JavaeeNodeDescriptor>();
      if (myXmlFiles != null) {
        for (XmlFile xmlFile : myXmlFiles) {
          nodes.add(new SeamDomModelNodeDescriptor(getProject(), this, getParameters(), xmlFile, JarFileSystem.getInstance().getJarRootForLocalFile(getElement())));
        }
      }
      if (mySeamComponents != null) {
        for (SeamJamComponent seamComponent : mySeamComponents) {
          nodes.add(new SeamComponentNodeDescriptor(seamComponent, this, getParameters()));
        }
      }
      return nodes.toArray(new JavaeeNodeDescriptor[nodes.size()]);
    }

    protected Icon getNewOpenIcon() {
      return getElement().getFileType().getIcon();
    }

    protected Icon getNewClosedIcon() {
      return getElement().getFileType().getIcon();
    }
  }

  public int getWeight() {
    return 10000;
  }
}
