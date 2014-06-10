/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;

class GeronimoAppDescription extends GeronimoFileDescription<GeronimoAppRoot> {

    GeronimoAppDescription() {
        super(GeronimoAppRoot.class, "application", JavaeeDescriptor.APP);
    }
}
