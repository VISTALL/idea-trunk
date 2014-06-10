package org.jetbrains.android.facet;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.prefs.AndroidLocation;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.intellij.CommonBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Processor;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import org.jetbrains.android.compiler.AndroidAptCompiler;
import org.jetbrains.android.compiler.AndroidCompileUtil;
import org.jetbrains.android.compiler.AndroidIdlCompiler;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.resourceManagers.LocalResourceManager;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.android.resourceManagers.SystemResourceManager;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidSdk;
import org.jetbrains.android.sdk.AndroidSdk15;
import org.jetbrains.android.sdk.MessageBuildingSdkLog;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.android.util.AndroidUtils;
import static org.jetbrains.android.util.AndroidUtils.EMULATOR;
import static org.jetbrains.android.util.AndroidUtils.SYSTEM_RESOURCE_PACKAGE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class AndroidFacet extends Facet<AndroidFacetConfiguration> {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.facet.AndroidFacet");
  public static final FacetTypeId<AndroidFacet> ID = new FacetTypeId<AndroidFacet>("android");
  private VirtualFileAdapter myListener;
  private final Object myLock = new Object();
  private volatile boolean myDisposed = false;

  private AvdManager myAvdManager = null;

  private SystemResourceManager mySystemResourceManager;
  private LocalResourceManager myLocalResourceManager;

  private final Map<String, Map<String, PsiClass>> myClassMaps = new HashMap<String, Map<String, PsiClass>>();

  public AndroidFacet(@NotNull Module module, String name, @NotNull AndroidFacetConfiguration configuration) {
    super(getFacetType(), module, name, configuration, null);
    configuration.setFacet(this);
  }

  public Object getLock() {
    return myLock;
  }

  public void androidPlatformChanged() {
    myAvdManager = null;
    myLocalResourceManager = null;
    mySystemResourceManager = null;
  }

  @Nullable
  public AndroidDebugBridge getDebugBridge() {
    AndroidPlatform platform = getConfiguration().getAndroidPlatform();
    if (platform != null) {
      return platform.getSdk().getDebugBridge(getModule().getProject());
    }
    return null;
  }

  public AvdManager.AvdInfo[] getAllAvds() {
    AvdManager manager = getAvdManagerSlowly();
    if (manager != null) {
      if (reloadAvds(manager)) {
        return manager.getAllAvds();
      }
    }
    return new AvdManager.AvdInfo[0];
  }

  private boolean reloadAvds(AvdManager manager) {
    try {
      MessageBuildingSdkLog log = new MessageBuildingSdkLog();
      manager.reloadAvds(log);
      if (log.getErrorMessage().length() > 0) {
        Messages
          .showErrorDialog(getModule().getProject(), AndroidBundle.message("cant.load.avds.error.prefix") + ' ' + log.getErrorMessage(),
                           CommonBundle.getErrorTitle());
      }
      return true;
    }
    catch (AndroidLocation.AndroidLocationException e) {
      Messages.showErrorDialog(getModule().getProject(), AndroidBundle.message("cant.load.avds.error"), CommonBundle.getErrorTitle());
    }
    return false;
  }

  public AvdManager.AvdInfo[] getAllCompatibleAvds() {
    List<AvdManager.AvdInfo> result = new ArrayList<AvdManager.AvdInfo>();
    addCompatibleAvds(result, getAllAvds());
    return result.toArray(new AvdManager.AvdInfo[result.size()]);
  }

  public AvdManager.AvdInfo[] getValidCompatibleAvds() {
    AvdManager manager = getAvdManagerSlowly();
    List<AvdManager.AvdInfo> result = new ArrayList<AvdManager.AvdInfo>();
    if (manager != null && reloadAvds(manager)) {
      addCompatibleAvds(result, manager.getValidAvds());
    }
    return result.toArray(new AvdManager.AvdInfo[result.size()]);
  }

  private AvdManager.AvdInfo[] addCompatibleAvds(List<AvdManager.AvdInfo> to, @NotNull AvdManager.AvdInfo[] from) {
    for (AvdManager.AvdInfo avd : from) {
      if (isCompatibleAvd(avd)) {
        to.add(avd);
      }
    }
    return to.toArray(new AvdManager.AvdInfo[to.size()]);
  }

  public boolean isCompatibleAvd(@NotNull AvdManager.AvdInfo avd) {
    IAndroidTarget target = getConfiguration().getAndroidTarget();
    return target != null && avd.getTarget() != null && target.isCompatibleBaseFor(avd.getTarget());
  }

  @Nullable
  public AvdManager getAvdManagerSlowly() {
    try {
      return getAvdManager();
    }
    catch (AvdsNotSupportedException e) {
    }
    catch (AndroidLocation.AndroidLocationException e) {
    }
    return null;
  }

  @NotNull
  public AvdManager getAvdManager() throws AvdsNotSupportedException, AndroidLocation.AndroidLocationException {
    if (myAvdManager == null) {
      AndroidPlatform platform = getConfiguration().getAndroidPlatform();
      AndroidSdk sdk = platform != null ? platform.getSdk() : null;
      Project project = getModule().getProject();
      if (sdk instanceof AndroidSdk15) {
        SdkManager sdkManager = ((AndroidSdk15)sdk).getSdkManager();
        myAvdManager = new AvdManager(sdkManager, AndroidUtils.getSdkLog(project));
      }
      else {
        throw new AvdsNotSupportedException();
      }
    }
    return myAvdManager;
  }

  @Nullable
  private static String executeCommand(GeneralCommandLine commandLine) throws ExecutionException {
    OSProcessHandler handler = new OSProcessHandler(commandLine.createProcess(), "");
    final StringBuilder messageBuilder = new StringBuilder();
    handler.addProcessListener(new ProcessAdapter() {
      public void onTextAvailable(final ProcessEvent event, final Key outputType) {
        messageBuilder.append(event.getText());
      }
    });
    handler.startNotify();
    handler.waitFor();
    int exitCode = handler.getProcess().exitValue();
    return exitCode != 0 ? messageBuilder.toString() : null;
  }

  public void launchEmulator(@Nullable final String avdName, @NotNull final String commands, @Nullable final ProcessHandler handler) {
    AndroidPlatform platform = getConfiguration().getAndroidPlatform();
    if (platform != null) {
      final String emulatorPath = platform.getSdk().getLocation() + File.separator + AndroidUtils.toolPath(EMULATOR);
      new Thread(new Runnable() {
        public void run() {
          GeneralCommandLine commandLine = new GeneralCommandLine();
          commandLine.setExePath(FileUtil.toSystemDependentName(emulatorPath));
          if (avdName != null) {
            commandLine.addParameter("-avd");
            commandLine.addParameter(avdName);
          }
          String[] params = commands.split("\\s+");
          for (String s : params) {
            if (s.length() > 0) {
              commandLine.addParameter(s);
            }
          }
          String[] commands = commandLine.getCommands();
          String command = StringUtil.join(commands, " ");
          LOG.info("Execute: " + command);
          if (handler != null && !handler.isProcessTerminated()) {
            handler.notifyTextAvailable(command + '\n', ProcessOutputTypes.STDOUT);
          }
          String result;
          try {
            result = executeCommand(commandLine);
          }
          catch (ExecutionException e) {
            result = e.getMessage();
          }
          if (result != null) {
            final String errorMessage = result;
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              public void run() {
                Messages.showErrorDialog(getModule().getProject(), errorMessage, AndroidBundle.message("emulator.error.dialog.title"));
              }
            });
          }
        }
      }).start();
    }
  }

  @Override
  public void initFacet() {
    myListener = new AndroidResourceFilesListener(this);
    Project project = getModule().getProject();
    LocalFileSystem.getInstance().addVirtualFileListener(myListener);
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        AndroidPlatform platform = getConfiguration().getAndroidPlatform();
        if (platform != null) {
          final ModifiableRootModel model = ModuleRootManager.getInstance(getModule()).getModifiableModel();
          if (model.findLibraryOrderEntry(platform.getLibrary()) == null) {
            model.addLibraryEntry(platform.getLibrary());
          }
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              model.commit();
            }
          });
        }
      }
    });
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        AndroidCompileUtil.generate(getModule(), new AndroidAptCompiler());
        AndroidCompileUtil.generate(getModule(), new AndroidIdlCompiler(getModule().getProject()));
      }
    });
  }

  @Override
  public void disposeFacet() {
    synchronized (myLock) {
      myDisposed = true;
      LocalFileSystem.getInstance().removeVirtualFileListener(myListener);
    }
  }

  public boolean isDisposed() {
    synchronized (myLock) {
      return myDisposed;
    }
  }

  @Nullable
  public static AndroidFacet getInstance(@NotNull Module module) {
    return FacetManager.getInstance(module).getFacetByType(ID);
  }

  @Nullable
  public static AndroidFacet getInstance(@NotNull ConvertContext context) {
    Module module = context.getModule();
    return module != null ? getInstance(module) : null;
  }

  @Nullable
  public static AndroidFacet getInstance(@NotNull PsiElement element) {
    Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module == null) return null;
    return getInstance(module);
  }

  @Nullable
  public static AndroidFacet getInstance(@NotNull DomElement element) {
    Module module = element.getModule();
    if (module == null) return null;
    return getInstance(module);
  }

  @Nullable
  public ResourceManager getResourceManager(@Nullable String resourcePackage) {
    return SYSTEM_RESOURCE_PACKAGE.equals(resourcePackage) ? getSystemResourceManager() : getLocalResourceManager();
  }

  @NotNull
  public LocalResourceManager getLocalResourceManager() {
    if (myLocalResourceManager == null) {
      myLocalResourceManager = new LocalResourceManager(getModule());
    }
    return myLocalResourceManager;
  }

  @Nullable
  public SystemResourceManager getSystemResourceManager() {
    if (mySystemResourceManager == null) {
      IAndroidTarget target = getConfiguration().getAndroidTarget();
      if (target != null) {
        mySystemResourceManager = new SystemResourceManager(getModule(), target);
      }
    }
    return mySystemResourceManager;
  }

  @Nullable
  public Manifest getManifest() {
    final VirtualFile manifestFile = AndroidRootUtil.getManifestFile(getModule());
    if (manifestFile == null) return null;
    return AndroidUtils.loadDomElement(getModule(), manifestFile, Manifest.class);
  }

  public static AndroidFacetType getFacetType() {
    return (AndroidFacetType)FacetTypeRegistry.getInstance().findFacetType(ID);
  }

  public PsiClass findClass(final String className) {
    return findClass(className, getModule().getModuleWithDependenciesAndLibrariesScope(true));
  }

  public PsiClass findClass(final String className, final GlobalSearchScope scope) {
    final Project project = getModule().getProject();
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    return ApplicationManager.getApplication().runReadAction(new Computable<PsiClass>() {
      @Nullable
      public PsiClass compute() {
        return facade.findClass(className, scope);
      }
    });
  }

  @NotNull
  public Map<String, PsiClass> getClassMap(@NotNull String className, @NotNull ClassMapConstructor constructor) {
    Map<String, PsiClass> classMap = getInitialClassMap(className, constructor);
    Project project = getModule().getProject();
    fillMap(className, constructor, ProjectScope.getProjectScope(project), classMap);
    return classMap;
  }

  @NotNull
  public synchronized Map<String, PsiClass> getInitialClassMap(@NotNull String className, @NotNull ClassMapConstructor constructor) {
    Map<String, PsiClass> viewClassMap = myClassMaps.get(className);
    if (viewClassMap != null) return viewClassMap;
    viewClassMap = new HashMap<String, PsiClass>();
    if (fillMap(className, constructor, getModule().getModuleWithDependenciesAndLibrariesScope(true), viewClassMap)) {
      myClassMaps.put(className, viewClassMap);
    }
    return viewClassMap;
  }

  private boolean fillMap(@NotNull String className,
                          @NotNull final ClassMapConstructor constructor,
                          GlobalSearchScope scope,
                          final Map<String, PsiClass> map) {
    PsiClass baseClass = findClass(className, getModule().getModuleWithDependenciesAndLibrariesScope(true));
    if (baseClass != null) {
      map.put(constructor.getTagNameByClass(baseClass), baseClass);
      ClassInheritorsSearch.search(baseClass, scope, true).forEach(new Processor<PsiClass>() {
        public boolean process(PsiClass c) {
          String s = constructor.getTagNameByClass(c);
          map.put(s, c);
          return true;
        }
      });
    }
    return map.size() > 0;
  }

  @Nullable
  public String getGenSourceRootPath() {
    VirtualFile manifest = AndroidRootUtil.getManifestFile(getModule());
    if (manifest != null) {
      VirtualFile manifestParent = manifest.getParent();
      if (manifestParent != null) {
        AndroidPlatform platform = getConfiguration().getAndroidPlatform();
        if (platform != null) {
          return manifestParent.getPath() + File.separator + SdkConstants.FD_GEN_SOURCES;
        }
      }
    }
    return null;
  }

}
