package com.intellij.spring.perspectives.graph;

/**
 * @author Serega.Vasiliev
 */
public class SpringBeanDependenciesDiagramContext {
  public static final SpringBeanDependenciesDiagramContext DEFAULT = new SpringBeanDependenciesDiagramContext();

  private SpringBeanNodeType myNodeType = SpringBeanNodeType.SIMPLE;

  private boolean myShowLocalModel = true;
  private boolean myShowAutowiredDependencies = true;
  private boolean myGroupSpringBeans = false;
  private boolean myShowEdgeLabels = false;
  private boolean myFilterDependenciesMode = false;

  public boolean isFilterDependenciesMode() {
    return myFilterDependenciesMode;
  }

  public void setFilterDependenciesMode(boolean filterDependenciesMode) {
    myFilterDependenciesMode = filterDependenciesMode;
  }

  public boolean isShowEdgeLabels() {
    return myShowEdgeLabels;
  }

  public void setShowEdgeLabels(boolean showEdgeLabels) {
    myShowEdgeLabels = showEdgeLabels;
  }

  public boolean isShowAutowiredDependencies() {
    return myShowAutowiredDependencies;
  }

  public void setShowAutowiredDependencies(final boolean showAutowiredDependencies) {
    myShowAutowiredDependencies = showAutowiredDependencies;
  }

  public boolean isShowLocalModel() {
    return myShowLocalModel;
  }

  public void setShowLocalModel(final boolean showLocalModel) {
    myShowLocalModel = showLocalModel;
  }

  public boolean isGroupSpringBeans() {
    return myGroupSpringBeans;
  }

  public void setGroupSpringBeans(final boolean groupSpringBeans) {
    myGroupSpringBeans = groupSpringBeans;
  }

  public SpringBeanNodeType getRenderedNodeType() {
    return myNodeType;
  }

  public void setRenderedNodeType(SpringBeanNodeType nodeType) {
    myNodeType = nodeType;
  }
}
