/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.aop;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Namespace;
import com.intellij.spring.constants.SpringConstants;

/**
 * @author peter
 */
@Namespace(SpringConstants.AOP_NAMESPACE_KEY)
public interface SpringAopElement extends DomElement {
}
