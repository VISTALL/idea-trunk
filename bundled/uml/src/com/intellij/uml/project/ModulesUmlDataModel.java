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

package com.intellij.uml.project;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.NodeFactory;
import com.intellij.openapi.graph.layout.NodeLayout;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uml.*;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.ProjectTopics;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.MessageHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.reflect.Method;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlDataModel extends UmlDataModel<ModuleItem> {
  private final ModuleItem myInitialElement;

  private final VirtualFile myEditorFile;
  private final Project project;
  private final Set<ModuleItem> addedByUser = new HashSet<ModuleItem>();
  private final Set<ModuleItem> removedByUser = new HashSet<ModuleItem>();

  private boolean myScheduleRefresh = true;

  public ModulesUmlDataModel(final ModuleItem moduleItem, final VirtualFile file) {
    project = moduleItem.getProject();
    myEditorFile = file;
    myInitialElement = moduleItem;
    addedByUser.add(myInitialElement);
    if (myInitialElement.isModule()) {
      for (ModuleItem item : getDependentModulesAndLibs()) {
        addedByUser.add(item);
      }
    }
    final MessageBusConnection connection = project.getMessageBus().connect(this);
    connection.setDefaultHandler(new MessageHandler() {
      public void handle(final Method event, final Object... params) {
        myScheduleRefresh = true;
      }
    });
    connection.subscribe(ProjectTopics.PROJECT_ROOTS);
    connection.subscribe(ProjectTopics.MODULES);
  }

  private Collection<ModuleItem> getDependentModulesAndLibs() {
    final Set<Module> modules = new HashSet<Module>();
    ModuleUtil.getDependencies(myInitialElement.getModule(), modules);
    final List<ModuleItem> items = new ArrayList<ModuleItem>();
    for (Module module : modules) {
      items.add(new ModuleItem(module));
      //Disable libs for a while
      //for (Library library : getModuleLibraries(module)) {
      //  items.add(new ModuleItem(library, project));
      //}
    }
    return items;
  }

  private final Collection<UmlNode<ModuleItem>> myNodes = new HashSet<UmlNode<ModuleItem>>();
  private final Collection<UmlEdge<ModuleItem>> myEdges = new HashSet<UmlEdge<ModuleItem>>();

  @NotNull
  public Collection<UmlNode<ModuleItem>> getNodes() {
    if (myScheduleRefresh) {
      myScheduleRefresh = false;
      refreshDataModel();
    }

    return myNodes;
  }

  @NotNull
  public Collection<UmlEdge<ModuleItem>> getEdges() {
    return myEdges;
  }


  @NotNull
  public UmlNode<ModuleItem> getSourceNode(final UmlEdge<ModuleItem> edge) {
    return edge.getSource();
  }

  @NotNull
  public UmlNode<ModuleItem> getTargetNode(final UmlEdge<ModuleItem> edge) {
    return edge.getTarget();
  }

  @NotNull
  @NonNls
  public String getNodeName(final UmlNode<ModuleItem> node) {
    return node.getIdentifyingElement().getName();
  }

  @NotNull
  public String getEdgeName(final UmlEdge<ModuleItem> edge) {
    return edge.getName();
  }

  public UmlEdge<ModuleItem> createEdge(@NotNull final UmlNode<ModuleItem> from, @NotNull final UmlNode<ModuleItem> to) {
    return null;
  }

  @Override
  public void removeNode(UmlNode<ModuleItem> node) {
    removeElement(node.getIdentifyingElement());
  }

  @Override
  public void removeEdge(UmlEdge<ModuleItem> edge) {
  }

  @Override
  public boolean hasElement(ModuleItem element) {
    return findNode(element) != null;
  }

  private void refreshDataModel() {
    myNodes.clear();
    myEdges.clear();
    updateDataModel();
  }

  public void removeAllElements() {
    removedByUser.clear();
    addedByUser.clear();
  }

  private boolean isAllowedToShow(ModuleItem item) {
    if (item == null) return false;
    for (ModuleItem moduleItem: removedByUser) {
      if (item.equals(moduleItem)) return false;
    }
    return true;
  }

  public synchronized void updateDataModel() {
    final ModulesUmlProvider umlProvider = (ModulesUmlProvider)Utils.getProvider(Utils.getBuilder(this));
    final Set<ModuleItem> moduleItems = getAllModules();
    final Map<Module, ModulesUmlNode> modules = new HashMap<Module, ModulesUmlNode>();
    final Map<Library, ModulesUmlNode> libs = new HashMap<Library, ModulesUmlNode>();
    for (ModuleItem item : moduleItems) {
      if (isAllowedToShow(item)) {
        final ModulesUmlNode umlNode = new ModulesUmlNode(item, umlProvider);
        myNodes.add(umlNode);
        if (item.isModule()) {
          modules.put(item.getModule(), umlNode);
        } else {
          libs.put(item.getLibrary(), umlNode);
        }
      }
    }

    for (ModulesUmlNode node : modules.values()) {
      final Module module = node.getIdentifyingElement().getModule();
      //final Set<Library> libraries = getModuleLibraries(module);
      //for (Library library : libraries) {
      //  final ModulesUmlNode libNode = libs.get(library);
      //  if (libNode != null) {
      //    addEdge(node, libNode, ModulesUmlRelationships.REALIZATION);
      //  }
      //}

      for (Module dependency : ModuleRootManager.getInstance(module).getDependencies()) {
        final ModulesUmlNode moduleNode = modules.get(dependency);
        if (moduleNode != null) {
          addEdge(node, moduleNode, ModulesUmlRelationships.GENERALIZATION);
        }
      }
    }
  }

  public void addEdge(UmlNode<ModuleItem> from, UmlNode<ModuleItem> to, UmlRelationshipInfo relationship) {
    addEdge(from, to, relationship, myEdges);
  }

  private static void addEdge(UmlNode<ModuleItem> from, UmlNode<ModuleItem> to, UmlRelationshipInfo relationship, Collection<UmlEdge<ModuleItem>> storage) {
    for (UmlEdge<ModuleItem> edge : storage) {
      if (edge.getSource() == from
          && edge.getTarget() == to
          && edge.getRelationship() == relationship) return;
    }
    storage.add(new ModulesUmlEdge(from, to, relationship));
  }

  private Set<ModuleItem> getAllModules() {
    return new HashSet<ModuleItem>(addedByUser);
  }


  @Nullable
  public UmlNode<ModuleItem> findNode(ModuleItem item) {
    for (UmlNode<ModuleItem> node : myNodes) {
      final ModuleItem element = node.getIdentifyingElement();
      if (element.equals(item)) {
        return node;
      }
    }
    return null;
  }

  public boolean contains(ModuleItem item) {
    return findNode(item) != null;
  }

  public void dispose() {
  }

  public void removeElement(final ModuleItem item) {
    addedByUser.remove(item);
    final UmlNode<ModuleItem> node = findNode(item);
    myNodes.remove(node);
    if (node == null) return;

    Collection<UmlEdge> edges = new ArrayList<UmlEdge>();
    for (UmlEdge edge : myEdges) {
      if (edge.getTarget() == node || edge.getSource() == node) {
        edges.add(edge);
      }
    }
    myEdges.removeAll(edges);
  }

  @Nullable
  public UmlNode<ModuleItem> addElement(ModuleItem element) {
    return addElement(element, true);
  }

  @Nullable
  public UmlNode<ModuleItem> addElement(final ModuleItem element, boolean createNodeInBuilder) {
    if (findNode(element) != null) return null;

    final ModulesUmlNode node = new ModulesUmlNode(element, Utils.getProvider(Utils.getBuilder(this)));
    if (createNodeInBuilder) {
      final GraphBuilder<UmlNode, UmlEdge> builder = Utils.getBuilder(this);
      final Point point = UmlUtils.getBestPositionForNode(builder);
      NodeFactory.getInstance().createDraggedNode(builder, node, getNodeName(node), point);
      myNodes.add(node);
      final Node nodeObj = builder.getNode(node);
      if (nodeObj != null) {
        final NodeLayout nodeLayout = builder.getGraph().getNodeLayout(nodeObj);
        if (nodeLayout != null) {
          nodeLayout.setLocation(point.x, point.y);
        }
      }
      //UmlUtils.updateGraph(myBuilder, true, false);
    }
    return node;
  }

  @Nullable
  public ModuleItem getInitialElement() {
    return myInitialElement;
  }

  public VirtualFile getFile() {
    return myEditorFile;
  }

  public static Set<Library> getModuleLibraries(Module module) {
    Set<Library> libs = new HashSet<Library>();
    for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        libs.add(((LibraryOrderEntry)orderEntry).getLibrary());
      }
    }
    return libs;
  }
}
