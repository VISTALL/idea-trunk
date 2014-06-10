/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.intellij.codeInspection.InspectionToolProvider;

class JBossInspections implements InspectionToolProvider {

    public Class<?>[] getInspectionClasses() {
        return new Class[]{JBossInspection.class};
    }
}
