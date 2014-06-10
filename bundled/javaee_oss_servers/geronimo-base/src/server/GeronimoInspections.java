/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.intellij.codeInspection.InspectionToolProvider;

class GeronimoInspections implements InspectionToolProvider {

    public Class<?>[] getInspectionClasses() {
        return new Class[]{GeronimoInspection.class};
    }
}
