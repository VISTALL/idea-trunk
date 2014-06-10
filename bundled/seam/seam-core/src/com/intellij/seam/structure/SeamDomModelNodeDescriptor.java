package com.intellij.seam.structure;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.SeamDomModel;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SeamDomModelNodeDescriptor extends JavaeeNodeDescriptor<XmlFile> {
  @Nullable private final VirtualFile myParentContent;


  public SeamDomModelNodeDescriptor(final Project project,
                                    final NodeDescriptor parentDescriptor,
                                    final Object parameters,
                                    final XmlFile element,
                                    @Nullable VirtualFile parentContent) {
    super(project, parentDescriptor, parameters, element);

    myParentContent = parentContent;
  }

  protected String getNewNodeText() {
    VirtualFile file = getElement().getVirtualFile();

    return file == null ? "" : file.getPresentableName();
  }

  protected Icon getNewOpenIcon() {
    return  getElement().getIcon(Iconable.ICON_FLAG_OPEN);
  }

  protected Icon getNewClosedIcon() {
    return getElement().getIcon(Iconable.ICON_FLAG_OPEN);
  }

  protected void doUpdate() {
    super.doUpdate();
    final String textExt = getNewNodeTextExt();
    if (textExt != null) {
      addColoredFragment(" (" + getNewNodeTextExt() + ")", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }
  }

  @Nullable
  protected String getNewNodeTextExt() {
    XmlFile xmlFile = getElement();
    if (xmlFile == null || !xmlFile.isValid() || myParentContent == null) return null;

    return VfsUtil.getRelativePath(xmlFile.getVirtualFile(), myParentContent, '/');
  }

  public JavaeeNodeDescriptor[] getChildren() {
    List<JavaeeNodeDescriptor> children = new ArrayList<JavaeeNodeDescriptor>();
    SeamDomModel model = SeamDomModelManager.getInstance(getProject()).getSeamModel(getElement());
    if (model != null) {
      for (SeamDomComponent domComponent : model.getSeamComponents()) {
          children.add(new SeamDomComponentNodeDescriptor(getProject(), this, getParameters(), domComponent));
      }
    }

    return children.toArray(new JavaeeNodeDescriptor[children.size()]);
  }
}
