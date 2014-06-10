/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeeParameters;
import com.fuhrer.idea.javaee.server.JavaeeStartupPolicy;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

class JBossStartupPolicy extends JavaeeStartupPolicy<JBossLocalModel> {

    @Override
    protected void getStartupParameters(JavaeeParameters params, JBossLocalModel model, boolean debug) {
        params.add(new File(getScriptDir(model), getStartupScript()));
        params.add("-c", model.SERVER);
    }

    @Override
    protected void getShutdownParameters(JavaeeParameters params, JBossLocalModel model, boolean debug) {
        params.add(new File(getScriptDir(model), getShutdownScript()));
        params.add("-s", "jnp://localhost:" + model.getServerPort());
        params.add("-u", model.USERNAME);
        params.add("-p", model.PASSWORD);
        params.add("-S");
    }

    @Override
    @Nullable
    protected List<EnvironmentVariable> getEnvironmentVariables(JBossLocalModel model) {
        return SystemInfo.isWindows ? Collections.singletonList(new EnvironmentVariable("NOPAUSE", "yes", false)) : null;
    }

    @NonNls
    private File getScriptDir(JBossLocalModel model) {
        return new File(model.getHome(), "bin");
    }

    @NonNls
    private String getStartupScript() {
        return SystemInfo.isWindows ? "run.bat" : "run.sh";
    }

    @NonNls
    private String getShutdownScript() {
        return SystemInfo.isWindows ? "shutdown.bat" : "shutdown.sh";
    }
}
