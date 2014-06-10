package com.advancedtools.webservices.environmentFacade;

import com.intellij.codeInsight.template.impl.TextExpression;
import com.advancedtools.webservices.utils.facet.*;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.AnnotatedMacro;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.deployment.*;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.extensions.LoadingOrder;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.jsp.WebDirectoryElement;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Maxim.Mossienko
 */
public class SelenaUtils {
  public interface ModuleContainerHandler {
    ContainerElement[] getElements();

    void addElement(LibraryLink link);

    void commit();
  }

  public static void addRequiredLibraryToDeploymentIfNeeded(final LibraryTable libraryTable,
                                                            final String name,
                                                            final String[] jars,
                                                            final Module module,
                                                            final ModuleContainerHandler moduleContainerHandler) {
    ContainerElement[] elements = moduleContainerHandler.getElements();
    for (ContainerElement element : elements) {
      if (element.getPresentableName().equals(name)) return;
    }
    final Library library = name != null ? libraryTable.getLibraryByName(name) : null;

    if (library != null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryTable.getTableLevel())) {
            addOneJar(null, library, module, moduleContainerHandler);
          } else {
            addOneJar(jars[0], library, module, moduleContainerHandler);
          }
        }
      });
    }
  }

  private static void addOneJar(String jarFileName, Library library, Module module, ModuleContainerHandler moduleContainerHandler) {
    LibraryLink libraryLink = DeploymentUtil.getInstance().createLibraryLink(library, module);
    libraryLink.setPackagingMethod(PackagingMethod.COPY_FILES);

    if (jarFileName != null) {
      jarFileName = jarFileName.substring(jarFileName.lastIndexOf(File.separator) + 1, jarFileName.length());
    }
    libraryLink.setURI("/WEB-INF/lib" + (jarFileName != null ? "/" + jarFileName : ""));
    moduleContainerHandler.addElement(libraryLink);
  }

  public static void showPopup(String title, JList list, Runnable onSelectAction, DataContext dataContext) {
    PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
    builder.setTitle(title).setItemChoosenCallback(onSelectAction).createPopup().showInBestPositionFor(dataContext);
  }

  public static Expression getAnnotatedExpression(String baseClass, Expression[] parameters) {
    final MacroCallNode macroCallNode = new MacroCallNode(new AnnotatedMacro());
    macroCallNode.addParameter(new TextExpression(baseClass));
    if (parameters != null) {
      for (Expression e : parameters) macroCallNode.addParameter(e);
    }
    return macroCallNode;
  }

  public static void doRunProcessInBackground(Project project, String title, Runnable action) {
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(project, title, action, null, null, new PerformInBackgroundOption() {
      public boolean shouldStartInBackground() {
        return true;
      }

      public void processSentToBackground() {
      }

    });
  }

  public abstract static class SelenaEnvironmentFacadeBase extends EnvironmentFacade {
    private static boolean hasJ2EE = false;

    static {
      try {
        Class.forName("com.intellij.javaee.web.facet.WebFacet");
        IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.javaee"));

        if (ideaPluginDescriptor != null && !PluginManager.shouldSkipPlugin(ideaPluginDescriptor) ) {
          hasJ2EE = true;
        }
      } catch (ClassNotFoundException ex) {}

      if (hasJ2EE) FacetTypeRegistry.getInstance().registerFacetType(new WebServicesFacetType());
      FacetTypeRegistry.getInstance().registerFacetType(new WebServicesClientFacetType());

      ExtensionsArea area = Extensions.getRootArea();
      final @NonNls String FRAMEWORK_SUPPORT_EP_NAME = "com.intellij.frameworkSupport";
      if (area != null && area.hasExtensionPoint(FRAMEWORK_SUPPORT_EP_NAME)) {
        if (hasJ2EE) area.getExtensionPoint(FRAMEWORK_SUPPORT_EP_NAME).registerExtension(new WebServicesSupportProvider(), LoadingOrder.ANY);
        area.getExtensionPoint(FRAMEWORK_SUPPORT_EP_NAME).registerExtension(new WebServicesClientSupportProvider(), LoadingOrder.ANY);
      }
    }

    public OpenFileDescriptor createOpenFileDescriptor(VirtualFile file, Project project) {
      return new OpenFileDescriptor(project, file);
    }

    public void openBrowserFor(String url) {
      BrowserUtil.launchBrowser(url);
    }

    public void showPopup(String title, JList list, Runnable onSelectAction, Project project, DataContext dataContext) {
      SelenaUtils.showPopup(title, list, onSelectAction, dataContext);
    }

    public void setupLibsForDeployment(Module currentModule, ExternalEngine.LibraryDescriptor[] libInfos) {
      if (!hasJ2EE) return;
      WebFacet facet = FacetManager.getInstance(currentModule).getFacetByType(WebFacet.ID);

      if (facet == null) {
        return;
      }
      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(currentModule).getModifiableModel();
      final PackagingConfiguration modifiableModel = facet.getPackagingConfiguration();
      LibraryTable moduleLibraryTable = rootModel.getModuleLibraryTable();
      LibraryTable projectLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(currentModule.getProject());

      SelenaUtils.ModuleContainerHandler handler = new SelenaUtils.ModuleContainerHandler() {
        final List<ContainerElement> links = new ArrayList<ContainerElement>();

        {
          links.addAll(Arrays.asList(modifiableModel.getElements()));
        }

        public ContainerElement[] getElements() {
          return links.toArray(new ContainerElement[links.size()]);
        }

        public void addElement(LibraryLink link) {
          links.add(link);
        }

        public void commit() {
          modifiableModel.setElements(links.toArray(new ContainerElement[links.size()]));
        }
      };

      for (int i = 0; i < libInfos.length; i++) {
        if (!libInfos[i].isToIncludeInJavaEEContainerDeployment()) continue;
        final String libraryName = libInfos[i].getName();

        SelenaUtils.addRequiredLibraryToDeploymentIfNeeded(
          libraryName != null ? projectLibraryTable : moduleLibraryTable,
          libraryName,
          libInfos[i].getLibJars(),
          currentModule,
          handler
        );
      }

      handler.commit();
      ApplicationManager.getApplication().runWriteAction(new Runnable(){
        public void run() {rootModel.commit();}
      });
    }

    public void runProcessWithProgressSynchronously(Runnable action, String title, boolean cancellable, Project project) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(action, title, cancellable, project);
    }

    public String getAntHomeDir() {
      return PathManager.getHomePath() + "/lib/ant";
    }

    public Expression getAnnotatedExpression(String baseClass, Expression[] parameters) {
      return SelenaUtils.getAnnotatedExpression(baseClass, parameters);
    }

    public void runProcessInTheBackground(Project project, String title, Runnable action) {
      SelenaUtils.doRunProcessInBackground(project, title, action);
    }

    public ConsoleView getConsole(Project project) {
      return TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    }

    public boolean isWebModule(Module module) {
      return hasJ2EE && FacetManager.getInstance(module).getFacetByType(WebFacet.ID) != null;
    }

    public boolean isEjbModule(Module module) {
      return hasJ2EE && FacetManager.getInstance(module).getFacetByType(EjbFacet.ID) != null;
    }

    public void addCompilerResourcePattern(Project project, String classResourceString) throws Exception {
      ((CompilerConfigurationImpl) CompilerConfiguration.getInstance(project)).addResourceFilePattern(classResourceString);
    }

    public PsiDirectory getDirectoryFromFile(PsiFile containingFile) {
      return containingFile.getParent();
    }

    public WebDirectoryElement findWebDirectoryByElement(String path, Module module) {
      if (!hasJ2EE) return null;
      final WebFacet webFacet = FacetManager.getInstance(module).getFacetByType(WebFacet.ID);
      if (webFacet == null) return null;
      return WebUtil.getWebUtil().findWebDirectoryElement(path, webFacet);
    }

    public WSEngine getEngineFromModule(Module module) {
      final FacetManager facetManager = FacetManager.getInstance(module);
      if (hasJ2EE) {
        final WebServicesFacet webServicesFacet = facetManager.getFacetByType(WebServicesFacet.ID);
        if (webServicesFacet != null) return webServicesFacet.getConfiguration().getWsEngine();
      }
      final WebServicesClientFacet webServicesClientFacet = facetManager.getFacetByType(WebServicesClientFacet.ID);
      if (webServicesClientFacet != null) return webServicesClientFacet.getConfiguration().getWsEngine();
      return null;
    }

    public VirtualFile getProjectFileDirectory(Project project) {
      return project.getBaseDir();
    }

    public Future<?> executeOnPooledThread(Runnable task) {
      return ApplicationManager.getApplication().executeOnPooledThread(task);
    }

    public VirtualFile ensureFileContentIsRefreshedForPath(String path) {
      return baseEnsureFileContentIsRefreshedForPath(path);
    }
  }
}
