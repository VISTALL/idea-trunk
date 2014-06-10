/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeResources;
import com.fuhrer.idea.javaee.util.FileWrapper;
import com.intellij.xml.index.XsdNamespaceBuilder;
import org.jetbrains.annotations.NotNull;

public class GeronimoResources extends JavaeeResources {

    @Override
    @NotNull
    protected String getResourceUri(FileWrapper file) throws Exception {
      String namespace = XsdNamespaceBuilder.computeNamespace(file.getStream());
      return namespace != null ? namespace : "";
    }
}
