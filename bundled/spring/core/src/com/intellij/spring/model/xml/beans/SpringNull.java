/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Namespace;
import com.intellij.spring.constants.SpringConstants;

/**
 * @author peter
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringNull extends GenericDomValue<String>{
}
