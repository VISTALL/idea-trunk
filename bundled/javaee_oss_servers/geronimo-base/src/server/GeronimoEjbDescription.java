/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;

class GeronimoEjbDescription extends GeronimoFileDescription<GeronimoEjbRoot> {

    GeronimoEjbDescription() {
        super(GeronimoEjbRoot.class, "openejb-jar", JavaeeDescriptor.EJB);
    }
}
