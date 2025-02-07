package com.intellij.seam.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.impl.PageflowModelImpl;
import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.ModuleContentRootSearchScope;
import com.intellij.util.xml.model.impl.DomModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowModelFactory extends DomModelFactory<PageflowDefinition, PageflowModel, PsiElement> {

  public PageflowModelFactory(final Project project) {
    super(PageflowDefinition.class, project, "pageflow");
  }

  protected List<PageflowModel> computeAllModels(@NotNull final Module module) {
    List<PageflowModel> models = new ArrayList<PageflowModel>();

    final GlobalSearchScope moduleContentScope = new ModuleContentRootSearchScope(module);
    final Collection<VirtualFile> pageflowlFiles = DomService.getInstance().getDomFileCandidates(PageflowDefinition.class, module.getProject(), moduleContentScope);

    for (VirtualFile pageflowlFile : pageflowlFiles) {
      final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(pageflowlFile);
      if (file instanceof XmlFile) {
        final PageflowModel pageflowModel = computeModel((XmlFile)file, module);
        if (pageflowModel != null) {
           models.add(pageflowModel);
        }
      }
    }

    return models;
  }

  @Nullable
  public PageflowModel getModelByConfigFile(@Nullable XmlFile psiFile) {
    if (psiFile == null) {
      return null;
    }
    return computeModel(psiFile, ModuleUtil.findModuleForPsiElement(psiFile));
  }

  protected PageflowModel computeModel(@NotNull XmlFile psiFile, @Nullable Module module) {
    if (module == null) return null;

    return createSingleModel(psiFile, module);
  }

  @Nullable
  private PageflowModel createSingleModel(final XmlFile psiFile, final Module module) {
    final DomFileElement<PageflowDefinition> componentsDomFileElement = getDomRoot(psiFile);
    if (componentsDomFileElement != null) {
      final HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);

      DomFileElement<PageflowDefinition> fileElement = files.size() > 1 ? createMergedModelRoot(files) : componentsDomFileElement;

      if (fileElement != null) {
        return new PageflowModelImpl(module, fileElement, files);
      }
    }
    return null;
  }

  protected PageflowModel createCombinedModel(@NotNull final Set<XmlFile> configFiles,
                                              @NotNull final DomFileElement<PageflowDefinition> mergedModel,
                                              final PageflowModel firstModel,
                                              final Module module) {
    throw new UnsupportedOperationException();
  }
}
