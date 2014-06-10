/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.util.EnvironmentVariable;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.localRun.*;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class JavaeeStartupPolicy<T extends JavaeeServerModel> implements ExecutableObjectStartupPolicy {

    @Deprecated
    @Nullable
    public ScriptsHelper getStartupHelper() {
        return null;
    }

    @Deprecated
    @Nullable
    public ScriptsHelper getShutdownHelper() {
        return null;
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    public ScriptHelper createStartupScriptHelper(final ProgramRunner runner) {
        return new ScriptHelper() {
            @Override
            public ExecutableObject getDefaultScript(CommonModel config) {
                JavaeeParameters params = new JavaeeParameters();
                getStartupParameters(params, getServerModel(config), isDebug(runner));
                return new CommandLineExecutableObject(params.get(), null);
            }

            @Override
            @SuppressWarnings({"RawUseOfParameterizedType"})
            public void initRunnerSettings(RunnerSettings settings) {
                RunProfile config = settings.getRunProfile();
                JDOMExternalizable data = settings.getData();
                if ((config instanceof CommonModel) && (data instanceof DebuggingRunnerData)) {
                    initSettings(getServerModel((CommonModel) config), (DebuggingRunnerData) data);
                }
            }

            @Override
            @SuppressWarnings({"RawUseOfParameterizedType"})
            public void checkRunnerSettings(RunnerSettings settings) throws RuntimeConfigurationException {
                RunProfile config = settings.getRunProfile();
                JDOMExternalizable data = settings.getData();
                if ((config instanceof CommonModel) && (data instanceof DebuggingRunnerData)) {
                    checkSettings(getServerModel((CommonModel) config), (DebuggingRunnerData) data);
                }
            }
        };
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    public ScriptHelper createShutdownScriptHelper(final ProgramRunner runner) {
        return new ScriptHelper() {
            @Override
            public ExecutableObject getDefaultScript(CommonModel config) {
                JavaeeParameters params = new JavaeeParameters();
                getShutdownParameters(params, getServerModel(config), isDebug(runner));
                return new CommandLineExecutableObject(params.get(), null);
            }
        };
    }

    public EnvironmentHelper getEnvironmentHelper() {
        return new EnvironmentHelper() {
            @Override
            public String getDefaultJavaVmEnvVariableName(CommonModel config) {
                return "JAVA_OPTS";
            }

            @Override
            public List<EnvironmentVariable> getAdditionalEnvironmentVariables(CommonModel config) {
                return getEnvironmentVariables(getServerModel(config));
            }
        };
    }

    @NonNls
    protected abstract void getStartupParameters(JavaeeParameters params, T model, boolean debug);

    @NonNls
    protected abstract void getShutdownParameters(JavaeeParameters params, T model, boolean debug);

    @Nullable
    @NonNls
    protected List<EnvironmentVariable> getEnvironmentVariables(T model) {
        return null;
    }

    protected void initSettings(T model, DebuggingRunnerData data) {
    }

    protected void checkSettings(T model, DebuggingRunnerData data) throws RuntimeConfigurationException {
    }

    protected void add(List<String> list, @NonNls String... parameters) {
        for (String parameter : parameters) {
            if (StringUtil.isEmpty(parameter)) {
                return;
            }
        }
        list.addAll(Arrays.asList(parameters));
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    private boolean isDebug(ProgramRunner runner) {
        return DebuggingRunnerData.DEBUGGER_RUNNER_ID.equals(runner.getRunnerId());
    }

    @SuppressWarnings({"unchecked"})
    private T getServerModel(CommonModel config) {
        return (T) config.getServerModel();
    }
}
