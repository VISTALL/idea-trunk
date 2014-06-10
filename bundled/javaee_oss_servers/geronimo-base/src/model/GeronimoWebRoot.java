/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoWebRoot extends GeronimoCommonRoot {

    GenericDomValue<String> getContextRoot();
}
