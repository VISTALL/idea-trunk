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

package com.intellij.seam.structure;

import com.intellij.ProjectTopics;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.j2ee.HelpID;
import com.intellij.jam.view.JamDeleteProvider;
import com.intellij.jam.view.tree.JamNodeDescriptor;
import com.intellij.jam.view.tree.JamTreeParameters;
import com.intellij.javaee.module.view.JavaeeTreeBuilderImpl;
import com.intellij.javaee.module.view.nodes.J2EEModuleParameters;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.FileStatusListener;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SeamView extends Wrapper implements TypeSafeDataProvider, Disposable {
  private Project myProject;
  private boolean isInitialized;
  private JavaeeTreeBuilderImpl myBuilder;
  private SimpleTree myTree;

  public SeamView(Project project) {
    myProject = project;
    setLayout(new BorderLayout());
    Disposer.register(myProject, this);
  }

  public JComponent getJComponent() {
    if (!isInitialized) {
      myTree = createTree();
      // todo deployment view param
      myBuilder = new JavaeeTreeBuilderImpl(myProject, myTree, new JamNodeDescriptor<Project>(myProject, null, new J2EEModuleParameters(true), myProject) {
        @Override
        public JamNodeDescriptor[] getChildren() {
          final JamNodeDescriptor<Project> parentDescriptor = this;
          final JamTreeParameters parameters = (JamTreeParameters)getParameters();
          final Collection<SeamFacet> facets = ContainerUtil.concat(ModuleManager.getInstance(myProject).getModules(), new Function<Module, Collection<? extends SeamFacet>>() {
            public Collection<SeamFacet> fun(final Module module) {
              return FacetManager.getInstance(module).getFacetsByType(SeamFacet.FACET_TYPE_ID);
            }
          });
          return ContainerUtil.map2Array(facets, JamNodeDescriptor.class, new Function<SeamFacet, JamNodeDescriptor>() {
            public JamNodeDescriptor fun(final SeamFacet facet) {
              return new SeamFacetNodeDescriptor(myProject, facet, parentDescriptor, parameters);
            }
          });
        }

        protected String getNewNodeText() {
          return myProject.getName();
        }
      });

      myBuilder.init();
      Disposer.register(this, myBuilder);

      add(createToolbar(myBuilder), BorderLayout.NORTH);
      add(new JScrollPane(myTree), BorderLayout.CENTER);

      initListeners();

      isInitialized = true;
    }

    return this;
  }


  private void initListeners() {
    final MessageBusConnection connection = myProject.getMessageBus().connect(this);
    connection.subscribe(ProjectTopics.MODIFICATION_TRACKER, new PsiModificationTracker.Listener() {
      public void modificationCountChanged() {
        updateTree();
      }
    });
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        updateTree();
      }
    });
    ProjectWideFacetListenersRegistry.getInstance(myProject).registerListener(SeamFacet.FACET_TYPE_ID, new ProjectWideFacetAdapter<SeamFacet>() {
      public void facetConfigurationChanged(final SeamFacet facet) {
        updateTree();
      }

      @Override
      public void facetRemoved(final SeamFacet facet) {
        updateTree();
      }

      @Override
      public void facetAdded(final SeamFacet facet) {
        updateTree();
      }
    }, this);
    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
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
    });
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      public void before(final List<? extends VFileEvent> events) {
      }

      public void after(final List<? extends VFileEvent> events) {
        updateTree();
      }
    });

    FileStatusManager.getInstance(myProject).addFileStatusListener(new FileStatusListener() {
      public void fileStatusesChanged() {
        updateTree();
      }

      public void fileStatusChanged(@NotNull final VirtualFile virtualFile) {
        updateTree();
      }
    }, this);

    CopyPasteManager.getInstance().addContentChangedListener(new CopyPasteManager.ContentChangedListener() {
      public void contentChanged(final Transferable oldTransferable, final Transferable newTransferable) {
        updateTree();
      }
    }, this);
  }

  private void updateTree() {
    myBuilder.updateFromRootCB();
  }

  private static SimpleTree createTree() {
    SimpleTree simpleTree = new SimpleTree(new DefaultTreeModel(new DefaultMutableTreeNode()));

    simpleTree.setRootVisible(false);
    simpleTree.setShowsRootHandles(true);
    simpleTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    ToolTipManager.sharedInstance().registerComponent(simpleTree);
    TreeUtil.installActions(simpleTree);
    EditSourceOnDoubleClickHandler.install(simpleTree);

    PopupHandler.installPopupHandler(simpleTree, IdeActions.GROUP_J2EE_VIEW_POPUP, ActionPlaces.J2EE_VIEW_POPUP);
    return simpleTree;
  }

  private JComponent createToolbar(@NotNull AbstractTreeBuilder builder) {
    DefaultActionGroup group = new DefaultActionGroup();

    final TreeExpander expander = new DefaultTreeExpander(myTree);
    final CommonActionsManager actionsManager = CommonActionsManager.getInstance();
    group.add(actionsManager.createExpandAllAction(expander, myTree));
    group.add(actionsManager.createCollapseAllAction(expander, myTree));
    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true).getComponent();
  }

  public void dispose() {
  }

  public void calcData(final DataKey key, final DataSink sink) {
    if (DataKeys.HELP_ID.equals(key)) {
      sink.put(DataKeys.HELP_ID, HelpID.J2EE_GENERAL);
      return;
    }
    if (DataKeys.PROJECT.equals(key)) {
      sink.put(DataKeys.PROJECT, myProject);
      return;
    }
    if (DataKeys.DELETE_ELEMENT_PROVIDER.equals(key)) {
      final Set<JamNodeDescriptor> descriptors = myBuilder.getSelectedElements(JamNodeDescriptor.class);
      final List<JamDeleteProvider> providers =
        ContainerUtil.mapNotNull(descriptors, new NullableFunction<JamNodeDescriptor, JamDeleteProvider>() {
          public JamDeleteProvider fun(final JamNodeDescriptor descriptor) {
            if (descriptor.isValid()) {
              final Object provider = descriptor.getDataForElement(key.getName());
              if (provider instanceof JamDeleteProvider) {
                return (JamDeleteProvider)provider;
              }
            }
            return null;
          }
        });
      if (!providers.isEmpty()) {
        sink.put(DataKeys.DELETE_ELEMENT_PROVIDER, providers.size() == 1? providers.get(0) : new JamDeleteProvider(providers));
      }
      return;
    }

    final SimpleNode nodeDescriptor = myTree.getSelectedNode();
    if (nodeDescriptor instanceof JamNodeDescriptor && ((JamNodeDescriptor)nodeDescriptor).isValid()) {
      sink.put(key, ((JamNodeDescriptor)nodeDescriptor).getDataForElement(key.getName()));
    }
  }
}