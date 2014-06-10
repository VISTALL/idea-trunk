/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.server.JavaeeParameters;
import com.fuhrer.idea.javaee.server.JavaeeStartupPolicy;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;

import java.io.File;

class GeronimoStartupPolicy extends JavaeeStartupPolicy<GeronimoLocalModel> {

    @Override
    protected void getStartupParameters(JavaeeParameters params, GeronimoLocalModel model, boolean debug) {
        params.add(getScript(model));
        params.add("run");
        params.add("--quiet");
    }

    @Override
    protected void getShutdownParameters(JavaeeParameters params, GeronimoLocalModel model, boolean debug) {
        params.add(getScript(model));
        params.add("stop");
        params.add("--user", model.USERNAME);
        params.add("--password", model.PASSWORD);
        params.add("--port", String.valueOf(model.getServerPort()));
    }

    private File getScript(GeronimoLocalModel model) {
        @NonNls String script = SystemInfo.isWindows ? "geronimo.bat" : "geronimo.sh";
        return new File(new File(model.getHome(), "bin"), script);
    }
}
