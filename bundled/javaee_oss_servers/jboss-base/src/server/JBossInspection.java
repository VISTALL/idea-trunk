/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeInspection;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import org.jetbrains.annotations.NotNull;

class JBossInspection extends JavaeeInspection {

    @SuppressWarnings({"unchecked"})
    JBossInspection() {
        super(JBossAppRoot.class, JBossEjbRoot.class, JBossCmpRoot.class, JBossWebRoot.class);
    }

    @NotNull
    public String getShortName() {
        return JBossBundle.getText("JBossIntegration.name");
    }
}
