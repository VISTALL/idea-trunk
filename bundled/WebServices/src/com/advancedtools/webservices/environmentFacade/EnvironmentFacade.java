package com.advancedtools.webservices.environmentFacade;

import com.advancedtools.webservices.references.MyPathReferenceProvider;
import com.advancedtools.webservices.references.MyReferenceProvider;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.Expression;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @by maxim
 */
public abstract class EnvironmentFacade {
  static {
    Runtime.getRuntime().addShutdownHook(new Thread("WS plugin shutdown hook") {
      public void run() {
        synchronized(EnvironmentFacade.class) {
          for (File anOurFilesToRemoveOnExit : ourFilesToRemoveOnExit) {
            FileUtil.delete(anOurFilesToRemoveOnExit);
          }
        }
      }
    });
  }
  
  private static final Logger LOG = Logger.getInstance("webservicesplugin.envfacade");
  private static EnvironmentFacade instance;
  private static boolean instanceInitialized;
  private ThreadPoolExecutor pool;
  private boolean poolInitialized;

  private static boolean ourSelenaOrBetter;
  private static final List<File> ourFilesToRemoveOnExit = new ArrayList<File>();
  private static boolean ourDianaOrBetter;

  public abstract OpenFileDescriptor createOpenFileDescriptor(VirtualFile file, Project project);
  public abstract void openBrowserFor(String url);
  public abstract void showPopup(String title, JList list, final Runnable onSelectAction, Project project, DataContext dataContext);

  public abstract void setupLibsForDeployment(Module currentModule, ExternalEngine.LibraryDescriptor[] libInfos);
  public abstract void runProcessWithProgressSynchronously(Runnable action,String title,boolean cancellable,Project project);
  public abstract String getAntHomeDir();

  public abstract Expression getAnnotatedExpression(String baseClass, Expression[] parameters);
  public abstract MyPathReferenceProvider acquirePathReferenceProvider(Project project, boolean relativeFromWebRoot);
  public abstract MyPathReferenceProvider acquireDynamicPathReferenceProvider(Project project);
  public abstract void runProcessInTheBackground(Project project, String title, Runnable action);

  public abstract ConsoleView getConsole(Project project);

  public abstract boolean isWebModule(@NotNull Module module);
  public abstract boolean isEjbModule(@NotNull Module module);

  public abstract void addCompilerResourcePattern(Project project, String classResourceString) throws Exception;

  public abstract PsiDirectory getDirectoryFromFile(PsiFile containingFile);

  public abstract WebDirectoryElement findWebDirectoryByElement(String path, Module module);
  public abstract PsiPackage getPackageFor(@NotNull PsiDirectory directory);
  public abstract void checkCreateClass(@NotNull PsiDirectory directory, @NotNull String className) throws IncorrectOperationException;
  public abstract void checkIsIdentifier(@NotNull PsiManager manager, @NotNull String name) throws IncorrectOperationException;

  public static boolean isSelenaOrBetter() {
    getInstance();
    return ourSelenaOrBetter;
  }

  public VirtualFile ensureFileContentIsRefreshedForPath(final String path) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
      public VirtualFile compute() {
        return baseEnsureFileContentIsRefreshedForPath(path);
      }
    });
  }

  protected VirtualFile baseEnsureFileContentIsRefreshedForPath(String path) {
    final VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    assert fileByPath != null;
    fileByPath.refresh(false, fileByPath.isDirectory());
    return fileByPath;
  }

  public abstract VirtualFile findRelativeFile(String lastPathToJWSDP, VirtualFile base);

  public abstract void shortenClassReferences(PsiModifierList modifierList, Project project) throws IncorrectOperationException;

  public static boolean isDianaOrBetter() {
    getInstance();
    return ourDianaOrBetter;
  }

  public abstract PsiPackage findPackage(@NotNull String packagePrefix,@NotNull Project project);
  public abstract void setEffectiveLanguageLevel(@NotNull LanguageLevel jdk15,@NotNull Project project);
  public abstract PsiElementFactory getElementsFactory(@NotNull Project project) throws IncorrectOperationException;
  public abstract XmlTag createTagFromText(@NotNull String text, @NotNull Project project) throws IncorrectOperationException;

  public abstract PsiShortNamesCache getShortNamesCache(@NotNull Project project);
  public abstract LanguageLevel getEffectiveLanguageLevel(@NotNull Module module);

  public abstract void setLanguageLevel(@NotNull Project project,@NotNull LanguageLevel jdk15);

  public abstract void setModuleLanguageLevel(@NotNull Module module, @NotNull LanguageLevel jdk15);

  public abstract VirtualFile getCompilerOutputPath(Module moduleForFile);

  public abstract String getSdkHome(@NotNull Sdk projectJdk);

  public abstract String getVMExecutablePathForSdk(@NotNull Sdk projectJdk);
  public abstract String getToolsJarPathForSdk(@NotNull Sdk projectJdk);

  public abstract boolean isAcceptableSdk(@Nullable Sdk jdk);
  public abstract boolean prepareFileForWrite(PsiFile containingFile);

  public abstract boolean isJavaModuleType(@NotNull ModuleType moduleType);

  public abstract MyReferenceProvider acquireClassReferenceProvider(@NotNull Project project);

  public abstract void addLookupItem(Set<LookupItem> set, String name);

  public abstract void registerXmlAttributeValueReferenceProvider(Project project, String[] attributeNames,
                                                                  ElementFilter filter, MyReferenceProvider wsdlReferenceProvider);

  public abstract void registerReferenceProvider(Project project, ElementFilter filter, Class aClass, 
                                                 MyReferenceProvider javaProvider);

  public abstract CloseAction createRunnerAction(RunContentDescriptor myDescriptor, Project project);
  public abstract void showRunContent(RunContentManager contentManager, RunContentDescriptor myDescriptor);

  public abstract Sdk getInternalJdk();

  public interface ThreadExecutionControl {
    void waitFor();
  }

  public Future<?> executeOnPooledThread(Runnable task) {
    if (!poolInitialized) {
      poolInitialized = true;
      pool = new ThreadPoolExecutor(
        2,
        Integer.MAX_VALUE,
        60000,
        TimeUnit.MILLISECONDS,
        new SynchronousQueue<Runnable>(),
        new ThreadFactory() {
          public Thread newThread(Runnable r) {
            return new Thread(r, "WS plugin workers");
          }
        }
      );
    }

    return pool.submit(task);
  }

  public static EnvironmentFacade getInstance() {
    if (instance == null && !instanceInitialized) {
      String buildNumberString = ApplicationInfo.getInstance().getBuildNumber();
      int buildNumber = 8000;
      try { buildNumber = Integer.parseInt(buildNumberString); } catch(NumberFormatException ex) {}

      int maxDemetraBuild = 5000;
      int maxM2Build = 7200;
      int maxSelenaBuild = 7999;
      
      @NonNls String className = "com.advancedtools.webservices.environmentFacade.";
      if (buildNumber > maxSelenaBuild) {
        className += "lastversion";
      } else if (buildNumber > maxM2Build) {
        className += "selena";
      } else if (buildNumber > maxDemetraBuild) {
        className += "demetra";
      }

      className += ".Facade";

      ourSelenaOrBetter = buildNumber > maxM2Build;
      ourDianaOrBetter = buildNumber > maxSelenaBuild;

      try {
        instanceInitialized = true;
        instance = (EnvironmentFacade) Class.forName(className).newInstance();
      } catch (Throwable e) {
        LOG.error(e);
      }
    }

    return instance;
  }

  public abstract void registerXmlTagReferenceProvider(Project project, String[] tagCandidateNames,
                                                       ElementFilter tagFilter, boolean b, MyReferenceProvider xmlReferenceProvider);

  public abstract PsiClass findClass(@NotNull String clazz, @NotNull Project project, @Nullable GlobalSearchScope scope);

  public boolean processProperties(PsiClass clazz, PsiElementProcessor<PsiMember> processor) {
    for(PsiMethod m:clazz.getAllMethods()) {
      if (PropertyUtil.isSimplePropertyGetter(m)) {
        if (!processor.execute(m)) return false;
      }
    }

    return true;
  }

  public static String escapeXmlString(String sss) {
    return sss.replaceAll("&","&amp;").replaceAll("<","&gt;").replaceAll(">","&lt;");
  }

  public abstract @Nullable WSEngine getEngineFromModule(@NotNull Module module);

  public abstract PsiFile createFileFromText(@NotNull String fileName, @NotNull String text, @NotNull Project project);

  public abstract VirtualFile getProjectFileDirectory(@NotNull Project project);

  public abstract Sdk getProjectJdkFromModule(@NotNull Module module);

  public abstract PsiElement handleContentChange(@NotNull PsiElement psiElement, @NotNull TextRange range,
                                                 @NotNull String value) throws IncorrectOperationException;

  public static synchronized void deleteDirectoryOnExit(File file) {
    ourFilesToRemoveOnExit.add(file);
  }
}
