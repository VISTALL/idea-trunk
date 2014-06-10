/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author peter
 */
public interface AopConstants {
  String ASPECT_ANNO = "org.aspectj.lang.annotation.Aspect";
  String POINTCUT_ANNO = "org.aspectj.lang.annotation.Pointcut";

  String BEFORE_ANNO = "org.aspectj.lang.annotation.Before";
  String AFTER_ANNO = "org.aspectj.lang.annotation.After";
  String AFTER_RETURNING_ANNO = "org.aspectj.lang.annotation.AfterReturning";
  String AFTER_THROWING_ANNO = "org.aspectj.lang.annotation.AfterThrowing";
  String AROUND_ANNO = "org.aspectj.lang.annotation.Around";

  String DECLARE_PARENTS_ANNO = "org.aspectj.lang.annotation.DeclareParents";

  @NonNls String ARG_NAMES_PARAM = "argNames";
  @NonNls String RETURNING_PARAM = "returning";
  @NonNls String THROWING_PARAM = "throwing";
  @NonNls String POINTCUT_PARAM = "pointcut";

  @NonNls String DEFAULT_IMPL_PARAM = "defaultImpl";

  String JOIN_POINT = "org.aspectj.lang.JoinPoint";
  String JOIN_POINT_STATIC_PART = "org.aspectj.lang.JoinPoint.StaticPart";
  String PROCEEDING_JOIN_POINT = "org.aspectj.lang.ProceedingJoinPoint";

  Icon POINTCUT_ICON = IconLoader.getIcon("/icons/pointcut.png");
  Icon TO_POINTCUT_ICON = IconLoader.getIcon("/icons/to_pointcut.png");
  Icon FROM_POINTCUT_ICON = IconLoader.getIcon("/icons/from_pointcut.png");

  Icon FROM_ICON = IconLoader.getIcon("/icons/from_arrow.png");
  Icon TO_ICON = IconLoader.getIcon("/icons/to_arrow.png");

  Icon ABSTRACT_ADVICE_ICON = IconLoader.getIcon("/icons/abstract_advice.png");
  Icon BEFORE_ADVICE_ICON = IconLoader.getIcon("/icons/before_advice.png");
  Icon AFTER_ADVICE_ICON = IconLoader.getIcon("/icons/after_advice.png");
  Icon AFTER_THROWING_ADVICE_ICON = IconLoader.getIcon("/icons/after_throwing_advice.png");
  Icon AFTER_RETURNING_ADVICE_ICON = IconLoader.getIcon("/icons/after_returning_advice.png");
  Icon AROUND_ADVICE_ICON = IconLoader.getIcon("/icons/around_advice.png");
  Icon INTRODUCTION_ICON = IconLoader.getIcon("/icons/introduction.png");
}
