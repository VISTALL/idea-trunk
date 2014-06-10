/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.descriptor.JavaeeFileDescription;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;

class JBossEjbDescription extends JavaeeFileDescription<JBossEjbRoot> {

    JBossEjbDescription() {
        super(JBossEjbRoot.class, "jboss", JavaeeDescriptor.EJB);
    }
}
