/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.descriptor.JavaeeFileDescription;
import com.fuhrer.idea.jboss.model.JBossAppRoot;

class JBossAppDescription extends JavaeeFileDescription<JBossAppRoot> {

    JBossAppDescription() {
        super(JBossAppRoot.class, "jboss-app", JavaeeDescriptor.APP);
    }
}
