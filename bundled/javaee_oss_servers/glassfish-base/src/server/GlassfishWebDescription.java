/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.descriptor.JavaeeFileDescription;

class GlassfishWebDescription extends JavaeeFileDescription<GlassfishWebRoot> {

    GlassfishWebDescription() {
        super(GlassfishWebRoot.class, "sun-web-app", JavaeeDescriptor.WEB);
    }
}
