package org.jetbrains.android.util;

import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkConstants;
import com.intellij.CommonBundle;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.android.dom.AndroidDomUtil;
import org.jetbrains.android.dom.manifest.Activity;
import org.jetbrains.android.dom.manifest.Application;
import org.jetbrains.android.dom.manifest.IntentFilter;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.run.AndroidRunConfiguration;
import org.jetbrains.android.run.AndroidRunConfigurationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author yole, coyote
 */
public class AndroidUtils {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.util.AndroidUtils");

  public static final String CLASSES_FILE_NAME = "classes.dex";

  public static final Icon ANDROID_ICON = IconLoader.getIcon("/icons/android.png");
  public static final String NAMESPACE_KEY = "android";
  public static final String SYSTEM_RESOURCE_PACKAGE = "android";
  public static final String R_JAVA_FILENAME = "R.java";
  public static final String ANDROID_PACKAGE = "android";
  public static final String CONTEXT = ANDROID_PACKAGE + ".content.Context";
  public static final String VIEW_CLASS_NAME = ANDROID_PACKAGE + ".view.View";
  public static final String PREFERENCE_CLASS_NAME = ANDROID_PACKAGE + ".preference.Preference";
  public static final String ANIMATION_PACKAGE = "android.view.animation";
  public static final String INTERPOLATOR_CLASS_NAME = ANIMATION_PACKAGE + ".Interpolator";

  // tools
  public static final String APK_BUILDER = SystemInfo.isWindows ? "apkbuilder.bat" : "apkbuilder";
  public static final String EMULATOR = SystemInfo.isWindows ? "emulator.exe" : "emulator";
  public static final String ADB = SystemInfo.isWindows ? "adb.exe" : "adb";
  public static final String NAMESPACE_PREFIX = "http://schemas.android.com/apk/res/";
  public static final String ACTIVITY_BASE_CLASS_NAME = "android.app.Activity";
  public static final String R_CLASS_NAME = "R";
  public static final String LAUNCH_ACTION_NAME = "android.intent.action.MAIN";
  public static final String LAUNCH_CATEGORY_NAME = "android.intent.category.LAUNCHER";
  public static final String INSTRUMENTATION_RUNNER_BASE_CLASS = "android.app.Instrumentation";

  private AndroidUtils() {
  }

  public static ISdkLog getSdkLog(@NotNull final Object o) {
    if (!(o instanceof Component || o instanceof Project)) {
      throw new IllegalArgumentException();
    }
    return new ISdkLog() {
      public void warning(String warningFormat, Object... args) {
        if (warningFormat != null) {
          LOG.warn(String.format(warningFormat, args));
        }
      }

      public void error(Throwable t, String errorFormat, Object... args) {
        if (t != null) {
          LOG.info(t);
        }
        if (errorFormat != null) {
          String message = String.format(errorFormat, args);
          LOG.info(message);
          if (o instanceof Project) {
            Messages.showErrorDialog((Project)o, message, CommonBundle.getErrorTitle());
          }
          else {
            Messages.showErrorDialog((Component)o, message, CommonBundle.getErrorTitle());
          }
        }
      }

      public void printf(String msgFormat, Object... args) {
        if (msgFormat != null) {
          LOG.info(String.format(msgFormat, args));
        }
      }
    };
  }

  public static String toolPath(@NotNull String toolFileName) {
    return SdkConstants.OS_SDK_TOOLS_FOLDER + toolFileName;
  }

  @Nullable
  public static <T extends DomElement> T loadDomElement(@NotNull final Module module,
                                                        @NotNull final VirtualFile file,
                                                        @NotNull final Class<T> aClass) {
    return ApplicationManager.getApplication().runReadAction(new Computable<T>() {
      @Nullable
      public T compute() {
        Project project = module.getProject();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null || !(psiFile instanceof XmlFile)) {
          return null;
        }
        DomManager domManager = DomManager.getDomManager(project);
        DomFileElement<T> element = domManager.getFileElement((XmlFile)psiFile, aClass);
        if (element == null) return null;
        return element.getRootElement();
      }
    });
  }

  @Nullable
  public static VirtualFile findSourceRoot(@NotNull Module module, VirtualFile file) {
    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    Set<VirtualFile> sourceRoots = new HashSet<VirtualFile>();
    Collections.addAll(sourceRoots, rootManager.getSourceRoots());
    while (file != null) {
      if (sourceRoots.contains(file)) {
        return file;
      }
      file = file.getParent();
    }
    return null;
  }

  @Nullable
  public static String getPackageName(@NotNull Module module, VirtualFile file) {
    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    Set<VirtualFile> sourceRoots = new HashSet<VirtualFile>();
    Collections.addAll(sourceRoots, rootManager.getSourceRoots());
    VirtualFile projectDir = module.getProject().getBaseDir();
    List<String> packages = new ArrayList<String>();
    file = file.getParent();
    while (file != null && projectDir != file && !sourceRoots.contains(file)) {
      packages.add(file.getName());
      file = file.getParent();
    }
    if (file != null && sourceRoots.contains(file)) {
      StringBuilder packageName = new StringBuilder();
      for (int i = packages.size() - 1; i >= 0; i--) {
        packageName.append(packages.get(i));
        if (i > 0) packageName.append('.');
      }
      return packageName.toString();
    }
    return null;
  }

  public static boolean contains2Ids(String packageName) {
    return packageName.split("\\.").length >= 2;
  }

  public static boolean isRClassFile(@NotNull AndroidFacet facet, @NotNull PsiFile file) {
    if (file.getName().equals(R_JAVA_FILENAME) && file instanceof PsiJavaFile) {
      PsiJavaFile javaFile = (PsiJavaFile)file;
      Manifest manifest = facet.getManifest();
      if (manifest == null) return false;

      String manifestPackage = manifest.getPackage().getValue();
      if (javaFile.getPackageName().equals(manifestPackage)) return true;
    }
    return false;
  }

  @Nullable
  public static XmlAttributeValue getNameAttrValue(XmlTag tag) {
    XmlAttribute attribute = tag.getAttribute("name");
    return attribute != null ? attribute.getValueElement() : null;
  }

  @Nullable
  public static String getLocalXmlNamespace(AndroidFacet facet) {
    final Manifest manifest = facet.getManifest();
    if (manifest != null) {
      String aPackage = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        @Nullable
        public String compute() {
          return manifest.getPackage().getValue();
        }
      });
      if (aPackage != null && aPackage.length() != 0) {
        return NAMESPACE_PREFIX + aPackage;
      }
    }
    return null;
  }

  public static boolean isActivityLaunchable(@NotNull Module module, PsiClass c) {
    Activity activity = AndroidDomUtil.getActivityDomElementByClass(module, c);
    if (activity != null) {
      for (IntentFilter filter : activity.getIntentFilters()) {
        if (AndroidDomUtil.containsAction(filter, LAUNCH_ACTION_NAME)) {
          return true;
        }
      }
    }
    return false;
  }

  public static void addRunConfiguration(Project project, AndroidFacet facet, @Nullable String activityClass) {
    RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
    RunnerAndConfigurationSettingsImpl settings = (RunnerAndConfigurationSettingsImpl)runManager
      .createRunConfiguration("Unnamed", AndroidRunConfigurationType.getInstance().getFactory());
    AndroidRunConfiguration configuration = (AndroidRunConfiguration)settings.getConfiguration();
    configuration.setModule(facet.getModule());
    if (activityClass != null) {
      configuration.MODE = AndroidRunConfiguration.LAUNCH_SPECIFIC_ACTIVITY;
      configuration.ACTIVITY_CLASS = activityClass;
    }
    else {
      configuration.MODE = AndroidRunConfiguration.LAUNCH_DEFAULT_ACTIVITY;
    }
    runManager.addConfiguration(settings, false);
    runManager.setActiveConfiguration(settings);
  }

  @Nullable
  public static String getDefaultActivityName(@NotNull Manifest manifest) {
    Application application = manifest.getApplication();
    if (application != null) {
      for (Activity activity : application.getActivities()) {
        for (IntentFilter filter : activity.getIntentFilters()) {
          if (AndroidDomUtil.containsAction(filter, LAUNCH_ACTION_NAME) && AndroidDomUtil.containsCategory(filter, LAUNCH_CATEGORY_NAME)) {
            PsiClass c = activity.getActivityClass().getValue();
            return c != null ? c.getQualifiedName() : null;
          }
        }
      }
    }
    return null;
  }

  public static boolean isAbstract(PsiClass c) {
    return (c.isInterface() || c.hasModifierProperty(PsiModifier.ABSTRACT));
  }
}
