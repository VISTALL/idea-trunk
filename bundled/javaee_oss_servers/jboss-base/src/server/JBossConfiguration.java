/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeConfigurationType;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.localRun.ExecutableObjectStartupPolicy;
import org.jetbrains.annotations.NotNull;

class JBossConfiguration extends JavaeeConfigurationType {

    @Override
    @NotNull
    protected ServerModel createLocalModel() {
        return new JBossLocalModel();
    }

    @Override
    @NotNull
    protected ServerModel createRemoteModel() {
        return new JBossRemoteModel();
    }

    @Override
    @NotNull
    protected ExecutableObjectStartupPolicy createStartupPolicy() {
        return new JBossStartupPolicy();
    }
}
