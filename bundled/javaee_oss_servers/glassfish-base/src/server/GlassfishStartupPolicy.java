/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.javaee.server.JavaeeParameters;
import com.fuhrer.idea.javaee.server.JavaeeStartupPolicy;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;

import java.io.File;

class GlassfishStartupPolicy extends JavaeeStartupPolicy<GlassfishLocalModel> {

    @Override
    protected void getStartupParameters(JavaeeParameters params, GlassfishLocalModel model, boolean debug) {
        params.add(getScript(model));
        params.add("start-domain");
        if (debug) {
            params.add("--debug");
        }
        params.add(model.DOMAIN);
    }

    @Override
    protected void getShutdownParameters(JavaeeParameters params, GlassfishLocalModel model, boolean debug) {
        params.add(getScript(model));
        params.add("stop-domain");
        params.add(model.DOMAIN);
    }

    @Override
    protected void initSettings(GlassfishLocalModel model, DebuggingRunnerData data) {
        data.setDebugPort(GlassfishDebugConfig.get(model));
    }

    @Override
    protected void checkSettings(GlassfishLocalModel model, DebuggingRunnerData data) throws RuntimeConfigurationException {
        GlassfishDebugConfig.check(model, data.getDebugPort());
    }

    private File getScript(GlassfishLocalModel model) {
        @NonNls String script = SystemInfo.isWindows ? "asadmin.bat" : "asadmin";
        return new File(new File(model.getHome(), "bin"), script);
    }
}
