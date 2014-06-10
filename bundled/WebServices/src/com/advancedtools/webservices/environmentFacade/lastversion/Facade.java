package com.advancedtools.webservices.environmentFacade.lastversion;

import com.advancedtools.webservices.environmentFacade.SelenaUtils;
import com.advancedtools.webservices.references.MyPathReferenceProvider;
import com.advancedtools.webservices.references.MyReferenceProvider;
import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.javaee.UriUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizableReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizingReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Maxim
 */
public class Facade extends SelenaUtils.SelenaEnvironmentFacadeBase {
  private MyPsiReferenceProvider myStaticPathReferenceProvider;
  private MyPathReferenceProvider myDynamicPathReferenceProvider;
  private static final ProcessingContext instance = new ProcessingContext();

  public VirtualFile findRelativeFile(String lastPathToJWSDP, VirtualFile base) {
    return UriUtil.findRelativeFile(lastPathToJWSDP, base);
  }

  public void shortenClassReferences(PsiModifierList modifierList, Project project) throws IncorrectOperationException {
    JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
  }

  public PsiPackage findPackage(String packagePrefix, Project project) {
    return JavaPsiFacade.getInstance(project).findPackage(packagePrefix);
  }

  public void setEffectiveLanguageLevel(@NotNull LanguageLevel jdk15, @NotNull Project project) {
    LanguageLevelProjectExtension.getInstance(project).setLanguageLevel(jdk15);
  }

  public PsiElementFactory getElementsFactory(Project project) throws IncorrectOperationException {
    return JavaPsiFacade.getInstance(project).getElementFactory();
  }

  public XmlTag createTagFromText(final String text, @NotNull final Project project) throws IncorrectOperationException {
    return XmlElementFactory.getInstance(project).createTagFromText(text);
  }

  public PsiShortNamesCache getShortNamesCache(Project project) {
    return JavaPsiFacade.getInstance(project).getShortNamesCache();
  }

  public LanguageLevel getEffectiveLanguageLevel(Module module) {
    return LanguageLevelUtil.getEffectiveLanguageLevel(module);
  }

  public PsiFile createFileFromText(String fileName, String text, Project project) {
    return PsiFileFactory.getInstance(project).createFileFromText(fileName, text);
  }

  public void checkCreateClass(PsiDirectory directory, String className) throws IncorrectOperationException {
    JavaDirectoryService.getInstance().checkCreateClass(directory, className);
  }

  public MyPathReferenceProvider acquirePathReferenceProvider(final Project project, final boolean relativeFromWebRoot) {
    if (myStaticPathReferenceProvider == null) {
      myStaticPathReferenceProvider = new MyPsiReferenceProvider();
    }

    MyPathReferenceProvider result = myStaticPathReferenceProvider;
    if (relativeFromWebRoot) {
      result = new MyCustomizingReferenceProvider();
      ((MyCustomizingReferenceProvider)result)
        .addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    return result;
  }

  public MyPathReferenceProvider acquireDynamicPathReferenceProvider(final Project project) {
    if (myDynamicPathReferenceProvider == null) {
      myDynamicPathReferenceProvider = new MyPathReferenceProvider() {
        final PathReferenceManager myPathReferenceManager = PathReferenceManager.getInstance();

        @NotNull
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
          return myPathReferenceManager.createReferences(psiElement, false, false, false);
        }

        @NotNull
        public PsiReference[] getReferencesByString(final String s, final PsiElement psiElement, final int i) {
          return getReferencesByElement(psiElement);
        }
      };
    }
    return myDynamicPathReferenceProvider;
  }

  public PsiPackage getPackageFor(PsiDirectory directory) {
    return JavaDirectoryService.getInstance().getPackage(directory);
  }

  public PsiClass findClass(@NotNull String clazz, @NotNull Project project, @Nullable GlobalSearchScope scope) {
    return JavaPsiFacade.getInstance(project).findClass(clazz, scope != null ? scope : GlobalSearchScope.allScope(project));
  }

  public void checkIsIdentifier(@NotNull PsiManager manager, @NotNull String name) throws IncorrectOperationException {
    PsiUtil.checkIsIdentifier(manager, name);
  }

  public Sdk getProjectJdkFromModule(@NotNull Module module) {
    return ModuleRootManager.getInstance(module).getSdk();
  }

  public PsiElement handleContentChange(@NotNull PsiElement myElement, @NotNull TextRange myRange, @NotNull String string)
    throws IncorrectOperationException {
    return ElementManipulators.getManipulator(myElement).handleContentChange(myElement, myRange, string);
  }

  public void setLanguageLevel(@NotNull Project project, @NotNull LanguageLevel jdk15) {
    LanguageLevelProjectExtension.getInstance(project).setLanguageLevel(jdk15);
  }

  public void setModuleLanguageLevel(Module module, LanguageLevel jdk15) {
    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
    rootModel.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(jdk15);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        rootModel.commit();
      }
    });
  }

  public VirtualFile getCompilerOutputPath(Module moduleForFile) {
    return CompilerModuleExtension.getInstance(moduleForFile).getCompilerOutputPath();
  }

  public String getSdkHome(@NotNull Sdk projectJdk) {
    return projectJdk.getHomeDirectory().getPath();
  }

  public String getVMExecutablePathForSdk(@NotNull Sdk jdk) {
    return ((JavaSdkType)jdk.getSdkType()).getVMExecutablePath(jdk);
  }

  public String getToolsJarPathForSdk(@NotNull Sdk jdk) {
    return ((JavaSdkType)jdk.getSdkType()).getToolsPath(jdk);
  }

  public boolean isAcceptableSdk(@Nullable Sdk jdk) {
    return jdk != null && jdk.getSdkType() instanceof JavaSdkType;
  }

  public boolean prepareFileForWrite(PsiFile containingFile) {
    return CodeInsightUtilBase.prepareFileForWrite(containingFile);
  }

  public boolean isJavaModuleType(@NotNull ModuleType moduleType) {
    return moduleType == StdModuleTypes.JAVA;
  }

  public void addLookupItem(Set<LookupItem> set, String name) {
    LookupItemUtil.addLookupItem((Collection)set, name);
  }

  public MyReferenceProvider acquireClassReferenceProvider(@NotNull Project project) {
    return new PsiReferenceProviderWrapper(CommonReferenceProviderTypes.getInstance(project).getClassReferenceProvider());
  }

  public void registerXmlAttributeValueReferenceProvider(Project project,
                                                         String[] attributeNames,
                                                         ElementFilter filter,
                                                         MyReferenceProvider wsdlReferenceProvider) {
    XmlUtil.registerXmlAttributeValueReferenceProvider(ReferenceProvidersRegistry.getInstance(project), attributeNames, filter,
                                                       new MyReferenceProviderWrapper(wsdlReferenceProvider));
  }

  public void registerReferenceProvider(Project project, ElementFilter filter, Class aClass, MyReferenceProvider javaProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerReferenceProvider(filter, aClass, new MyReferenceProviderWrapper(javaProvider));
  }

  public CloseAction createRunnerAction(RunContentDescriptor myDescriptor, Project project) {
    return new CloseAction(DefaultRunExecutor.getRunExecutorInstance(), myDescriptor, project);
  }

  public void showRunContent(RunContentManager contentManager, RunContentDescriptor myDescriptor) {
    contentManager.showRunContent(DefaultRunExecutor.getRunExecutorInstance(), myDescriptor);
  }

  public Sdk getInternalJdk() {
    return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
  }

  public void registerXmlTagReferenceProvider(final Project project,
                                              final String[] tagCandidateNames,
                                              final ElementFilter tagFilter,
                                              final boolean b,
                                              final MyReferenceProvider xmlReferenceProvider) {
    XmlUtil.registerXmlTagReferenceProvider(ReferenceProvidersRegistry.getInstance(project), tagCandidateNames, tagFilter, b,
                                            new MyReferenceProviderWrapper(xmlReferenceProvider));
  }

  static class MyReferenceProviderWrapper extends PsiReferenceProvider {
    private final MyReferenceProvider provider;

    public MyReferenceProviderWrapper(final MyReferenceProvider javaProvider) {
      provider = javaProvider;
    }

    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      return provider.getReferencesByElement(element);
    }
  }

  static class PsiReferenceProviderWrapper extends MyReferenceProvider {
    final PsiReferenceProvider provider;

    public PsiReferenceProviderWrapper(final PsiReferenceProvider provider) {
      this.provider = provider;
    }

    public PsiReference[] getReferencesByElement(final PsiElement psiElement) {
      return provider.getReferencesByElement(psiElement, instance);
    }
  }

  private static class MyPsiReferenceProvider extends MyPathReferenceProvider {
    private final MyCustomizableReferenceProvider myWrappedCustomizableProvider;

    MyPsiReferenceProvider() {
      myWrappedCustomizableProvider = new MyCustomizableReferenceProvider();
    }

    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
      return myWrappedCustomizableProvider.getReferencesByElement(psiElement, instance);
    }

    @NotNull
    public PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i) {
      return myWrappedCustomizableProvider.getReferencesByString(s, psiElement, i);
    }

    private static class MyCustomizableReferenceProvider extends PsiReferenceProvider implements CustomizableReferenceProvider  {
      public Map<CustomizationKey, Object> myCustomizationOptions;

      public void setOptions(@Nullable Map<CustomizationKey, Object> customizationKeyObjectMap) {
        myCustomizationOptions = customizationKeyObjectMap;
      }

      @Nullable
      public Map<CustomizationKey, Object> getOptions() {
        return myCustomizationOptions;
      }

      @NotNull
        public PsiReference[] getReferencesByElement(PsiElement psiElement, ProcessingContext context) {
        final FileReferenceSet set = FileReferenceSet.createSet(psiElement, false, true, false);

        final Function<PsiFile, Collection<PsiFileSystemItem>> o = FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION.getValue(getOptions());
        if (o != null) set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, o);

        return set.getAllReferences();
      }

      @NotNull
        public PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i) {
        final FileReferenceSet set = new FileReferenceSet(s, psiElement, i, this, true);
        final Function<PsiFile, Collection<PsiFileSystemItem>> o = FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION.getValue(getOptions());
        if (o != null) set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, o);
        return set.getAllReferences();
      }
    }
  }

  private class MyCustomizingReferenceProvider extends MyPathReferenceProvider {
    private final CustomizingReferenceProvider myWrappedProvider;

    public MyCustomizingReferenceProvider() {
      myWrappedProvider = new CustomizingReferenceProvider(myStaticPathReferenceProvider.myWrappedCustomizableProvider);
    }

    public PsiReference[] getReferencesByElement(PsiElement psiElement) {
      return myWrappedProvider.getReferencesByElement(psiElement, instance);
    }

    public PsiReference[] getReferencesByString(String str, PsiElement position, int offsetInPosition) {
      try {
        myStaticPathReferenceProvider.myWrappedCustomizableProvider.setOptions(myWrappedProvider.getOptions());
        return myStaticPathReferenceProvider.myWrappedCustomizableProvider.getReferencesByString(str, position, offsetInPosition);
      } finally {
        myStaticPathReferenceProvider.myWrappedCustomizableProvider.setOptions(myWrappedProvider.getOptions());
      }
    }

    public void addCustomization(CustomizableReferenceProvider.CustomizationKey<Function<PsiFile, Collection<PsiFileSystemItem>>> defaultPathEvaluatorOption,
                                 Function<PsiFile, Collection<PsiFileSystemItem>> absoluteTopLevel) {
      myWrappedProvider.addCustomization(defaultPathEvaluatorOption, absoluteTopLevel);
    }
  }
}
