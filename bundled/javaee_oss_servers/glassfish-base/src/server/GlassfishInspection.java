/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishCmpRoot;
import com.fuhrer.idea.glassfish.model.GlassfishEjbRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeInspection;
import org.jetbrains.annotations.NotNull;

class GlassfishInspection extends JavaeeInspection {

    @SuppressWarnings({"unchecked"})
    GlassfishInspection() {
        super(GlassfishAppRoot.class, GlassfishEjbRoot.class, GlassfishCmpRoot.class, GlassfishWebRoot.class);
    }

    @NotNull
    public String getShortName() {
        return GlassfishBundle.getText("GlassfishIntegration.name");
    }
}
