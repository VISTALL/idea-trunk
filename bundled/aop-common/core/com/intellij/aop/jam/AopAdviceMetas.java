/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import static com.intellij.aop.jam.AopConstants.*;
import com.intellij.jam.reflect.JamAnnotationArchetype;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.psi.PsiParameter;

/**
 * @author peter
 */
public abstract class AopAdviceMetas {
  public static final JamStringAttributeMeta.Single<String> VALUE_ATTR = JamAttributeMeta.singleString("value");
  public static final JamStringAttributeMeta.Single<String> ARG_NAMES_ATTR = JamAttributeMeta.singleString("argNames");

  static final JamAnnotationArchetype COMMON_ADVICE_META = new JamAnnotationArchetype().
    addAttribute(VALUE_ATTR).
    addAttribute(ARG_NAMES_ATTR);

  private static final JamAnnotationArchetype POINTCUTTED_ADVICE_META = new JamAnnotationArchetype(COMMON_ADVICE_META).
    addAttribute(AopAdviceWithPointcutAttribute.POINTCUT_ATTR);

  public static final JamStringAttributeMeta.Single<PsiParameter> THROWING_META =
    JamAttributeMeta.singleString(THROWING_PARAM, MethodParameterConverter.INSTANCE);

  public static final JamStringAttributeMeta.Single<PsiParameter> RETURNING_META =
    JamAttributeMeta.singleString(RETURNING_PARAM, MethodParameterConverter.INSTANCE);

  public static final JamAnnotationMeta BEFORE_META = new JamAnnotationMeta(BEFORE_ANNO, COMMON_ADVICE_META);
  public static final JamAnnotationMeta AFTER_META = new JamAnnotationMeta(AFTER_ANNO, COMMON_ADVICE_META);
  public static final JamAnnotationMeta AROUND_META = new JamAnnotationMeta(AROUND_ANNO, COMMON_ADVICE_META);
  public static final JamAnnotationMeta AFTER_RETURNING_META = new JamAnnotationMeta(AFTER_RETURNING_ANNO, POINTCUTTED_ADVICE_META).addAttribute(RETURNING_META);
  public static final JamAnnotationMeta AFTER_THROWING_META = new JamAnnotationMeta(AFTER_THROWING_ANNO, POINTCUTTED_ADVICE_META).addAttribute(THROWING_META);

}
