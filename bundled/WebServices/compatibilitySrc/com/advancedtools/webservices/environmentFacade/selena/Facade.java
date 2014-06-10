package com.advancedtools.webservices.environmentFacade.selena;

import com.advancedtools.webservices.environmentFacade.SelenaUtils;
import com.advancedtools.webservices.references.MyPathReferenceProvider;
import com.advancedtools.webservices.references.MyReferenceProvider;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.execution.ExecutionRegistry;
import com.intellij.execution.ui.CloseAction;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizableReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.CustomizingReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.j2ee.openapi.ex.ExternalResourceManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @by maxim
 */
public class Facade extends SelenaUtils.SelenaEnvironmentFacadeBase {
  private MyPathReferenceProvider myDynamicPathReferenceProvider;
  private MyPsiReferenceProvider myStaticPathReferenceProvider;

  public Sdk getInternalJdk() {
    return ProjectJdkTable.getInstance().getInternalJdk();
  }
  
  public void checkCreateClass(PsiDirectory directory, String className) throws IncorrectOperationException {
    directory.checkCreateClass(className);
  }

  public void checkIsIdentifier(@NotNull PsiManager manager, @NotNull String name) throws IncorrectOperationException {
    CheckUtil.checkIsIdentifier(manager, name);
  }

  public void registerStdResource(String url, String s, Class aClass) {
    ExternalResourceManagerEx.getInstanceEx().addStdResource(url, s, aClass);
  }

  public void unregisterResource(String url) {
    ExternalResourceManagerEx.getInstanceEx().removeResource(url);
  }
  
  public PsiPackage getPackageFor(PsiDirectory directory) {
    return directory.getPackage();
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

  public XmlTag createTagFromText(@NotNull String text, @NotNull Project project) throws IncorrectOperationException {
    return getElementsFactory(project).createTagFromText(text);
  }

  public PsiShortNamesCache getShortNamesCache(Project project) {
    return PsiManager.getInstance(project).getShortNamesCache();
  }

  public LanguageLevel getEffectiveLanguageLevel(@NotNull Module module) {
    return module.getEffectiveLanguageLevel();
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

  public String getToolsJarPathForSdk(@NotNull Sdk projectJdk) {
    return ((ProjectJdk) projectJdk).getToolsPath();
  }

  public String getVMExecutablePathForSdk(@NotNull Sdk projectJdk) {
    return ((ProjectJdk) projectJdk).getVMExecutablePath();
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

  public void addLookupItem(Set<LookupItem> set, String name) {
    LookupItemUtil.addLookupItem(set, name, "");
  }

  public PsiClass findClass(@NotNull String clazz, @NotNull Project project, @Nullable GlobalSearchScope scope) {
    return PsiManager.getInstance(project).findClass(clazz, scope != null ? scope : GlobalSearchScope.allScope(project));
  }

  public PsiFile createFileFromText(String fileName, String text, Project project) {
    return PsiManager.getInstance(project).getElementFactory().createFileFromText(fileName, text);
  }

  public ProjectJdk getProjectJdkFromModule(@NotNull Module module) {
    return ModuleRootManager.getInstance(module).getJdk();
  }

  public PsiElement handleContentChange(@NotNull PsiElement element, @NotNull TextRange range, @NotNull String value) throws IncorrectOperationException {
    return ReferenceProvidersRegistry.getInstance(element.getProject()).getManipulator(element).handleContentChange(element, range, value);
  }

  public void registerXmlTagReferenceProvider(Project project, String[] tagCandidateNames,
                                              ElementFilter tagFilter, boolean b, MyReferenceProvider xmlReferenceProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerXmlTagReferenceProvider(
      tagCandidateNames, tagFilter, true, new MyReferenceProviderWrapper(xmlReferenceProvider));
  }

  public void registerXmlAttributeValueReferenceProvider(Project project, String[] attributeNames, ElementFilter filter, MyReferenceProvider wsdlReferenceProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerXmlAttributeValueReferenceProvider(
      attributeNames,
      filter,
      new MyReferenceProviderWrapper(wsdlReferenceProvider)
    );
  }

  public void registerReferenceProvider(Project project, ElementFilter filter, Class aClass, MyReferenceProvider javaProvider) {
    ReferenceProvidersRegistry.getInstance(project).registerReferenceProvider(filter, aClass, new MyReferenceProviderWrapper(javaProvider));
  }

  public CloseAction createRunnerAction(RunContentDescriptor myDescriptor, Project project) {
    return new CloseAction(ExecutionRegistry.getInstance().getDefaultRunner(), myDescriptor, project);
  }

  public void showRunContent(RunContentManager contentManager, RunContentDescriptor myDescriptor) {
    contentManager.showRunContent(ExecutionRegistry.getInstance().getDefaultRunner(), myDescriptor);
  }

  public MyReferenceProvider acquireClassReferenceProvider(@NotNull Project project) {
    final PsiReferenceProvider psiReferenceProvider = ReferenceProvidersRegistry.getInstance(project).getProviderByType(
      ReferenceProvidersRegistry.CLASS_REFERENCE_PROVIDER);
    return new MyReferenceProvider() {
      public PsiReference[] getReferencesByElement(PsiElement psiElement) {
        return psiReferenceProvider.getReferencesByElement(psiElement);
      }
    };
  }

  public MyPathReferenceProvider acquirePathReferenceProvider(Project project, boolean relativeFromWebRoot) {
    if (myStaticPathReferenceProvider == null) {
      myStaticPathReferenceProvider = new MyPsiReferenceProvider();
    }

    MyPathReferenceProvider result = myStaticPathReferenceProvider;
    if (relativeFromWebRoot) {
      result = new MyCustomizingReferenceProvider();
      ((MyCustomizingReferenceProvider) result).addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    return result;
  }

  public MyPathReferenceProvider acquireDynamicPathReferenceProvider(Project project) {
    if (myDynamicPathReferenceProvider == null) {
      myDynamicPathReferenceProvider = new MyPathReferenceProvider() {
        final PathReferenceManager myPathReferenceManager = PathReferenceManager.getInstance();

        @NotNull
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
          return myPathReferenceManager.createReferences(psiElement, false, false, false);
        }

        @NotNull
        public PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i) {
          return getReferencesByElement(psiElement);
        }
      };
    }
    return myDynamicPathReferenceProvider;
  }

  private static class MyReferenceProviderWrapper implements PsiReferenceProvider {
    private final MyReferenceProvider wsdlReferenceProvider;

    public MyReferenceProviderWrapper(MyReferenceProvider wsdlReferenceProvider) {
      this.wsdlReferenceProvider = wsdlReferenceProvider;
    }

    @NotNull
    public PsiReference[] getReferencesByElement(PsiElement psiElement) {
      return wsdlReferenceProvider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByElement(PsiElement psiElement, ReferenceType referenceType) {
      return wsdlReferenceProvider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByString(String s, PsiElement psiElement, ReferenceType referenceType, int i) {
      return wsdlReferenceProvider.getReferencesByElement(psiElement);
    }

    public void handleEmptyContext(PsiScopeProcessor psiScopeProcessor, PsiElement psiElement) {
    }
  }

  private static class MyPsiReferenceProvider extends MyPathReferenceProvider {
    private final CustomizableReferenceProvider myWrappedCustomizableProvider;

    MyPsiReferenceProvider() {
      myWrappedCustomizableProvider = new CustomizableReferenceProvider() {
        public Map<CustomizationKey, Object> myCustomizationOptions;

        public void setOptions(@Nullable Map<CustomizationKey, Object> customizationKeyObjectMap) {
          myCustomizationOptions = customizationKeyObjectMap;
        }

        @Nullable
        public Map<CustomizationKey, Object> getOptions() {
          return myCustomizationOptions;
        }

        @NotNull
        public PsiReference[] getReferencesByElement(PsiElement psiElement) {
          final FileReferenceSet set = FileReferenceSet.createSet(psiElement, false, true, false);

          final Function<PsiFile, Collection<PsiFileSystemItem>> o = FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION.getValue(getOptions());
          if (o != null) set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, o);

          return set.getAllReferences();
        }

        @NotNull
        public PsiReference[] getReferencesByElement(PsiElement psiElement, ReferenceType referenceType) {
          return new PsiReference[0];
        }

        @NotNull
        public PsiReference[] getReferencesByString(String s, PsiElement psiElement, ReferenceType referenceType, int i) {
          final FileReferenceSet set = new FileReferenceSet(s, psiElement, i, this, true);
          final Function<PsiFile, Collection<PsiFileSystemItem>> o = FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION.getValue(getOptions());
          if (o != null) set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, o);
          return set.getAllReferences();
        }

        public void handleEmptyContext(PsiScopeProcessor psiScopeProcessor, PsiElement psiElement) {
        }
      };
    }

    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
      return myWrappedCustomizableProvider.getReferencesByElement(psiElement);
    }

    @NotNull
    public PsiReference[] getReferencesByString(String s, PsiElement psiElement, int i) {
      return myWrappedCustomizableProvider.getReferencesByString(s, psiElement, ReferenceType.FILE_TYPE, i);
    }
  }

  private class MyCustomizingReferenceProvider extends MyPathReferenceProvider {
    private final CustomizingReferenceProvider myWrappedProvider;

    public MyCustomizingReferenceProvider() {
      myWrappedProvider = new CustomizingReferenceProvider(
        myStaticPathReferenceProvider.myWrappedCustomizableProvider
      );
    }

    public PsiReference[] getReferencesByElement(PsiElement psiElement) {
      return myWrappedProvider.getReferencesByElement(psiElement);
    }

    public PsiReference[] getReferencesByString(String str, PsiElement position, int offsetInPosition) {
      return myWrappedProvider.getReferencesByString(str, position, ReferenceType.FILE_TYPE, offsetInPosition);
    }

    public void addCustomization(CustomizableReferenceProvider.CustomizationKey<Function<PsiFile, Collection<PsiFileSystemItem>>> defaultPathEvaluatorOption,
                                 Function<PsiFile, Collection<PsiFileSystemItem>> absoluteTopLevel) {
      myWrappedProvider.addCustomization(defaultPathEvaluatorOption, absoluteTopLevel);
    }
  }
}
