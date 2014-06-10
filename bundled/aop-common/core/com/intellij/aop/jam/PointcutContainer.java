/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.psi.PsiAnnotation;
import com.intellij.jam.JamStringAttributeElement;

/**
 * @author peter
 */
public interface PointcutContainer {

  PsiAnnotation getAnnotation();

  JamStringAttributeElement<String> getArgNames();

}
