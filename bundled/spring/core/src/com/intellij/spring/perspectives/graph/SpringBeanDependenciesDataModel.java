/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.NodesGroup;
import com.intellij.openapi.graph.builder.components.BasicNodesGroup;
import com.intellij.openapi.graph.view.NodeLabel;
import com.intellij.openapi.graph.view.hierarchy.GroupNodeRealizer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.SpringAnnotationConfigUtils;
import com.intellij.spring.model.highlighting.SpringAutowireUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpringBeanDependenciesDataModel extends GraphDataModel<SpringBaseBeanPointer, SpringBeanDependencyInfo> {
  private final Set<SpringBaseBeanPointer> myNodes = new THashSet<SpringBaseBeanPointer>();
  private final Set<SpringBeanDependencyInfo> myEdges = new THashSet<SpringBeanDependencyInfo>();

  private final Map<PsiFile, NodesGroup> myGroups = new THashMap<PsiFile, NodesGroup>();
  private final XmlFile myFile;
  private final SpringBeanDependenciesDiagramContext myDiagramContext;

  private final Project myProject;

  public SpringBeanDependenciesDataModel(final XmlFile file, SpringBeanDependenciesDiagramContext diagramContext) {
    myFile = file;
    myDiagramContext = diagramContext;
    myProject = file.getProject();
  }

  @NotNull
  public Collection<SpringBaseBeanPointer> getNodes() {
    refreshDataModel();

    return myNodes;
  }

  @NotNull
  public Collection<SpringBeanDependencyInfo> getEdges() {
    return myEdges;
  }

  @NotNull
  public SpringBaseBeanPointer getSourceNode(final SpringBeanDependencyInfo edge) {
    return edge.getSource();
  }

  @NotNull
  public SpringBaseBeanPointer getTargetNode(final SpringBeanDependencyInfo edge) {
    return edge.getTarget();
  }

  @NotNull
  public String getNodeName(final SpringBaseBeanPointer springBean) {
    return SpringUtils.getPresentationBeanName(SpringUtils.getBasePointer(springBean));
  }

  @NotNull
  public String getEdgeName(final SpringBeanDependencyInfo springBeanDependencyInfo) {
    return springBeanDependencyInfo.getName();
  }

  public SpringBeanDependencyInfo createEdge(@NotNull final SpringBaseBeanPointer from, @NotNull final SpringBaseBeanPointer to) {
    return null;
  }

  public void dispose() {

  }

  private void refreshDataModel() {
    clearAll();

    updateDataModel();
  }

  public void updateDataModel() {
    final SpringModel model = getModel();
    if (model == null) return;

    final Collection<? extends SpringBaseBeanPointer> pointers = model.getAllCommonBeans(true);
    final THashSet<SpringBaseBeanPointer> pointerSet = new THashSet<SpringBaseBeanPointer>(pointers);
    for (SpringBaseBeanPointer pointer : pointers) {
      addNode(pointer);
      final CommonSpringBean springBean = pointer.getSpringBean();
      if (springBean instanceof SpringBean) {
        addSpringBeanDependencies(pointer, model, pointerSet);
      }

      if (myDiagramContext.isShowAutowiredDependencies() && SpringAnnotationConfigUtils.containsAutowiredAnnotationBeanPostProcessor(model)) {
        addAnnotatedAutowring(springBean, model);
      }
    }
  }

  private void addSpringBeanDependencies(final SpringBaseBeanPointer springBeanPointer,
                                         final SpringModel model,
                                         final Set<SpringBaseBeanPointer> modelBeans) {

    addConstructorInjectedDependencies(springBeanPointer, model, modelBeans);
    addLookupMethodInjectedDependencies(springBeanPointer, modelBeans);
    addFactoryBeanDependencies(springBeanPointer, modelBeans);
    addParentBeanDependencies(springBeanPointer, modelBeans);
    if (myDiagramContext.isShowAutowiredDependencies()) {
      addAutowiredDependencies(springBeanPointer, model);
    }
  }

  private void addAutowiredDependencies(final SpringBaseBeanPointer springBeanPointer, final SpringModel model) {
    final PsiClass beanClass = springBeanPointer.getBeanClass();
    if (beanClass == null) return;
    final SpringBean springBean = (SpringBean)springBeanPointer.getSpringBean();

    if (SpringAutowireUtil.isByTypeAutowired(springBean)) {
      addByTypeAutowireDependencies(springBeanPointer, model);
    }
    else if (SpringAutowireUtil.isByNameAutowired(springBean)) {
      addByNameAutowireDependencies(springBeanPointer);
    }
    else if (SpringAutowireUtil.isConstructorAutowire(springBean)) {
      addConstructorAutowireDependencies(springBeanPointer, model);
    }
  }

  private void addAnnotatedAutowring(final CommonSpringBean commonSpringBean, final SpringModel model) {
    final Map<PsiMember, List<SpringBaseBeanPointer>> map = SpringAutowireUtil.getAutowireAnnotationProperties(commonSpringBean, model);

    for (List<SpringBaseBeanPointer> springBeans : map.values()) {
      for (SpringBaseBeanPointer bean : springBeans) {
        addNode(bean);
        addEdge(new SpringBeanDependencyInfo(SpringBeanPointer.createSpringBeanPointer(commonSpringBean), bean,
                                             SpringBeanDependencyInfo.ANNO_AUTOWIRED));
      }
    }
  }

  private void addConstructorAutowireDependencies(final SpringBaseBeanPointer beanPointer, SpringModel model) {
    final Map<PsiType, Collection<SpringBaseBeanPointer>> autowiredProperties =
        SpringAutowireUtil.getConstructorAutowiredProperties((SpringBean)beanPointer.getSpringBean(), model);
    for (Collection<SpringBaseBeanPointer> springBeans : autowiredProperties.values()) {
      for (SpringBaseBeanPointer bean : springBeans) {
        addNode(bean);
        addEdge(new SpringBeanDependencyInfo(beanPointer, bean, SpringBeanDependencyInfo.AUTOWIRE));
      }
    }
  }

  private void addByNameAutowireDependencies(final SpringBaseBeanPointer springBean) {
    final Map<PsiMethod, SpringBaseBeanPointer> autowiredProperties = SpringAutowireUtil.getByNameAutowiredProperties((SpringBean)springBean.getSpringBean());
    for (SpringBaseBeanPointer bean : autowiredProperties.values()) {
      if (bean != null) {
        addNode(bean);
        addEdge(new SpringBeanDependencyInfo(springBean, bean, SpringBeanDependencyInfo.AUTOWIRE));
      }
    }
  }

  private void addByTypeAutowireDependencies(final SpringBaseBeanPointer beanPointer, SpringModel model) {
    final Map<PsiMethod, Collection<SpringBaseBeanPointer>> autowiredProperties =
        SpringAutowireUtil.getByTypeAutowiredProperties((SpringBean)beanPointer.getSpringBean(), model);
    for (Collection<SpringBaseBeanPointer> springBeans : autowiredProperties.values()) {
      for (SpringBaseBeanPointer bean : springBeans) {
        addNode(bean);
        addEdge(new SpringBeanDependencyInfo(beanPointer, bean, SpringBeanDependencyInfo.AUTOWIRE));
      }
    }
  }

  private void addParentBeanDependencies(final SpringBaseBeanPointer springBeanPointer, final Set<SpringBaseBeanPointer> modelBeans) {
    final SpringBaseBeanPointer parentBeanPointer = SpringUtils.getBasePointer( ((SpringBean)springBeanPointer.getSpringBean()).getParentBean().getValue());
    if (parentBeanPointer != null && modelBeans.contains(parentBeanPointer)) {
      addEdge(new SpringBeanDependencyInfo(springBeanPointer, parentBeanPointer, SpringBeanDependencyInfo.PARENT));
    }
  }

  private void addFactoryBeanDependencies(final SpringBaseBeanPointer springBeanPointer, final Set<SpringBaseBeanPointer> modelBeans) {
    final SpringBaseBeanPointer factoryBeanPointer = SpringUtils.getBasePointer(((SpringBean)springBeanPointer.getSpringBean()).getFactoryBean().getValue());
    if (factoryBeanPointer != null && modelBeans.contains(factoryBeanPointer)) {
      addEdge(new SpringBeanDependencyInfo(factoryBeanPointer, springBeanPointer, SpringBeanDependencyInfo.FACTORY_BEAN));
    }
  }

  private void addConstructorInjectedDependencies(final SpringBaseBeanPointer pointer,
                                                  final SpringModel model,
                                                  final Set<SpringBaseBeanPointer> modelBeans) {
    for (SpringValueHolderDefinition arg : SpringUtils.getValueHolders(pointer.getSpringBean())) {
      for (SpringBaseBeanPointer bean : SpringUtils.getSpringValueHolderDependencies(arg)) {
        if (modelBeansContain(modelBeans, bean)) {
          if (!myNodes.contains(bean)) {
            addNode(bean);
            if (bean.getSpringBean() instanceof SpringBean) {
              addSpringBeanDependencies(pointer, model, modelBeans);
            }
          }
          addEdge(new SpringBeanDependencyInfo(pointer, bean, SpringBeanDependencyInfo.CONSTRUCTOR_INJECTION));
        }

      }
    }
  }

  private static boolean modelBeansContain(final Collection<SpringBaseBeanPointer> modelBeans, final SpringBaseBeanPointer bean) {
    if (modelBeans.contains(bean)) return true;

    CommonSpringBean commonSpringBean = bean.getSpringBean();
    if (commonSpringBean instanceof DomSpringBean) {
      DomSpringBean parent = ((DomSpringBean)commonSpringBean).getParentOfType(DomSpringBean.class, true);
      while (parent != null) {
        SpringBaseBeanPointer beanPointer = SpringBeanPointer.createSpringBeanPointer(parent);
        if (modelBeans.contains(beanPointer)) return true;

        parent = parent.getParentOfType(DomSpringBean.class, true);
      }
    }
    return false;
  }

  private void addLookupMethodInjectedDependencies(final SpringBaseBeanPointer beanPointer, final Set<SpringBaseBeanPointer> modelBeans) {
    for (LookupMethod method : ((SpringBean)beanPointer.getSpringBean()).getLookupMethods()) {
      final SpringBaseBeanPointer pointer = SpringUtils.getBasePointer(method.getBean().getValue());
      if (pointer != null) {
        if (modelBeans.contains(pointer)) {
          addEdge(new SpringBeanDependencyInfo(beanPointer, pointer,
                                               SpringBeanDependencyInfo.LOOKUP_METHOD_INJECTION));
        }
      }
    }
  }

  private void clearAll() {
    myNodes.clear();
    myEdges.clear();
  }

  public NodesGroup getGroup(final SpringBaseBeanPointer springBean) {
    if (myDiagramContext.isGroupSpringBeans()) {
      return myGroups.get(springBean.getContainingFile());
    }
    return super.getGroup(springBean);
  }

  private void addNode(SpringBaseBeanPointer springBean) {
    myNodes.add(springBean);
    if (myDiagramContext.isGroupSpringBeans()) {
      final PsiFile file = springBean.getContainingFile();
      if (file != null && !myGroups.containsKey(file)) {
        final String name = file.getName();

        final BasicNodesGroup group = new BasicNodesGroup(name == null ? "noname" : name) {

          @Nullable
          public GroupNodeRealizer getGroupNodeRealizer() {
            final GroupNodeRealizer groupNodeRealizer = super.getGroupNodeRealizer();

            groupNodeRealizer.setFillColor(new Color(239, 239, 239));

            final NodeLabel nodeLabel = groupNodeRealizer.getLabel();
            nodeLabel.setText("      " + getGroupName());
            nodeLabel.setBackgroundColor(Color.GRAY);
            nodeLabel.setModel(NodeLabel.INTERNAL);
            nodeLabel.setPosition(NodeLabel.TOP_RIGHT);


            return groupNodeRealizer;
          }
        };
        group.setClosed(true);

        myGroups.put(file, group);
      }
    }
  }

  private void addEdge(SpringBeanDependencyInfo edge) {
    myEdges.add(edge);
  }

  public Project getProject() {
    return myProject;
  }

  @Nullable
  public SpringModel getModel() {
    final SpringManager springManager = SpringManager.getInstance(getProject());
    return myDiagramContext.isShowLocalModel() ? springManager.getLocalSpringModel(myFile) : springManager.getSpringModelByFile(myFile);
  }

  public SpringBeanDependenciesDiagramContext getDiagramContext() {
    return myDiagramContext;
  }
}
