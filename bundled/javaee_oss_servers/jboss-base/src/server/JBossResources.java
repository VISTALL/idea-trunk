/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeResources;
import com.fuhrer.idea.javaee.util.FileWrapper;
import org.jetbrains.annotations.NotNull;

public class JBossResources extends JavaeeResources {

    @Override
    @NotNull
    protected String getResourceUri(FileWrapper file) throws Exception {
        return "http://www.jboss.org/j2ee/dtd/" + file.getName();
    }
}
