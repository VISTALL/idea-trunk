/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.javaee.server.JavaeeConfigurationType;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.localRun.ExecutableObjectStartupPolicy;
import org.jetbrains.annotations.NotNull;

class GlassfishConfiguration extends JavaeeConfigurationType {

    @Override
    @NotNull
    protected ServerModel createLocalModel() {
        return new GlassfishLocalModel();
    }

    @Override
    @NotNull
    protected ServerModel createRemoteModel() {
        return new GlassfishRemoteModel();
    }

    @Override
    @NotNull
    protected ExecutableObjectStartupPolicy createStartupPolicy() {
        return new GlassfishStartupPolicy();
    }
}
