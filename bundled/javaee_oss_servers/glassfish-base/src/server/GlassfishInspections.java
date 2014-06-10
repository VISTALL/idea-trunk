/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.intellij.codeInspection.InspectionToolProvider;

class GlassfishInspections implements InspectionToolProvider {

    public Class<?>[] getInspectionClasses() {
        return new Class[]{GlassfishInspection.class};
    }
}
