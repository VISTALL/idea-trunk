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

package com.intellij.beanValidation.toolWindow;

import com.intellij.ProjectTopics;
import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.toolWindow.tree.BVTreeStructure;
import com.intellij.beanValidation.toolWindow.tree.actions.NodeTypesToggleAction;
import com.intellij.beanValidation.toolWindow.tree.nodes.BVNodeTypes;
import com.intellij.beanValidation.toolWindow.tree.nodes.BVNodesConfig;
import com.intellij.beanValidation.toolWindow.tree.nodes.PsiMemberSimpleNode;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationView extends Wrapper implements DataProvider, Disposable {
  private Project myProject;
  private BVNodesConfig myNodesConfig = new BVNodesConfig();
  private boolean isInitialized;
  private AbstractTreeBuilder myBuilder;
  private SimpleTree myTree;
  private PsiModificationTracker.Listener myModificationHandler;
  private ModuleRootListener myModuleRootListener;
  private ModuleListener myModuleListener;

  public BeanValidationView(Project project) {
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
    return new JamAbstractTreeBuilder(tree, (DefaultTreeModel)tree.getModel(), new BVTreeStructure(myProject, myNodesConfig)) {

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

    actions.add(new NodeTypesToggleAction(builder, myNodesConfig, BVNodeTypes.CONSTRAINTS, BVBundle.message("actions.show.constraints"),
                                          BVIcons.CONSTRAINT_TYPE));
    actions.add(new NodeTypesToggleAction(builder, myNodesConfig, BVNodeTypes.VALIDATORS, BVBundle.message("actions.show.validators"),
                                          BVIcons.CONSTRAINT_VALIDATOR_TYPE));
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
