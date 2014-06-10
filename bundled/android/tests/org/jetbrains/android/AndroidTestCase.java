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

package org.jetbrains.android;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.JavaTestFixtureFactoryImpl;
import junit.framework.TestCase;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidFacetConfiguration;
import org.jetbrains.android.sdk.AndroidSdk;
import org.jetbrains.android.sdk.AndroidSdkTestProfile;
import org.jetbrains.android.sdk.EmptySdkLog;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public abstract class AndroidTestCase extends TestCase {
  private static final String ANDROID_LIBRARY_NAME = "Android SDK";
  
  protected CodeInsightTestFixture myFixture;
  private boolean myCreateManifest;
  protected AndroidFacet myFacet;

  public AndroidTestCase(boolean createManifest) {
    this.myCreateManifest = createManifest;
  }

  public AndroidTestCase() {
    this(true);
  }

  private static String getTestDataPath() {
    File f = new File(PathManager.getHomePath(), "svnPlugins/android/testData");
    return FileUtil.toSystemIndependentName(f.getAbsolutePath());
  }

  private String getTestSdkPath() {
    return getTestDataPath() + '/' + getTestProfile().getSdkDirName();
  }

  public abstract AndroidSdkTestProfile getTestProfile();

  public void setUp() throws Exception {
    IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
    factory.registerFixtureBuilder(JavaModuleFixtureBuilder.class, JavaTestFixtureFactoryImpl.MyJavaModuleFixtureBuilderImpl.class);
    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = factory.createFixtureBuilder();
    JavaModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(JavaModuleFixtureBuilder.class);
    myFixture = factory.createCodeInsightFixture(projectBuilder.getFixture());
    myFixture.setTestDataPath(getTestDataPath() + '/');
    configureModule(moduleBuilder);
    myFixture.setUp();
    myFixture.copyDirectoryToProject("res", "res");
    myFixture.copyDirectoryToProject("src", "src");
    myFixture.copyDirectoryToProject("gen", "gen");
    ModuleFixture moduleFixture = moduleBuilder.getFixture();
    addAndroidFacet(moduleFixture.getModule());
    if (myCreateManifest) {
      myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
    }
  }

  public void tearDown() throws Exception {
    myFixture.tearDown();       
    myFixture = null;
    super.tearDown();
  }

  private void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws IOException {
    moduleBuilder.addSourceContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addLibraryJars(ANDROID_LIBRARY_NAME, getTestSdkPath() + getTestProfile().getAndroidJarDirPath(), "android.jar");
  }

  @Nullable
  private static Library findAndroidLibrary(@NotNull Module module) {
    for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (entry instanceof LibraryOrderEntry) {
        Library library = ((LibraryOrderEntry)entry).getLibrary();
        if (library != null && library.getName().equals(ANDROID_LIBRARY_NAME)) {
          return library;
        }
      }
    }
    return null;
  }

  private void addAndroidFacet(Module module) {
    FacetManager facetManager = FacetManager.getInstance(module);
    myFacet = facetManager.createFacet(AndroidFacet.getFacetType(), "Android", null);
    AndroidFacetConfiguration configuration = myFacet.getConfiguration();
    AndroidSdk sdk = AndroidSdk.parse(getTestSdkPath(), new EmptySdkLog());
    IAndroidTarget target = sdk.findTargetByName(getTestProfile().getAndroidTargetName());
    Library androidLibrary = findAndroidLibrary(module);
    configuration.setAndroidPlatform(new AndroidPlatform(sdk, target, androidLibrary));
    final ModifiableFacetModel model = facetManager.createModifiableModel();
    model.addFacet(myFacet);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        model.commit();
      }
    });
  }
}