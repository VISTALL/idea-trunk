/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;

class GeronimoWebDescription extends GeronimoFileDescription<GeronimoWebRoot> {

    GeronimoWebDescription() {
        super(GeronimoWebRoot.class, "web-app", JavaeeDescriptor.WEB);
    }
}
