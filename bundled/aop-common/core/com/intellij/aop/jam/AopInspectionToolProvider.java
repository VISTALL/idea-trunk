/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author peter
 */
public class AopInspectionToolProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{ArgNamesErrorsInspection.class,
        ArgNamesWarningsInspection.class,
        DeclareParentsInspection.class,
        PointcutMethodStyleInspection.class,
        AroundAdviceStyleInspection.class};
  }
}
