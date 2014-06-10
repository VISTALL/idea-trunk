/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossValueClass extends JBossPropertyHolder {

    @SubTag(value = "class")
    GenericDomValue<PsiClass> getClassName();

    GenericDomValue<String> getDescription();
}
