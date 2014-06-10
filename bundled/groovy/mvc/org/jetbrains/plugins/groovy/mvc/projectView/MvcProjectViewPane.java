/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ProjectTopics;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.ide.*;
import com.intellij.ide.projectView.BaseProjectTreeBuilder;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.*;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeUpdater;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.actions.ModuleDeleteProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Krasislchikov
 */
public class MvcProjectViewPane extends AbstractProjectViewPSIPane implements IdeView {
  private final CopyPasteDelegator myCopyPasteDelegator;
  private final JPanel myComponent;
  private final DeleteProvider myDeletePSIElementProvider;
  private final ModuleDeleteProvider myDeleteModuleProvider = new ModuleDeleteProvider();

  @NonNls private final String myId;
  private final MvcToolWindowDescriptor myDescriptor;

  public MvcProjectViewPane(final Project project, MvcToolWindowDescriptor descriptor) {
    super(project);
    myDescriptor = descriptor;
    myId = descriptor.getToolWindowId();

    project.getMessageBus().connect(this).subscribe(ProjectTopics.MODIFICATION_TRACKER, new PsiModificationTracker.Listener() {
      public void modificationCountChanged() {
        if (getTree() != null) {
          updateFromRoot(true);
        }
      }
    });

    myComponent = new JPanel(new BorderLayout());
    myComponent.add(createComponent(), BorderLayout.CENTER);
    myComponent.add(createToolbar(), BorderLayout.NORTH);
    myComponent.putClientProperty(DataManager.CLIENT_PROPERTY_DATA_PROVIDER, this);

    myCopyPasteDelegator = new CopyPasteDelegator(project, myComponent) {
      @NotNull
      @Override
      protected PsiElement[] getSelectedElements() {
        return MvcProjectViewPane.this.getSelectedPSIElements();
      }
    };
    myDeletePSIElementProvider = new DeleteHandler.DefaultDeleteProvider();
  }

  private JComponent createToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();

    final TreeExpander expander = new DefaultTreeExpander(myTree);
    final CommonActionsManager actionsManager = CommonActionsManager.getInstance();
    group.addAction(new ScrollFromSourceAction());
    group.add(actionsManager.createCollapseAllAction(expander, myTree));

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true).getComponent();
  }


  public JPanel getComponent() {
    return myComponent;
  }

  public String getTitle() {
    throw new UnsupportedOperationException();
  }

  public Icon getIcon() {
    return myDescriptor.getFramework().getIcon();
  }

  @NotNull
  public String getId() {
    return myId;
  }

  public int getWeight() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInitiallyVisible() {
    throw new UnsupportedOperationException();
  }

  public SelectInTarget createSelectInTarget() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  protected BaseProjectTreeBuilder createBuilder(final DefaultTreeModel treeModel) {
    return new ProjectTreeBuilder(myProject, myTree, treeModel, null, (ProjectAbstractTreeStructureBase)myTreeStructure) {
      protected AbstractTreeUpdater createUpdater() {
        return createTreeUpdater(this);
      }
    };
  }

  protected ProjectAbstractTreeStructureBase createStructure() {
    final Project project = myProject;
    final String id = getId();
    return new ProjectTreeStructure(project, id) {
      protected AbstractTreeNode createRoot(final Project project, ViewSettings settings) {
        return new MvcProjectNode(project, this, myDescriptor);
      }
    };
  }

  protected ProjectViewTree createTree(final DefaultTreeModel treeModel) {
    return new ProjectViewTree(treeModel) {
      public String toString() {
        return myDescriptor.getFramework().getDisplayName() + " " + super.toString();
      }

      public DefaultMutableTreeNode getSelectedNode() {
        return MvcProjectViewPane.this.getSelectedNode();
      }
    };
  }

  protected AbstractTreeUpdater createTreeUpdater(final AbstractTreeBuilder treeBuilder) {
    return new AbstractTreeUpdater(treeBuilder);
  }

  @Nullable
  protected PsiElement getPSIElement(@Nullable final Object element) {
    // E.g is used by Project View's DataProvider
   if (element instanceof NodeId) {
      final PsiElement psiElement = ((NodeId)element).getPsiElement();
      if (psiElement != null && psiElement.isValid()) {
        return psiElement;
      }
    }
    return super.getPSIElement(element);
  }

  @Override
  public Object getData(String dataId) {
    if (DataConstants.PSI_ELEMENT.equals(dataId)) {
      final PsiElement[] elements = getSelectedPSIElements();
      return elements.length == 1 ? elements[0] : null;
    }
    if (DataConstants.PSI_ELEMENT_ARRAY.equals(dataId)) {
      return getSelectedPSIElements();
    }
    if (DataConstants.MODULE_CONTEXT.equals(dataId)) {
      final Object element = getSelectedElement();
      if (element instanceof Module) {
        return element;
      }
      return null;
    }
    if (DataConstants.MODULE_CONTEXT_ARRAY.equals(dataId)) {
      final List<Module> moduleList = ContainerUtil.findAll(getSelectedElements(), Module.class);
      if (!moduleList.isEmpty()) {
        return moduleList.toArray(new Module[moduleList.size()]);
      }
      return null;
    }
    if (dataId.equals(DataConstants.IDE_VIEW)) {
      return this;
    }
    if (dataId.equals(DataConstants.HELP_ID)) {
      return "reference.toolwindows." + myId.toLowerCase();
    }
    if (DataConstants.CUT_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getCutProvider();
    }
    if (DataConstants.COPY_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getCopyProvider();
    }
    if (DataConstants.PASTE_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getPasteProvider();
    }
    if (DataConstants.DELETE_ELEMENT_PROVIDER.equals(dataId)) {
      for (final Object element : getSelectedElements()) {
        if (element instanceof Module) {
          return myDeleteModuleProvider;
        }
      }
      return myDeletePSIElementProvider;
    }
    return super.getData(dataId);
  }

  @Nullable
  public static MvcProjectViewPane getView(final Project project, MvcFramework framework) {
    final ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(framework.getFrameworkName());
    final Content content = window == null ? null : window.getContentManager().getContent(0);
    return content == null ? null : (MvcProjectViewPane)content.getDisposer();
  }

  public void selectElement(PsiElement element) {
  }

  public PsiDirectory[] getDirectories() {
    return getSelectedDirectories();
  }

  public PsiDirectory getOrChooseDirectory() {
    return DirectoryChooserUtil.getOrChooseDirectory(this);
  }

  @Nullable
  private List<Object> getSelectPath(VirtualFile file) {
    if (file == null) {
      return null;
    }

    final Module module = ModuleUtil.findModuleForFile(file, myProject);
    if (module == null || !myDescriptor.getFramework().hasSupport(module)) {
      return null;
    }
    List<Object> result = new ArrayList<Object>();

    final MvcProjectViewPane view = getView(myProject, myDescriptor.getFramework());
    if (view == null) {
      return null;
    }

    final MvcProjectNode root = (MvcProjectNode)view.getTreeBuilder().getTreeStructure().getRootElement();
    result.add(root);

    for (AbstractTreeNode moduleNode : root.getChildren()) {
      if (moduleNode.getValue() == module) {
        result.add(moduleNode);

        AbstractTreeNode<?> cur = moduleNode;

        path:
        while (true) {
          for (AbstractTreeNode descriptor : cur.getChildren()) {
            if (descriptor instanceof AbstractFolderNode) {
              final AbstractFolderNode folderNode = (AbstractFolderNode)descriptor;
              final VirtualFile dir = folderNode.getVirtualFile();
              if (dir != null && VfsUtil.isAncestor(dir, file, false)) {
                cur = folderNode;
                result.add(folderNode);
                if (dir.equals(file)) {
                  return result;
                }
                continue path;
              }
            }
            if (descriptor instanceof AbstractMvcPsiNodeDescriptor) {
              if (file.equals(((AbstractMvcPsiNodeDescriptor)descriptor).getVirtualFile())) {
                result.add(descriptor);
                return result;
              }
            }
          }
          return null;
        }
      }
    }
    return null;
  }

  public boolean canSelectFile(VirtualFile file) {
    return getSelectPath(file) != null;
  }

  public void selectFile(VirtualFile file, boolean requestFocus) {
    final List<Object> path = getSelectPath(file);
    assert path != null;
    final Object value = ((AbstractTreeNode)path.get(path.size() - 1)).getValue();
    select(value, file, requestFocus);
  }

  private class ScrollFromSourceAction extends AnAction implements DumbAware {
    private ScrollFromSourceAction() {
      super("Scroll from Source", "Select the file open in the active editor", IconLoader.getIcon("/general/autoscrollFromSource.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      final VirtualFile file = e.getData(DataKeys.VIRTUAL_FILE);
      if (file != null && canSelectFile(file)) {
        selectFile(file, false);
      }
    }
  }

}
