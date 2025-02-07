package org.jetbrains.android.facet;

import com.android.sdklib.SdkConstants;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.newProject.AndroidModuleType;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author yole
 */
public class AndroidFacetType extends FacetType<AndroidFacet, AndroidFacetConfiguration> {
  public AndroidFacetType() {
    super(AndroidFacet.ID, "android", "Android");
  }


  public AndroidFacetConfiguration createDefaultConfiguration() {
    AndroidFacetConfiguration configuration = new AndroidFacetConfiguration();
    PropertiesComponent component = PropertiesComponent.getInstance();
    LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
    if (component.isValueSet(AndroidSdkUtils.DEFAULT_PLATFORM_NAME_PROPERTY)) {
      Library defaultLib = libraryTable.getLibraryByName(component.getValue(AndroidSdkUtils.DEFAULT_PLATFORM_NAME_PROPERTY));
      if (defaultLib != null && tryToSetAndroidPlatform(configuration, defaultLib)) {
        return configuration;
      }
      for (Library library : libraryTable.getLibraries()) {
        if (tryToSetAndroidPlatform(configuration, library)) {
          component.setValue(AndroidSdkUtils.DEFAULT_PLATFORM_NAME_PROPERTY, library.getName());
          return configuration;
        }
      }
    }
    return configuration;
  }

  private static boolean tryToSetAndroidPlatform(AndroidFacetConfiguration configuration, Library library) {
    AndroidPlatform platform = AndroidPlatform.parse(library, null, null);
    if (platform != null) {
      configuration.setAndroidPlatform(platform);
      return true;
    }
    return false;
  }

  public AndroidFacet createFacet(@NotNull Module module,
                                  String name,
                                  @NotNull AndroidFacetConfiguration configuration,
                                  @Nullable Facet underlyingFacet) {
    return new AndroidFacet(module, name, configuration);
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType || moduleType instanceof AndroidModuleType;
  }

  public void registerDetectors(FacetDetectorRegistry<AndroidFacetConfiguration> detectorRegistry) {
    FacetDetector<VirtualFile, AndroidFacetConfiguration> detector = new FacetDetector<VirtualFile, AndroidFacetConfiguration>() {
      public AndroidFacetConfiguration detectFacet(VirtualFile source, Collection<AndroidFacetConfiguration> existentFacetConfigurations) {
        if (!existentFacetConfigurations.isEmpty()) {
          return existentFacetConfigurations.iterator().next();
        }
        return createDefaultConfiguration();
      }

      @Override
      public void afterFacetAdded(@NotNull final Facet facet) {
        if (facet instanceof AndroidFacet) {
          final Project project = facet.getModule().getProject();
          StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            public void run() {
              AndroidFacet androidFacet = (AndroidFacet)facet;
              Manifest manifest = androidFacet.getManifest();
              if (manifest != null) {
                if (AndroidUtils.getDefaultActivityName(manifest) != null) {
                  AndroidUtils.addRunConfiguration(project, androidFacet, null);
                }
              }
              ApplicationManager.getApplication().saveAll();
            }
          });
        }
      }
    };
    VirtualFileFilter androidManifestFilter = new VirtualFileFilter() {
      public boolean accept(VirtualFile file) {
        return file.getName().equals(SdkConstants.FN_ANDROID_MANIFEST_XML);
      }
    };
    detectorRegistry.registerUniversalDetector(StdFileTypes.XML, androidManifestFilter, detector);
  }

  public Icon getIcon() {
    return AndroidUtils.ANDROID_ICON;
  }
}
