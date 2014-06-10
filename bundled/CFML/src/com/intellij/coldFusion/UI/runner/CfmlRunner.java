package com.intellij.coldFusion.UI.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 14.04.2009
 * Time: 16:20:39
 * To change this template use File | Settings | File Templates.
 */
public class CfmlRunner extends GenericProgramRunner {

    protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state, RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
        final CfmlRunConfiguration runProfile = (CfmlRunConfiguration) env.getRunProfile();

        for (Module module : ModuleManager.getInstance(project).getModules()) {
            BrowserUtil.launchBrowser(runProfile.getModuleURL(module));
        }
        return null;
    }


    @NotNull
    public String getRunnerId() {
        return "CfmlRunner";
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return profile instanceof CfmlRunConfiguration;
    }
}
