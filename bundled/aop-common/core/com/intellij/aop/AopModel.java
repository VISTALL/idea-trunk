/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import java.util.List;

/**
 * @author peter
 */
public interface AopModel {
  List<? extends AopAspect> getAspects();

  List<? extends AopPointcut> getPointcuts();
}
