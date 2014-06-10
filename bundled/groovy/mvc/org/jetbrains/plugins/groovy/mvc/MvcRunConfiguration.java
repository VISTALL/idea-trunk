/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.plugins.groovy.mvc;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author peter
 */
public abstract class MvcRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> {
  public String vmParams;
  public String cmdLine;
  public boolean depsClasspath = true;
  private final MvcFramework myFramework;

  public MvcRunConfiguration(final String name, final RunConfigurationModule configurationModule, final ConfigurationFactory factory, MvcFramework framework) {
    super(name, configurationModule, factory);
    myFramework = framework;
  }

  public MvcFramework getFramework() {
    return myFramework;
  }

  public Collection<Module> getValidModules() {
    Module[] modules = ModuleManager.getInstance(getProject()).getModules();
    ArrayList<Module> res = new ArrayList<Module>();
    for (Module module : modules) {
      if (myFramework.hasSupport(module)) {
        res.add(module);
      }
    }
    return res;
  }

  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);
    vmParams = JDOMExternalizer.readString(element, "vmparams");
    cmdLine = JDOMExternalizer.readString(element, "cmdLine");
    depsClasspath = !"false".equals(JDOMExternalizer.readString(element, "depsClasspath"));
  }

  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);
    JDOMExternalizer.write(element, "vmparams", vmParams);
    JDOMExternalizer.write(element, "cmdLine", cmdLine);
    JDOMExternalizer.write(element, "depsClasspath", depsClasspath);
  }

  protected abstract String getNoSdkMessage();

  public void checkConfiguration() throws RuntimeConfigurationException {
    final Module module = getModule();
    if (module == null) {
      throw new RuntimeConfigurationException("Module not specified");
    }
    if (module.isDisposed()) {
      throw new RuntimeConfigurationException("Module is disposed");
    }
    if (!myFramework.hasSupport(module)) {
      throw new RuntimeConfigurationException(getNoSdkMessage());
    }
    super.checkConfiguration();
  }

  @Nullable
  public Module getModule() {
    return getConfigurationModule().getModule();
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    final Module module = getModule();
    if (module == null) {
      throw new ExecutionException("Module is not specified");
    }

    if (myFramework.getSdkRoot(module) == null) {
      throw new ExecutionException(getNoSdkMessage());
    }

    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final Sdk sdk = rootManager.getSdk();
    if (sdk == null || !(sdk.getSdkType() instanceof JavaSdkType)) {
      throw CantRunException.noJdkForModule(module);
    }

    final JavaCommandLineState state = createCommandLineState(environment, module);
    state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
    return state;

  }

  protected MvcCommandLineState createCommandLineState(@NotNull ExecutionEnvironment environment, Module module) {
    return new MvcCommandLineState(environment, false);
  }

  public MvcRunConfigurationEditor getConfigurationEditor() {
    return new MvcRunConfigurationEditor();
  }

  protected class MvcCommandLineState extends JavaCommandLineState {
    private final boolean myForTests;

    public MvcCommandLineState(@NotNull ExecutionEnvironment environment, boolean forTests) {
      super(environment);
      myForTests = forTests;
    }

    protected JavaParameters createJavaParameters() throws CantRunException {
      final JavaParameters params = new JavaParameters();
      params.getVMParametersList().addParametersString(vmParams);
      final Module module = getModule();
      assert module != null;
      myFramework.fillJavaParameters(module, params, false, myForTests, depsClasspath);

      final VirtualFile sdkRoot = myFramework.getSdkRoot(module);
      if (sdkRoot != null) {
        final Map<String, String> env = new HashMap<String, String>(System.getenv());
        env.put(myFramework.getSdkHomePropertyName(), FileUtil.toSystemDependentName(sdkRoot.getPath()));
        params.setEnv(env);
      }

      params.getProgramParametersList().add(cmdLine);
      return params;
    }

  }

}
