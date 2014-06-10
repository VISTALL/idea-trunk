package com.intellij.webBeans.toolWindow;

import com.intellij.ProjectTopics;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.jam.view.tree.JamAbstractTreeBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.StatusBarProgress;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.toolWindow.tree.WebBeansTreeStructure;
import com.intellij.webBeans.toolWindow.tree.actions.NodeTypesToggleAction;
import com.intellij.webBeans.toolWindow.tree.nodes.PsiMemberSimpleNode;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansNodeTypes;
import com.intellij.webBeans.toolWindow.tree.nodes.WebBeansNodesConfig;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;

public class WebBeansView extends Wrapper implements DataProvider, Disposable {
  private Project myProject;
  private WebBeansNodesConfig myNodesConfig = new WebBeansNodesConfig();
  private boolean isInitialized;
  private AbstractTreeBuilder myBuilder;
  private SimpleTree myTree;
  private PsiModificationTracker.Listener myModificationHandler;
  private ModuleRootListener myModuleRootListener;
  private ModuleListener myModuleListener;

  public WebBeansView(Project project) {
    myProject = project;
    setLayout(new BorderLayout());
    Disposer.register(myProject, this);
  }

  public JComponent getJComponent() {
    if (!isInitialized) {
      myTree = createTree();

      myBuilder = createBuilder(myTree);

      myBuilder.initRootNode();
      Disposer.register(this, myBuilder);

      add(createToolbar(myBuilder), BorderLayout.NORTH);
      add(new JScrollPane(myTree), BorderLayout.CENTER);

      addListeners();

      isInitialized = true;
    }

    return this;
  }

  private void addListeners() {
    final MessageBusConnection connection = myProject.getMessageBus().connect();

    initListeners();

    connection.subscribe(ProjectTopics.MODIFICATION_TRACKER, myModificationHandler);
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, myModuleRootListener);
    connection.subscribe(ProjectTopics.MODULES, myModuleListener);
  }

  private void initListeners() {
    myModificationHandler = new PsiModificationTracker.Listener() {
      public void modificationCountChanged() {
        updateTree();
      }
    };


    myModuleRootListener = new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        updateTree();
      }
    };

    myModuleListener = new ModuleListener() {
      public void moduleAdded(Project project, Module module) {
        updateTree();
      }

      public void beforeModuleRemoved(Project project, Module module) {
      }

      public void moduleRemoved(Project project, Module module) {
        updateTree();
      }

      public void modulesRenamed(Project project, List<Module> modules) {
        updateTree();
      }
    };
  }

  private void updateTree() {
    myBuilder.updateFromRoot();
  }

  private AbstractTreeBuilder createBuilder(final JTree tree) {
    return new JamAbstractTreeBuilder(tree, (DefaultTreeModel)tree.getModel(), new WebBeansTreeStructure(myProject, myNodesConfig)) {

      @NotNull
      protected ProgressIndicator createProgressIndicator() {
        return new StatusBarProgress();
      }
    };
  }


  private static SimpleTree createTree() {
    SimpleTree simpleTree = new SimpleTree(new DefaultTreeModel(new DefaultMutableTreeNode()));

    simpleTree.setRootVisible(false);
    simpleTree.setShowsRootHandles(true);
    simpleTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    ToolTipManager.sharedInstance().registerComponent(simpleTree);
    TreeUtil.installActions(simpleTree);
    EditSourceOnDoubleClickHandler.install(simpleTree);

    return simpleTree;
  }

  private JComponent createToolbar(@NotNull AbstractTreeBuilder builder) {
    DefaultActionGroup actions = new DefaultActionGroup();

    actions.add(
      new NodeTypesToggleAction(builder, myNodesConfig, WebBeansNodeTypes.BINDING, WebBeansBundle.message("actions.show.binding.types"),
                                WebBeansIcons.WEB_BEAN));
    actions.add(
      new NodeTypesToggleAction(builder, myNodesConfig, WebBeansNodeTypes.DEPLOYMENT, WebBeansBundle.message("actions.show.deployment"),
                                WebBeansIcons.DEPLOYMENT_TYPES));
    actions.add(new NodeTypesToggleAction(builder, myNodesConfig, WebBeansNodeTypes.SCOPE, WebBeansBundle.message("actions.show.scope"),
                                          WebBeansIcons.SCOPE_TYPES));
    actions.add(
      new NodeTypesToggleAction(builder, myNodesConfig, WebBeansNodeTypes.INTERCEPTOR, WebBeansBundle.message("actions.show.interceptors"),
                                WebBeansIcons.INTERCEPTOR_TYPES));
    actions.addSeparator();

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true).getComponent();
  }

  public void dispose() {
  }

  public Object getData(@NonNls String dataId) {
    final SimpleNode simpleNode = myTree.getSelectedNode();

    if (PlatformDataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId)) {
      if (simpleNode instanceof PsiMemberSimpleNode) {
        final PsiMember psiMember = ((PsiMemberSimpleNode)simpleNode).getMember();
        if (psiMember != null && psiMember.isValid()) {
          return new Navigatable[]{psiMember};
        }
      }
    }
    return null;
  }
}
