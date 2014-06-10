/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.server.JavaeeConfigurationType;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.localRun.ExecutableObjectStartupPolicy;
import org.jetbrains.annotations.NotNull;

class GeronimoConfiguration extends JavaeeConfigurationType {

    @Override
    @NotNull
    protected ServerModel createLocalModel() {
        return new GeronimoLocalModel();
    }

    @Override
    @NotNull
    protected ServerModel createRemoteModel() {
        return new GeronimoRemoteModel();
    }

    @Override
    @NotNull
    protected ExecutableObjectStartupPolicy createStartupPolicy() {
        return new GeronimoStartupPolicy();
    }
}
