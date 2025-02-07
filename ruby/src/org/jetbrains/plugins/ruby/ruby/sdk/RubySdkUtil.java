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

import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.sdk.jruby.JRubySdkType;
import org.jetbrains.plugins.ruby.support.utils.OSUtil;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 17, 2007
 */
public class RubySdkUtil {
    @NonNls
    private static final String RUBY_WIN_DEFAULT_HOME_PATH = "C:\\ruby";
    @NonNls
    private static final String RUBY_UNIX_DEFAULT_HOME_PATH = "/usr";

    @NonNls
    private static final String JRUBY_WIN_DEFAULT_HOME_PATH = "C:\\jruby";
    @NonNls
    private static final String JRUBY_UNIX_DEFAULT_HOME_PATH = "/usr/local/jruby";
    @NonNls
    private static final String GEM_LIB_DIR = File.separator + "lib";


    /**
     * Checks if sdk is Ruby SDK (or JRuby SDK)
     *
     * @param sdk sdk to inspect
     * @return true, if sdk is Ruby type
     */
    public static boolean isKindOfRubySDK(@Nullable final ProjectJdk sdk) {
        return sdk != null && sdk.getSdkType() instanceof RubySdkType;
    }

    public static boolean isSDKValid(@Nullable final ProjectJdk sdk) {
        return isKindOfRubySDK(sdk) && isSDKHomeExist(sdk);
    }

    public static boolean isSDKHomeExist(@Nullable final ProjectJdk sdk) {
        if (sdk == null) {
            return false;
        }
        final VirtualFile sdkHomeDir = sdk.getHomeDirectory();
        return sdkHomeDir != null
                && sdk.getSdkType().isValidSdkHome(sdkHomeDir.getPath());
    }

    /**
     * @param sdk Ruby SDK
     * @return Gems bin folder path for given GEM.
     */
    @NotNull
    public static String getGemsBinFolderPath(@NotNull final Sdk sdk) {
        return RubySdkType.getInstance().getGemsBinDirectory(sdk);
    }

    private static String[] getSdkContentRootUrls(@NotNull final ProjectJdk sdk) {
        return sdk.getRootProvider().getUrls(OrderRootType.CLASSES);
    }

    /**
     * Returns all the roots with all gems
     *
     * @param sdk Sdk to get roots for
     * @return array of roots
     */
    public static String[] getSdkRootsWithAllGems(@NotNull final ProjectJdk sdk) {
        final ArrayList<String> urls = new ArrayList<String>();
        for (String rootUrl : sdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
            if (isGemsRootUrl(rootUrl)) {
                final VirtualFileManager manager = VirtualFileManager.getInstance();
                final VirtualFile gemsRoot = manager.findFileByUrl(rootUrl);
                if (gemsRoot != null) {
                    urls.addAll(getAllGemsLibUrls(gemsRoot));
                }
            } else {
                urls.add(rootUrl);
            }
        }
        return urls.toArray(new String[urls.size()]);
    }

    @Nullable
    public static String getRubyStubsDirUrl(@NotNull final ProjectJdk sdk) {
        for (String url : getSdkContentRootUrls(sdk)) {
            if (isRubystubsDirUrl(url)) {
                return url;
            }
        }
        return null;
    }

    /**
     * Determinates whether directory ends with "/gems" e.g. "gem" subdirectory of some gems lookup path
     *
     * @param url directory url
     * @return if directory is gems root directory
     */
    public static boolean isGemsRootUrl(@NotNull final String url) {
        return url.endsWith(RubySdkType.GEMS_SUBDIR);
    }

    public static boolean isRubystubsDirUrl(@NotNull final String url) {
        return url.endsWith(RubySdkType.RUBYSTUBS_DIR);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    /**
     * Creates mock sdk
     *
     * You can set forced sdk home path with system property "idea.ruby.testingFramework.mockSDK";
     */
    public static ProjectJdk getMockSdk(final String versionName) {
        return createMockSdk(RubySdkType.getInstance(), versionName);
    }

    public static ProjectJdk createMockSdk(final SdkType sdkType,
                                           final String versionName) {
        return createMockSdk(sdkType, versionName, true);
    }

    public static ProjectJdk createMockSdkWithoutStubs(final SdkType sdkType,
                                                       final String versionName) {
        return createMockSdk(sdkType, versionName, false);
    }

    public static ProjectJdk createMockSdk(final SdkType sdkType,
                                           final String versionName,
                                           final boolean addStubs) {
        final ProjectJdk sdk = new ProjectJdkImpl(versionName, sdkType);
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        final String sdkHome = System.getProperty("idea.ruby.mock.sdk");
        if (sdkHome != null){
            sdkModificator.setHomePath(sdkHome);
            //adding ruby stubs
            if (addStubs) {
                final VirtualFile stubsFile = LocalFileSystem.getInstance().findFileByPath(sdkHome + File.separator + RubySdkType.RUBYSTUBS_DIR);
                Assert.assertNotNull("Stubs file cannot be null", stubsFile);
                addToSourceAndClasses(sdkModificator, stubsFile);
            }
        }
        sdkModificator.setVersionString(versionName); // must be set after home path, otherwise setting home path clears the version string
        RubySdkType.findAndSaveGemsRootsBy(sdkModificator);
        sdkModificator.commitChanges();

        return sdk;
    }

    static void addToSourceAndClasses(@NotNull final SdkModificator sdkModificator, @Nullable final VirtualFile vFile) {
        if (vFile != null) {
            sdkModificator.addRoot(vFile, ProjectRootType.CLASS);
            sdkModificator.addRoot(vFile, ProjectRootType.SOURCE);
        }
    }

    @Nullable
    private static String findInterpreter(@NotNull final RubySdkType sdkType) {
        final String exePath = sdkType.getExePath();
        final String interpreterName = sdkType.getRubyExecutable();
        assert exePath.endsWith(interpreterName);
        final String pathByName = OSUtil.findExecutableByName(interpreterName);
        if (pathByName == null) {
            return null;
        }
        return pathByName.endsWith(exePath) ? pathByName.substring(0, pathByName.length() - exePath.length()) : null;
    }

    public static String suggestRubyHomePath() {
        // try to find in system path
        final String path = findInterpreter(RubySdkType.getInstance());
        if (path != null) {
            return path;
        }

        if (SystemInfo.isWindows) {
            return RUBY_WIN_DEFAULT_HOME_PATH;
        }
        if (SystemInfo.isUnix) {
            return RUBY_UNIX_DEFAULT_HOME_PATH;
        }
        throw new IllegalStateException(RBundle.message("os.not.supported"));
    }

    public static String suggestJRubyHomePath() {
        // try to find in system path
        final String path = findInterpreter(JRubySdkType.getInstance());
        if (path != null) {
            return path;
        }

        if (SystemInfo.isWindows) {
            return JRUBY_WIN_DEFAULT_HOME_PATH;
        }
        if (SystemInfo.isUnix) {
            return JRUBY_UNIX_DEFAULT_HOME_PATH;
        }
        throw new IllegalStateException(RBundle.message("os.not.supported"));
    }

    @NotNull
    public static List<String> getAllGemsLibUrls(@NotNull final VirtualFile gemRoot) {
        final List<String> gemsUrls = new ArrayList<String>();
        for (VirtualFile file : gemRoot.getChildren()) {
            gemsUrls.add(file.getUrl() + GEM_LIB_DIR);
        }
        return gemsUrls;
    }

    @NotNull
    public static List<GemInfo> getAllGems(@NotNull final VirtualFile gemRoot) {
        final List<GemInfo> gemsUrls = new ArrayList<GemInfo>();
        for (VirtualFile gemFile : gemRoot.getChildren()) {
            gemsUrls.add(GemInfo.create(gemFile));
        }
        return gemsUrls;
    }

    /**
     * Returns presentable version of url. If url is to one of gems,
     * then /gem_name/ is added to presentation
     *
     * @param sdk Current ruby sdk
     * @param url Url to get presentable text for
     * @return String - presentable text
     */
    @Nullable
    public static String getPresentableLocation(@Nullable final ProjectJdk sdk,
                                                @NotNull final String url) {
        if (sdk != null && RubySdkUtil.isKindOfRubySDK(sdk)) {
            final List<String> gemsRoots = RubySdkType.getGemsRootUrls(sdk);
            if (gemsRoots != null) {
                for (String gemsRoot : gemsRoots) {
                    if (url.startsWith(gemsRoot)) {
                        final String gemName = GemInfo.getGemNameByUrl(gemsRoot, url);
                        if (gemName != null) {
                            return ".../" + gemName + "/.../" + VirtualFileUtil.getFileName(url);
                        }
                    }
                }
            }
        }
        return null;
    }
}
