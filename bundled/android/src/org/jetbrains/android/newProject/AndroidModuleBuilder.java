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

package org.jetbrains.android.newProject;

import com.android.sdklib.SdkConstants;
import static com.android.sdklib.SdkConstants.FN_ANDROID_MANIFEST_XML;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.ide.util.PackageUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ExternalChangeAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.android.actions.CreateActivityAction;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.dom.resources.ResourceValue;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidFacetConfiguration;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.android.AndroidFileTemplateProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 26, 2009
 * Time: 7:32:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidModuleBuilder extends JavaModuleBuilder {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.android.newProject.AndroidModuleBuilder");

  private AndroidPlatform myPlatform;
  private String myPackageName;
  private String myApplicationName;
  private String myActivityName;
  private static final String LIBS_FOLDER = "libs";

  public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
    super.setupRootModel(rootModel);
    PropertiesComponent.getInstance().setValue(AndroidSdkUtils.DEFAULT_PLATFORM_NAME_PROPERTY, myPlatform.getName());
    VirtualFile[] files = rootModel.getContentRoots();
    if (files.length > 0) {
      final VirtualFile contentRoot = files[0];
      final AndroidFacet facet = addAndroidFacetAndLibrary(rootModel);
      final Project project = rootModel.getProject();
      StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              try {
                AndroidFileTemplateProvider
                  .createFromTemplate(project, contentRoot, AndroidFileTemplateProvider.ANDROID_MANIFEST_TEMPLATE, FN_ANDROID_MANIFEST_XML);
              }
              catch (Exception e) {
                LOG.error(e);
              }
              createResourcesAndLibs(project, contentRoot);
              createActivityAndSetupManifest(facet);
              addRunConfiguration(project, facet);
            }
          });
        }
      });
    }
  }

  private void addRunConfiguration(Project project, AndroidFacet facet) {
    if (isHelloAndroid()) {
      String activityClass = myPackageName + '.' + myActivityName;
      AndroidUtils.addRunConfiguration(project, facet, activityClass);
    }
  }

  private boolean isHelloAndroid() {
    return myActivityName.length() > 0;
  }

  private void createActivityAndSetupManifest(final AndroidFacet facet) {
    if (myPackageName != null) {
      CommandProcessor.getInstance().executeCommand(facet.getModule().getProject(), new ExternalChangeAction() {
        public void run() {
          Runnable action = new Runnable() {
            public void run() {
              PsiDirectory packageDir = PackageUtil.findOrCreateDirectoryForPackage(facet.getModule(), myPackageName, null, false);
              final Manifest manifest = facet.getManifest();
              if (manifest != null) {
                manifest.getPackage().setValue(myPackageName);
                if (isHelloAndroid()) {
                  createActivity(packageDir);
                }
                if (myApplicationName.length() > 0) {
                  manifest.getApplication().getLabel().setValue(ResourceValue.literal(myApplicationName));
                }

              }
            }
          };
          ApplicationManager.getApplication().runWriteAction(action);
        }
      }, AndroidBundle.message("build.android.module.process.title"), null);
    }
  }

  private void createActivity(PsiDirectory packageDir) {
    CreateActivityAction hackAction = new CreateActivityAction(true) {
      @Override
      protected boolean asStartupActivity() {
        return true;
      }
    };
    final PsiClass c = hackAction.createActivity(myActivityName, "", packageDir);
    StartupManager.getInstance(c.getProject()).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        c.getProject().save();
      }
    });
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        c.getContainingFile().navigate(true);
      }
    });
  }

  @NotNull
  private AndroidFacet addAndroidFacetAndLibrary(ModifiableRootModel rootModel) {
    Module module = rootModel.getModule();
    final FacetManager facetManager = FacetManager.getInstance(module);
    ModifiableFacetModel model = facetManager.createModifiableModel();
    AndroidFacet facet = facetManager.createFacet(AndroidFacet.getFacetType(), "Android", null);
    AndroidFacetConfiguration configuration = facet.getConfiguration();
    configuration.setAndroidPlatform(myPlatform);
    model.addFacet(facet);
    rootModel.addLibraryEntry(myPlatform.getLibrary());
    model.commit();
    return facet;
  }

  private void createResourcesAndLibs(final Project project, final VirtualFile rootDir) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          rootDir.createChildDirectory(project, SdkConstants.FD_ASSETS);
          rootDir.createChildDirectory(project, LIBS_FOLDER);
          VirtualFile resDir = rootDir.createChildDirectory(project, SdkConstants.FD_RES);
          VirtualFile drawableDir = resDir.createChildDirectory(project, SdkConstants.FD_DRAWABLE);
          createFileFromResource(project, drawableDir, "icon.png", "/icons/androidLarge.png");
          if (isHelloAndroid()) {
            VirtualFile valuesDir = resDir.createChildDirectory(project, SdkConstants.FD_VALUES);
            createFileFromResource(project, valuesDir, "strings.xml", "res/values/strings.xml");
            VirtualFile layoutDir = resDir.createChildDirectory(project, SdkConstants.FD_LAYOUT);
            createFileFromResource(project, layoutDir, "main.xml", "res/layout/main.xml");
          }
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    });
  }

  private static void createFileFromResource(Project project, VirtualFile drawableDir, String name, String resourceFilePath) throws IOException {
    VirtualFile resFile = drawableDir.createChildData(project, name);
    InputStream stream = AndroidModuleBuilder.class.getResourceAsStream(resourceFilePath);
    try {
      byte[] bytes = FileUtil.adaptiveLoadBytes(stream);
      resFile.setBinaryContent(bytes);
    }
    finally {
      stream.close();
    }
  }

  public void setActivityName(String activityName) {
    myActivityName = activityName;
  }

  public void setApplicationName(String applicationName) {
    myApplicationName = applicationName;
  }

  public void setPackageName(String packageName) {
    myPackageName = packageName;
  }

  public void setPlatform(@NotNull AndroidPlatform platform) {
    myPlatform = platform;
  }

  public ModuleType getModuleType() {
    return AndroidModuleType.getInstance();
  }
}
