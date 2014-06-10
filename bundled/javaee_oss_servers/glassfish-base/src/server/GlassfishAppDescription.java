/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.descriptor.JavaeeFileDescription;

class GlassfishAppDescription extends JavaeeFileDescription<GlassfishAppRoot> {

    GlassfishAppDescription() {
        super(GlassfishAppRoot.class, "sun-application", JavaeeDescriptor.APP);
    }
}
