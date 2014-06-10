package com.advancedtools.webservices.environmentFacade.demetra;

import com.intellij.ide.BrowserUtil;
import com.intellij.javaee.JavaeeModuleProperties;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.WebModuleProperties;
import com.intellij.javaee.make.MakeUtil;
import com.intellij.javaee.module.ContainerElement;
import com.intellij.javaee.module.J2EEPackagingMethod;
import com.intellij.javaee.module.LibraryLink;
import com.intellij.javaee.module.ModuleContainer;
import com.intellij.javaee.module.components.WebModulePropertiesImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizingReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizableReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.util.Query;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.CloseAction;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.JavaProgramRunner;
import com.intellij.execution.ExecutionRegistry;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.j2ee.openapi.ex.ExternalResourceManagerEx;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.utils.MyTextExpression;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.references.MyReferenceProvider;
import com.advancedtools.webservices.references.MyPathReferenceProvider;
import com.advancedtools.webservices.WebServicesPluginSettings;

import javax.swing.*;
import java.io.File;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @by maxim
 */
public class Facade extends EnvironmentFacade {
  public OpenFileDescriptor createOpenFileDescriptor(VirtualFile file, Project project) {
    return new OpenFileDescriptor(project, file);
  }

  public void openBrowserFor(String url) {
    BrowserUtil.launchBrowser(url);
  }

  public void showPopup(String title, JList list, final Runnable onSelectAction, Project project, DataContext dataContext) {
    PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
    builder.setTitle(title).setItemChoosenCallback(onSelectAction).createPopup().showInBestPositionFor(dataContext);
  }

  public void setupLibsForDeployment(Module currentModule, ExternalEngine.LibraryDescriptor[] libInfos) {
    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(currentModule).getModifiableModel();
    final JavaeeModuleProperties instance = WebModulePropertiesImpl.getInstance(currentModule);

    if (instance == null) {
      return;
    }

    instance.startEdit(rootModel);

    final ModuleContainer modifiableModel = instance.getModifiableModel();
    LibraryTable moduleLibraryTable = rootModel.getModuleLibraryTable();
    LibraryTable projectLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(currentModule.getProject());

    for (int i = 0; i < libInfos.length; i++) {
      if (!libInfos[i].isToIncludeInJavaEEContainerDeployment()) continue;
      final String libraryName = libInfos[i].getName();

      addRequiredLibraryToDeploymentIfNeeded(
        libraryName != null ? projectLibraryTable : moduleLibraryTable,
        libraryName,
        libInfos[i].getLibJars(),
        currentModule,
        modifiableModel
      );
    }

    try {
      instance.commit(rootModel);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }

    modifiableModel.disposeModifiableModel();
  }

  public void runProcessWithProgressSynchronously(Runnable action, String title, boolean cancellable, Project project) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(action, title, cancellable, project);
  }

  public String getAntHomeDir() {
    return PathManager.getHomePath() + "/lib/ant";
  }

  public Expression getAnnotatedExpression(final String baseClass, Expression[] parameters) {
    final MacroCallNode macroCallNode = new MacroCallNode(new AnnotatedMacro());
    macroCallNode.addParameter(new MyTextExpression(baseClass));
    if (parameters != null) {
      for (Expression e : parameters) macroCallNode.addParameter(e);
    }
    return macroCallNode;
  }

  public void runProcessInTheBackground(Project project, String title, Runnable action) {
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(project, title, action, null, null, new PerformInBackgroundOption() {
      public boolean shouldStartInBackground() {
        return true;
      }

      public void processSentToBackground() {
      }

      public void processRestoredToForeground() {
      }
    });
  }

  public ConsoleView getConsole(Project project) {
    return TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
  }

  public boolean isWebModule(Module module) {
    return module.getModuleType() == ModuleType.WEB;
  }

  public boolean isEjbModule(Module module) {
    return module.getModuleType() == ModuleType.EJB;
  }

  public void addCompilerResourcePattern(Project project, String classResourceString) throws Exception {
    CompilerConfiguration.getInstance(project).addResourceFilePattern(classResourceString);
  }

  public PsiDirectory getDirectoryFromFile(PsiFile containingFile) {
    return (PsiDirectory) containingFile.getParent();
  }

  public WebDirectoryElement findWebDirectoryByElement(String path, Module module) {
    return JspManager.getInstance(module.getProject()).findWebDirectoryElementByPath(
      path,
      (WebModuleProperties) WebModulePropertiesImpl.getInstance(module)
    );
  }

  public void registerXmlTagReferenceProvider(ReferenceProvidersRegistry registry, String[] tagCandidateNames, ElementFilter tagFilter, boolean b, PsiReferenceProvider xmlReferenceProvider) {
    registry.registerXmlTagReferenceProvider(
      tagCandidateNames,
      tagFilter,
      true,
      xmlReferenceProvider
    );
  }

  public MyReferenceProvider[] getProvidersByElement(Project project, PsiElement element, Class clazz) {
    PsiReferenceProvider[] byElement = ReferenceProvidersRegistry.getInstance(project).getProvidersByElement(element, clazz);
    List<MyReferenceProvider> list = new ArrayList<MyReferenceProvider>(byElement.length);
    for (PsiReferenceProvider r : byElement) {
      if (r instanceof MyReferenceProviderWrapper) {
        list.add(((MyReferenceProviderWrapper) r).referenceProvider);
      }
    }
    return list.toArray(new MyReferenceProvider[list.size()]);
  }

  public
  @Nullable
  WSEngine getEngineFromModule(@NotNull Module module) {
    return null;
  }

  public VirtualFile getProjectFileDirectory(Project project) {
    return project.getProjectFile().getParent();
  }

  static void addRequiredLibraryToDeploymentIfNeeded(final LibraryTable libraryTable,
                                                     final String name,
                                                     final String[] jars,
                                                     final Module module,
                                                     final ModuleContainer modifiableModel) {
    ContainerElement[] elements = modifiableModel.getElements();
    for (ContainerElement element : elements) {
      if (element.getPresentableName().equals(name)) return;
    }
    final Library library = name != null ? libraryTable.getLibraryByName(name) : null;

    if (library != null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryTable.getTableLevel())) {
            addOneJar(null, library, module, modifiableModel);
          } else {
            addOneJar(jars[0], library, module, modifiableModel);
          }
        }
      });
    }
  }

  private static void addOneJar(String jarFileName, Library library, Module module, ModuleContainer modifiableModel) {
    LibraryLink libraryLink = MakeUtil.getInstance().createLibraryLink(library, module);
    libraryLink.setPackagingMethod(J2EEPackagingMethod.COPY_FILES);

    if (jarFileName != null) {
      jarFileName = jarFileName.substring(jarFileName.lastIndexOf(File.separator) + 1, jarFileName.length());
    }
    libraryLink.setURI("/WEB-INF/lib" + (jarFileName != null ? "/" + jarFileName : ""));
    modifiableModel.addElement(libraryLink);
  }

  static class AnnotatedMacro implements Macro {

    @NonNls
    public String getName() {
      return "annotated";
    }

    public String getDescription() {
      return "Returns members annotated with specified annotation";
    }

    @NonNls
    public String getDefaultValue() {
      return "";
    }

    private Query<PsiMember> findAnnotated(ExpressionContext context, Expression[] params) {
      if (params == null || params.length == 0) return null;
      PsiManager instance = PsiManager.getInstance(context.getProject());

      final String paramResult = params[0].calculateResult(context).toString();
      if (paramResult == null) return null;
      final GlobalSearchScope scope = GlobalSearchScope.allScope(context.getProject());
      final PsiClass myBaseClass = instance.findClass(
        paramResult,
        scope
      );

      if (myBaseClass != null) {
        return AnnotatedMembersSearch.search(myBaseClass, scope);
      }
      return null;
    }

    public Result calculateResult(Expression[] expressions, ExpressionContext expressionContext) {
      final Query<PsiMember> psiMembers = findAnnotated(expressionContext, expressions);

      if (psiMembers != null) {
        final PsiMember member = psiMembers.findFirst();

        if (member != null) {
          return new TextResult(member instanceof PsiClass ? ((PsiClass) member).getQualifiedName() : member.getName());
        }
      }
      return null;
    }

    public Result calculateQuickResult(Expression[] expressions, ExpressionContext expressionContext) {
      return calculateResult(expressions, expressionContext);
    }

    public LookupItem[] calculateLookupItems(Expression[] params, ExpressionContext context) {
      final Query<PsiMember> query = findAnnotated(context, params);

      if (query != null) {
        Set<LookupItem> set = new LinkedHashSet<LookupItem>();
        final String secondParamValue = params.length > 1 ? params[1].calculateResult(context).toString() : null;
        final boolean isShortName = secondParamValue != null && !Boolean.valueOf(secondParamValue);
        final PsiClass findInClass = secondParamValue != null ? PsiManager.getInstance(context.getProject()).findClass(secondParamValue) : null;

        for (PsiMember object : query.findAll()) {
          if (findInClass != null && !object.getContainingClass().equals(findInClass)) continue;
          boolean isClazz = object instanceof PsiClass;
          final String name = isShortName || !isClazz ? object.getName() : ((PsiClass) object).getQualifiedName();
          if (name != null && name.length() > 0) LookupItemUtil.addLookupItem(set, name, "");
        }

        return set.toArray(new LookupItem[set.size()]);
      }
      return new LookupItem[0];
    }
  }

  public VirtualFile findRelativeFile(String lastPathToJWSDP, VirtualFile base) {
    return VfsUtil.findRelativeFile(lastPathToJWSDP, base);
  }

  public void shortenClassReferences(PsiModifierList modifierList, Project project) throws IncorrectOperationException {
    CodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
  }

  public PsiPackage findPackage(@NotNull String packagePrefix, @NotNull Project project) {
    return PsiManager.getInstance(project).findPackage(packagePrefix);
  }

  public void setEffectiveLanguageLevel(@NotNull LanguageLevel jdk15, @NotNull Project project) {
    PsiManager.getInstance(project).setEffectiveLanguageLevel(jdk15);
  }

  public PsiElementFactory getElementsFactory(Project project) throws IncorrectOperationException {
    return PsiManager.getInstance(project).getElementFactory();
  }

  public PsiShortNamesCache getShortNamesCache(Project project) {
    return PsiManager.getInstance(project).getShortNamesCache();
  }

  public LanguageLevel getEffectiveLanguageLevel(@NotNull Module module) {
    return module.getEffectiveLanguageLevel();
  }

  public PsiFile createFileFromText(String fileName, String text, Project project) {
    return PsiManager.getInstance(project).getElementFactory().createFileFromText(fileName, text);
  }

  public void checkCreateClass(PsiDirectory directory, String className) throws IncorrectOperationException {
    directory.checkCreateClass(className);
  }

  public PsiPackage getPackageFor(PsiDirectory directory) {
    return directory.getPackage();
  }

  public PsiClass findClass(@NotNull String clazz, @NotNull Project project, @Nullable GlobalSearchScope scope) {
    return PsiManager.getInstance(project).findClass(clazz, scope != null ? scope : GlobalSearchScope.allScope(project));
  }

  public void checkIsIdentifier(@NotNull PsiManager manager, @NotNull String name) throws IncorrectOperationException {
    CheckUtil.checkIsIdentifier(manager, name);
  }

  public void setLanguageLevel(@NotNull Project project, @NotNull LanguageLevel jdk15) {
    ProjectRootManagerEx.getInstanceEx(project).setLanguageLevel(jdk15);
  }

  public void setModuleLanguageLevel(@NotNull Module module, @NotNull LanguageLevel jdk15) {
    ModuleRootManager.getInstance(module).setLanguageLevel(jdk15);
  }

  public VirtualFile getCompilerOutputPath(Module moduleForFile) {
    return ModuleRootManager.getInstance(moduleForFile).getCompilerOutputPath();
  }

  public String getSdkHome(@NotNull Sdk projectJdk) {
    return projectJdk.getHomePath();
  }

  public String getVMExecutablePathForSdk(@NotNull Sdk projectJdk) {
    return ((ProjectJdk) projectJdk).getVMExecutablePath();
  }

  public String getToolsJarPathForSdk(@NotNull Sdk projectJdk) {
    return ((ProjectJdk) projectJdk).getToolsPath();
  }

  public boolean isAcceptableSdk(@Nullable Sdk jdk) {
    return jdk instanceof ProjectJdk;
  }

  public boolean prepareFileForWrite(PsiFile containingFile) {
    return CodeInsightUtil.preparePsiElementForWrite(containingFile);
  }

  public boolean isJavaModuleType(@NotNull ModuleType moduleType) {
    return moduleType == ModuleType.JAVA;
  }

  public JavaProgramRunner getDefaultRunner() {
    return ExecutionRegistry.getInstance().getDefaultRunner();
  }

  public void addLookupItem(Set<LookupItem> set, String name) {
    LookupItemUtil.addLookupItem(set, name, "");
  }

  public MyPathReferenceProvider acquirePathReferenceProvider(Project project, boolean relativeFromWebRoot) {
    PsiReferenceProvider psiReferenceProvider = ReferenceProvidersRegistry.getInstance(project).getProviderByType(
      ReferenceProvidersRegistry.PATH_REFERENCES_PROVIDER);
    if (!relativeFromWebRoot) return new DelegatingReferenceProvider(psiReferenceProvider);
    final CustomizingReferenceProvider referenceProvider = new CustomizingReferenceProvider(
      (CustomizableReferenceProvider) psiReferenceProvider
    );
    referenceProvider.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, new Function<PsiFile, PsiElement>() {
      public PsiElement fun(PsiFile psiFile) {
        return FileReferenceSet.getAbsoluteTopLevelDirLocation(WebUtil.getWebModuleProperties(psiFile), psiFile.getProject(), psiFile);
      }
    });
    return new DelegatingReferenceProvider(referenceProvider);
  }

  public MyPathReferenceProvider acquireDynamicPathReferenceProvider(Project project) {
    PsiReferenceProvider psiReferenceProvider = ReferenceProvidersRegistry.getInstance(project).getProviderByType(
      ReferenceProvidersRegistry.DYNAMIC_PATH_REFERENCES_PROVIDER);
    return new DelegatingReferenceProvider(psiReferenceProvider);
  }

  public CloseAction createRunnerAction(RunContentDescriptor myDescriptor, Project project) {
    return new CloseAction(ExecutionRegistry.getInstance().getDefaultRunner(), myDescriptor, project);
  }

  public void showRunContent(RunContentManager contentManager, RunContentDescriptor myDescriptor) {
    contentManager.showRunContent(ExecutionRegistry.getInstance().getDefaultRunner(), myDescriptor);
  }

  public Sdk getInternalJdk() {
    return ProjectJdkTable.getInstance().getInternalJdk();
  }

  public void registerStdResource(String url, String s, Class aClass) {
    ExternalResourceManagerEx.getInstanceEx().addStdResource(url, s, aClass);
  }

  public void unregisterResource(String url) {
    ExternalResourceManagerEx.getInstanceEx().removeResource(url);
  }

  public MyReferenceProvider acquireClassReferenceProvider(@NotNull Project project) {
    return new DelegatingReferenceProvider(ReferenceProvidersRegistry.getInstance(project).getProviderByType(ReferenceProvidersRegistry.CLASS_REFERENCE_PROVIDER));
  }

  public void registerXmlAttributeValueReferenceProvider(Project project, String[] attributeNames, ElementFilter filter, MyReferenceProvider wsdlReferenceProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerXmlAttributeValueReferenceProvider(attributeNames, filter, new MyReferenceProviderWrapper(wsdlReferenceProvider));
  }

  public void registerReferenceProvider(Project project, ElementFilter filter, Class aClass, MyReferenceProvider javaProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerReferenceProvider(filter, aClass, new MyReferenceProviderWrapper(javaProvider));
  }

  public void registerXmlTagReferenceProvider(Project project, String[] tagCandidateNames, ElementFilter tagFilter, boolean b, MyReferenceProvider xmlReferenceProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerXmlTagReferenceProvider(tagCandidateNames, tagFilter, b,
      new MyReferenceProviderWrapper(xmlReferenceProvider)
    );
  }

  public ProjectJdk getProjectJdkFromModule(@NotNull Module module) {
    return ModuleRootManager.getInstance(module).getJdk();
  }

  public PsiElement handleContentChange(@NotNull PsiElement element, @NotNull TextRange range, @NotNull String value) throws IncorrectOperationException {
    return ReferenceProvidersRegistry.getInstance(element.getProject()).getManipulator(element).handleContentChange(element, range, value);
  }

  private static class DelegatingReferenceProvider extends MyPathReferenceProvider {
    private final PsiReferenceProvider provider;

    DelegatingReferenceProvider(PsiReferenceProvider provider) {
      this.provider = provider;
    }

    @NotNull
    public PsiReference[] getReferencesByElement(PsiElement psiElement) {
      return provider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i) {
      return provider.getReferencesByString(s, psiElement, ReferenceType.FILE_TYPE, i);
    }
  }

  private static class MyReferenceProviderWrapper implements PsiReferenceProvider {
    private final MyReferenceProvider referenceProvider;

    public MyReferenceProviderWrapper(MyReferenceProvider wsdlReferenceProvider) {
      this.referenceProvider = wsdlReferenceProvider;
    }

    @NotNull
    public PsiReference[] getReferencesByElement(PsiElement psiElement) {
      return referenceProvider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByElement(PsiElement psiElement, ReferenceType referenceType) {
      return referenceProvider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByString(String s, PsiElement psiElement, ReferenceType referenceType, int i) {
      return referenceProvider.getReferencesByElement(psiElement);
    }

    public void handleEmptyContext(PsiScopeProcessor psiScopeProcessor, PsiElement psiElement) {
    }
  }

  public XmlTag createTagFromText(@NotNull String text, @NotNull Project project) throws IncorrectOperationException {
    return getElementsFactory(project).createTagFromText(text);
  }
}
