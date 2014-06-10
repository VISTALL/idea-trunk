/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeResources;
import com.fuhrer.idea.javaee.util.FileWrapper;
import org.jetbrains.annotations.NotNull;

public class GlassfishResources extends JavaeeResources {

    @Override
    @NotNull
    protected String getResourceUri(FileWrapper file) throws Exception {
        return "http://www.sun.com/software/dtd/appserver/" + file.getName();
    }
}
