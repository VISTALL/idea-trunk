/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiClass;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;

public class PopupBeanNodeRenderer extends SpringBeanNodeRenderer {

  public PopupBeanNodeRenderer(@NotNull GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder,
                               ModificationTracker modificationTracker) {
    super(builder, modificationTracker);
  }


  @Override
  protected String getNodeTitle(@NotNull SpringBaseBeanPointer pointer) {
    String nodeName = pointer.getName();
    if (nodeName == null) {
      nodeName = SpringBundle.message("spring.bean.with.unknown.name");
    }
    final PsiClass psiClass = pointer.getBeanClass();
    if (psiClass != null && nodeName.equals(psiClass.getQualifiedName())) {
      nodeName = psiClass.getName();
    }

    return nodeName;
  }

  @Override
  protected boolean isGenerateProperties() {
    return false;
  }
}