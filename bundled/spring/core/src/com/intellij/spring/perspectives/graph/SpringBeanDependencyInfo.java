/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;

public class SpringBeanDependencyInfo {

  public final static int UNKNOWN = 0;
  public final static int PROPERTY_INJECTION = 1;
  public final static int CONSTRUCTOR_INJECTION = 2;
  public final static int AUTOWIRE = 3;
  public final static int INNER = 4;
  public final static int PARENT = 5;
  public final static int DEPENDS_ON = 6;
  public final static int FACTORY_BEAN = 7;
  public final static int LOOKUP_METHOD_INJECTION = 8;
  public final static int ANNO_AUTOWIRED = 9;

  private final SpringBaseBeanPointer mySource;
  private final SpringBaseBeanPointer myTarget;
  private final int myType;

  public SpringBeanDependencyInfo(@NotNull SpringBaseBeanPointer source, @NotNull SpringBaseBeanPointer target) {
    this(source, target, UNKNOWN);
  }

  public SpringBeanDependencyInfo(final SpringBaseBeanPointer source, final SpringBaseBeanPointer target, int type) {
    mySource = source;
    myTarget = target;
    myType = type;
  }

  public SpringBaseBeanPointer getSource() {
    return mySource;
  }

  public SpringBaseBeanPointer getTarget() {
    return myTarget;
  }


  public int getType() {
    return myType;
  }

  public String getName() {
    switch (myType) {
      case PROPERTY_INJECTION:
      case CONSTRUCTOR_INJECTION:
      case LOOKUP_METHOD_INJECTION:
        return SpringBundle.message("spring.bean.dependency.graph.edge.injected");
      case FACTORY_BEAN:
        return SpringBundle.message("spring.bean.dependency.graph.edge.creates");
      case DEPENDS_ON:
        return SpringBundle.message("spring.bean.dependency.graph.edge.depends.on");
      case AUTOWIRE:
        return SpringBundle.message("spring.bean.dependency.graph.edge.autowired");
      case ANNO_AUTOWIRED:
        return SpringBundle.message("spring.bean.dependency.graph.edge.anno.autowired");
      case PARENT:
        return SpringBundle.message("spring.bean.dependency.graph.edge.inherits");
    }

    return "";
  }


  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SpringBeanDependencyInfo that = (SpringBeanDependencyInfo)o;

    if (!mySource.equals(that.mySource)) return false;
    if (!myTarget.equals(that.myTarget)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = mySource.hashCode();
    result = 31 * result + myTarget.hashCode();

    return result;
  }
}
