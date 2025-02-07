/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.JavaProgramRunner;
import com.intellij.execution.runners.RunnerInfo;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.cache.RCacheUtil;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualMethod;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualClassUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RMethodPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RPresentationConstants;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.scope.SearchScope;
import org.jetbrains.plugins.ruby.ruby.scope.SearchScopeUtil;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 18.07.2007
 */
public class RTestsRunConfiguration extends AbstractRubyRunConfiguration implements RTestRunConfigurationParams {
    public static final String DEFAULT_TESTS_SEARCH_MASK = "**/*." + RubyFileType.RUBY.getDefaultExtension();

    private static final RVirtualMethod[] EMPTY_VIRT_METHODS = new RVirtualMethod[0];

    private String myTestsFolderPath = TextUtil.EMPTY_STRING;
    private String myTestScriptPath = TextUtil.EMPTY_STRING;
    private String myTestClassName = TextUtil.EMPTY_STRING;
    private String myTestFileMask = TextUtil.EMPTY_STRING;
    private String myTestMethodName = TextUtil.EMPTY_STRING;
    private TestType myTestType = TestType.TEST_SCRIPT;
    private boolean myInheritanceCheckDisabled; //false

    public RTestsRunConfiguration(final Project project,
                                  final ConfigurationFactory factory,
                                  final String name) {
        super(project, factory, name);

        setRubyArgs(RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));
    }

    public static void copyParams(final RTestRunConfigurationParams fromParams,
                                  final RTestRunConfigurationParams toParams) {
        AbstractRubyRunConfiguration.copyParams(fromParams, toParams);

        toParams.setTestType(fromParams.getTestType());

        toParams.setTestsFolderPath(fromParams.getTestsFolderPath());
        toParams.setTestScriptPath(fromParams.getTestScriptPath());
        toParams.setTestQualifiedClassName(fromParams.getTestQualifiedClassName());
        toParams.setTestFileMask(fromParams.getTestFileMask());
        toParams.setTestMethodName(fromParams.getTestMethodName());

        toParams.setInheritanceCheckDisabled(fromParams.isInheritanceCheckDisabled());
    }

    protected RTestsRunConfiguration createInstance() {
        return new RTestsRunConfiguration(getProject(), getFactory(), getName());
    }

    @SuppressWarnings({"deprecation"})
    public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider provider) {
        return null;
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RTestsRunConfigurationEditor(getProject(), this);
    }

    @SuppressWarnings({"deprecation"})
    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final JavaProgramRunner runner) {
        return null;
    }

    public RunProfileState getState(final DataContext context, final RunnerInfo runnerInfo, final RunnerSettings runnerSettings, final ConfigurationPerRunnerSettings configurationSettings) throws ExecutionException {
        try {
            validateConfiguration(true);
        } catch (ExecutionException ee) {
            throw ee;
        } catch (Exception e) {
            throw new ExecutionException(e.getMessage(), e);
        }


        return new RTestsRunCommandLineState(this, runnerSettings, configurationSettings);
    }

    protected void validateConfiguration(boolean isExecution) throws Exception {
        RubyRunConfigurationUtil.inspectSDK(this, isExecution);
        RubyRunConfigurationUtil.inspectWorkingDirectory(this, isExecution);

        switch (getTestType()) {
            case ALL_IN_FOLDER:
                //testfolder
                inspectTestsFolder(isExecution);

                //mask
                if (TextUtil.isEmpty(getTestFileMask())) {
                    RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.no.mask"),
                                                                              isExecution);
                }
                break;
            case TEST_CLASS:
                //class and script
                inspectTestClassAndScript(isExecution, null);
                break;
            case TEST_METHOD:
                //class and script
                final Ref<FileSymbol> fSWrapper = new Ref<FileSymbol>();
                inspectTestClassAndScript(isExecution, fSWrapper);

                //method
                inspectMethod(isExecution, fSWrapper);

                break;
            case TEST_SCRIPT:
                //script
                inspectTestScript(isExecution);
                break;
        }
    }

    private void inspectTestScript(final boolean isExecution) throws Exception {
        final String scriptPath = getTestScriptPath().trim();
        final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
        if (TextUtil.isEmpty(scriptPath) || script == null || !script.exists()) {
            RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.script.not.exists"), isExecution);
        }

        //noinspection ConstantConditions
        if (script.isDirectory()) {
            RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.script.is.dir"), isExecution);
        }
    }

    private void inspectMethod(final boolean isExecution, final Ref<FileSymbol> fSWrapper)
            throws Exception {

        final String methodName = getTestMethodName().trim();
        if (!TextUtil.isEmpty(methodName)) {
            final String className = getTestQualifiedClassName().trim();
            final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(getTestScriptPath());
            assert script != null;
            final RVirtualClass rClass =
                    RCacheUtil.getClassByNameInScriptInRubyTestMode(className, getProject(),
                                                         SearchScopeUtil.getTestUnitClassSearchScope(getProject()),
                                                         script, fSWrapper);
            assert rClass != null;

            final Pair<Symbol,FileSymbol> pair = SymbolUtil.getSymbolByContainerRubyTestMode(rClass, fSWrapper);

            final RVirtualMethod[] methods;
            if (pair == null) {
                methods = EMPTY_VIRT_METHODS;
            } else {
                methods = RVirtualClassUtil.getAllMethodsWithName(pair.first, pair.second, methodName, Context.ALL);
            }

            for (RVirtualMethod method : methods) {
                final String name =
                        RMethodPresentationUtil.formatName(method, RPresentationConstants.SHOW_NAME);
                if (methodName.equals(name)) {
                    return;
                }
            }
            if (isInheritanceCheckDisabled()) {
                return; // we are not sure that all methods were found in test mode.
            }
        }
        RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.method.not.exists"), isExecution);
    }

    /**
     * Checks that class exists in script, and is inherited from Unit::Test::Case
     * @param isExecution Is currunt mode Execution mode
     * @param fSWrapper fSWrapper if null will be ignored. If wrapper contains
     * not null value, this value will be used for comparing qualified names, otherwise method
     * will store evaluated light mode symbol.
     * @throws Exception If mode isExecution then Execution exception, otherwisee RuntimeException
     */
    private void inspectTestClassAndScript(final boolean isExecution,
                                           final Ref<FileSymbol> fSWrapper) throws Exception {
        final String className = getTestQualifiedClassName().trim();
        if (!TextUtil.isEmpty(className)) {
            final String scriptPath = getTestScriptPath().trim();
            // Empty scriptPath mean that class wasn't chosen
            if (!TextUtil.isEmpty(scriptPath)) {
                //if some class is chosen then let's check script               
                inspectTestScript(isExecution);
                
                final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
                assert script != null; //has been already checked
                final Project project = getProject();
                final SearchScope classSearchScope = SearchScopeUtil.getTestUnitClassSearchScope(project);

                final RVirtualClass rClass =
                        RCacheUtil.getClassByNameInScriptInRubyTestMode(className, project, classSearchScope, script, fSWrapper);
                if (rClass != null) {
                    if (isInheritanceCheckDisabled() || RTestUnitUtil.isClassUnitTestCase(rClass, fSWrapper)) {
                        return;
                    } else {
                        RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.class.must.inherited.from.testcase"), isExecution);
                    }
                }
            }
        }
        RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.class.not.exists"), isExecution);
    }

    private void inspectTestsFolder(final boolean isExecution) throws Exception {
        final String folderPath = getTestsFolderPath().trim();
        File folder = new File(folderPath);
        if (!folder.exists()) {
            RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.folder.not.exists"), isExecution);
        }

        if (!folder.isDirectory()) {
            RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.folder.not.dir"), isExecution);
        }
    }

    public String getTestsFolderPath() {
        return myTestsFolderPath;
    }

    public String getTestScriptPath() {
        return myTestScriptPath;
    }

    public String getTestMethodName() {
        return myTestMethodName;
    }

    public TestType getTestType() {
        return myTestType;
    }

    public String getTestQualifiedClassName() {
        return myTestClassName;
    }

    public String getTestFileMask() {
        return myTestFileMask;
    }

    /**
     * Path to folder
     * @param path Path should contains only ruby style path separator: "/"
     */
    public void setTestsFolderPath(final String path) {
        myTestsFolderPath = TextUtil.getAsNotNull(path);
    }

    public void setTestScriptPath(final String pathOrMask) {
        myTestScriptPath = TextUtil.getAsNotNull(pathOrMask);
    }

    public void setTestMethodName(@Nullable final String name) {
        myTestMethodName = TextUtil.getAsNotNull(name);
    }

    public void setTestType(@NotNull final TestType testType) {
        myTestType = testType;
    }

    public void setTestQualifiedClassName(@Nullable final String testClassName) {
        myTestClassName = TextUtil.getAsNotNull(testClassName);
    }

    public void setTestFileMask(final String testFileMask) {
        myTestFileMask = TextUtil.getAsNotNull(testFileMask);
    }

   public void readExternal(Element element) throws InvalidDataException {
       RTestRunConfigurationExternalizer.getInstance().readExternal(this, element);
   }

    public void writeExternal(Element element) throws WriteExternalException {
        RTestRunConfigurationExternalizer.getInstance().writeExternal(this, element);
    }

    public boolean isInheritanceCheckDisabled() {
        return myInheritanceCheckDisabled;
    }

    public void setInheritanceCheckDisabled(final boolean disabled) {
        myInheritanceCheckDisabled = disabled;
    }
}
