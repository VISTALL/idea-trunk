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

package org.jetbrains.plugins.ruby.ruby.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.RComponents;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.Output;
import org.jetbrains.plugins.ruby.ruby.run.RubyScriptRunner;
import org.jetbrains.plugins.ruby.ruby.run.Runner;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 17.07.2006
 */

public class RubySdkType extends SdkType implements ApplicationComponent {

    protected static final Logger LOG = Logger.getInstance(RubySdkType.class.getName());

    //pathes use VFS path separator

    /** "/bin" */
    @NonNls public static final String BIN_DIR = VirtualFileUtil.VFS_PATH_SEPARATOR + "bin";
    /** "/gems" */
    @NonNls public static final String GEMS_SUBDIR = VirtualFileUtil.VFS_PATH_SEPARATOR + "gems";
    @NonNls public static final String RUBYSTUBS_DIR = "rubystubs";

    @NonNls private static final String GET_LOAD_PATH_SCRIPT =  "puts $LOAD_PATH";
    @NonNls private static final String GET_GEM_PATHES_SCRIPT = "require 'rubygems'; puts Gem.path";
    @NonNls private static final String GET_VERSION_SCRIPT =    "print VERSION";

    @NonNls private static final String VERSION_ARG = "--version";
    @NonNls private static final String JAR = ".jar";


    @NonNls private static final String RUBY_SDK_NAME =     "RUBY_SDK";
    @NonNls private static final String RUBY_WIN_EXE =      "ruby.exe";
    @NonNls private static final String RUBY_UNIX_EXE =     "ruby";


    @NonNls public static final String MAC_OS_BUNDLED_RUBY_PATH_PREFIX = "/System/Library/Frameworks/Ruby.framework/Versions";
    @NonNls public static final String MAC_OS_BUNDLED_RUBY_GEM_BIN_PATH = "/usr/bin";

    /**
     * @deprecated Don't use direct this constant(you can affect JRubySdkType), use getRubyExecutable()
     */
    @NonNls private static String RUBY_EXE;

    static {
        if (SystemInfo.isWindows){
            //noinspection deprecation
            RUBY_EXE = RUBY_WIN_EXE;
        } else
        if (SystemInfo.isUnix){
            //noinspection deprecation
            RUBY_EXE = RUBY_UNIX_EXE;
        } else {
            LOG.error(RBundle.message("os.not.supported"));
        }
    }

    public String getRubyExecutable() {
        //noinspection deprecation
        return RUBY_EXE;
    }

    protected RubySdkType() {
        super(RUBY_SDK_NAME);
    }

    protected RubySdkType(final String type) {
        super(type);
    }

    public String suggestHomePath() {
        return RubySdkUtil.suggestRubyHomePath();
    }

    public String getExePath() {
        return BIN_DIR + VirtualFileUtil.VFS_PATH_SEPARATOR + getRubyExecutable();
    }

    @NotNull
    public String getGemsBinDirectory(@NotNull final Sdk sdk) {
        return getSdkAdditionalData(sdk).getGemsBinDirectory();
    }

    public void setGemsBinDirectory(@NotNull final Sdk sdk,
                                    @NotNull final String path) {
        getSdkAdditionalData(sdk).setGemsBinDirectory(path);
    }

    public boolean isValidSdkHome(final String path) {
        return (new File(path + getExePath())).exists();
    }

    @Nullable
    public String getVersionString(final String sdkHome) {
        return getFullVersion(sdkHome);
    }

    public String suggestSdkName(final String currentSdkName, final String sdkHome) {
        final String version = getShortVersion(sdkHome);
        return getPresentableName()
               + (TextUtil.isEmpty(version) ? TextUtil.EMPTY_STRING : " " + version);
    }

    /**
     * Adds pathes from $LOAD_PATH into classpath
     * @param sdk current SDK
     */
    public void setupSdkPaths(final Sdk sdk) {
        final Runnable setupSdkPathsRunnable = new Runnable() {
            public void run() {
                final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
                final String rubyInterpreterExecutable = getVMExecutablePath(sdk);

                final Set<String> urls = new LinkedHashSet<String>();
                final String scriptSource = GET_LOAD_PATH_SCRIPT;
                final Output result = RubyScriptRunner.runScriptFromSource(rubyInterpreterExecutable, new String[]{}, scriptSource, new String[]{});
                final String loadPaths[] = TextUtil.splitByLines(result.getStdout());
                for (String s : loadPaths){
                    if (!s.trim().equals(".")){
                        urls.add(VirtualFileUtil.constructLocalUrl(s));
                    }
                }

// Adding GEM pathes to search for gems
                final Output gemsPathesResult = RubyScriptRunner.runScriptFromSource(rubyInterpreterExecutable, new String[]{}, GET_GEM_PATHES_SCRIPT, new String[]{});
                final String gemPaths[] = TextUtil.splitByLines(gemsPathesResult.getStdout());
                for (String s : gemPaths){
                    if (!s.trim().equals(".")){
                        urls.add(VirtualFileUtil.constructLocalUrl(s + GEMS_SUBDIR));
                    }
                }

         // trying to add rubystubs from plugin jar file
                final String jarPath = PathUtil.getJarPathForClass(RubySdkType.class);
                if (jarPath != null && jarPath.endsWith(JAR)) {
                    final VirtualFile jarFile = VirtualFileUtil.findFileByLocalPath(jarPath);

                    LOG.assertTrue(jarFile!=null, "jar file cannot be null");
                    //noinspection ConstantConditions
                    final VirtualFile rubyStubsDir = jarFile.getParent().getParent().findChild(RUBYSTUBS_DIR);
                    LOG.assertTrue(rubyStubsDir!=null, "main.rb file cannot be null");
                    urls.add(rubyStubsDir.getUrl());
                }

// WARNING: not all ruby LOAD_PATH may exist!
                final SdkModificator sdkModificator = sdk.getSdkModificator();
                for (String url : urls) {
                    final VirtualFile vFile = virtualFileManager.findFileByUrl(url);
                    if (vFile != null){
                        RubySdkUtil.addToSourceAndClasses(sdkModificator, vFile);
                    }
                }
                findAndSaveGemsRootsBy(sdkModificator);

                sdkModificator.commitChanges();
            }
        };

        final String title = RBundle.message("sdk.setup.progress.title");
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            public void run() {
                final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                indicator.setText(RBundle.message("sdk.setup.progress.setup.pathes"));
                setupSdkPathsRunnable.run();
            }
        }, title, false, null);
    }

    public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
      return new RubySdkConfigurable();
    }

    public void saveAdditionalData(SdkAdditionalData additionalData, Element additional) {
        if (additionalData instanceof RubySdkAdditionalData) {
            ((RubySdkAdditionalData)additionalData).save(additional);
        }
    }

    @NotNull
    public SdkAdditionalData loadAdditionalData(@NotNull final Sdk sdk, @Nullable Element additional) {
      return RubySdkAdditionalData.load(sdk, additional);
    }

    public SdkAdditionalData loadAdditionalData(Element additional) {
        //N/A
        return null;
    }

    @NotNull
    public String getBinPath(final Sdk sdk) {
        return sdk.getHomePath() + BIN_DIR;
    }

    @Nullable
    public String getToolsPath(final Sdk sdk) {
        //TODO Hot fix for [RUBY-216], remove it in Selena release!
        return getBinPath(sdk);
    }

    @Nullable
    public String getVMExecutablePath(final Sdk sdk) {

        return sdk.getHomePath() + getExePath();
    }

    @Nullable
    public String getRtLibraryPath(final Sdk sdk) {
        return null;
    }

    public String getPresentableName() {
        return RBundle.message("sdk.ruby.title");
    }

    @NotNull
    public String getComponentName() {
        return RComponents.RUBY_SDK_TYPE;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public Icon getIcon() {
      return RubyIcons.RUBY_SDK;
    }

    public Icon getIconForExpandedTreeNode() {
        return RubyIcons.RUBY_SDK_EXPANDED;
    }

    public Icon getIconForAddAction() {
        return RubyIcons.RUBY_SDK_ADD_ICON;
    }

    private RubySdkAdditionalData getSdkAdditionalData(@NotNull final Sdk sdk) {
        RubySdkAdditionalData rubySdkAdditionalData = (RubySdkAdditionalData) sdk.getSdkAdditionalData();
        if (rubySdkAdditionalData == null) {
             rubySdkAdditionalData = (RubySdkAdditionalData)loadAdditionalData(sdk, null);
            ((ProjectJdkImpl)sdk).setSdkAdditionalData(rubySdkAdditionalData);
        }
        return rubySdkAdditionalData;
    }

    @Nullable
    private String getShortVersion(final String sdkHome){
        final Output output = RubyScriptRunner.runScriptFromSource(sdkHome + getExePath(), new String[]{}, GET_VERSION_SCRIPT, new String[]{});
        return getSDKVersionByOutput(output, false);
    }

    @Nullable
    private String getFullVersion(final String sdkHome){
        final Output output = Runner.run(sdkHome + getExePath(), VERSION_ARG);
        return getSDKVersionByOutput(output, true);
    }

    private String getSDKVersionByOutput(@NotNull final Output output,
                                         final boolean showErrorMsg) {
        final String errorTitle = RBundle.message("sdk.error.cannot.create.sdk.title");
        if (output.getStdout().contains("JAVA_HOME")) {
            if (showErrorMsg) {
                Messages.showErrorDialog(output.getStdout(), errorTitle);
            }
            return null;
        } else if (!TextUtil.isEmpty(output.getStderr())) {
            if (showErrorMsg) {
                Messages.showErrorDialog(output.getStderr(), errorTitle);
            }
            return null;
        }
        return output.getStdout();
    }

    public static RubySdkType getInstance(){
        return ApplicationManager.getApplication().getComponent(RubySdkType.class);
    }

    public static void findAndSaveGemsRootsBy(final SdkModificator sdkModificator) {
        final SdkAdditionalData sdkAdditionalData = sdkModificator.getSdkAdditionalData();
        if (!(sdkAdditionalData instanceof RubySdkAdditionalData)) {
            return;
        }

        final List<String> gemsRoots = findGemsRoots(sdkModificator);
        ((RubySdkAdditionalData)sdkAdditionalData).setGemsRootUrls(gemsRoots);
    }

    @NotNull
    protected static List<String> findGemsRoots(@NotNull final SdkModificator sdkModificator) {
        final List<String> gemsRoots = new ArrayList<String>();
        final VirtualFile[] roots = sdkModificator.getRoots(ProjectRootType.SOURCE);
        for (VirtualFile root : roots) {
            final String url = root.getUrl();
            if (RubySdkUtil.isGemsRootUrl(url)) {
                gemsRoots.add(url);
            }
        }
        return gemsRoots;
    }

    @Nullable
    public static List<String> getGemsRootUrls(@NotNull final ProjectJdk sdk) {
        final SdkAdditionalData sdkAdditionalData = sdk.getSdkAdditionalData();
        if (sdkAdditionalData instanceof RubySdkAdditionalData) {
            return ((RubySdkAdditionalData) sdkAdditionalData).getGemsRootUrls();
        }
        return null;
    }   
}
