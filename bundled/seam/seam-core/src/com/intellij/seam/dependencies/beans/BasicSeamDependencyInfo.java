package com.intellij.seam.dependencies.beans;

import com.intellij.javaee.model.common.CommonModelElement;
import org.jetbrains.annotations.NotNull;

public class  BasicSeamDependencyInfo implements SeamDependencyInfo<CommonModelElement> {
  private final SeamComponentNodeInfo mySource;
  private final SeamComponentNodeInfo myTarget;
  private final String myName;
  private final CommonModelElement myIdentifyingElement;

  public BasicSeamDependencyInfo(final SeamComponentNodeInfo source,
                                    final SeamComponentNodeInfo target,
                                    final String name,
                                    final CommonModelElement identifyingElement) {
    mySource = source;
    myTarget = target;
    myName = name;
    myIdentifyingElement = identifyingElement;
  }

  public SeamComponentNodeInfo getSource() {
    return mySource;
  }

  public SeamComponentNodeInfo getTarget() {
    return myTarget;
  }

  public String getName() {
    return myName;
  }

  @NotNull
  public CommonModelElement getIdentifyingElement() {
    return myIdentifyingElement;
  }
}
