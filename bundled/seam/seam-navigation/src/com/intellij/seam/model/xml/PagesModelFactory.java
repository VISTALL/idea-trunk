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
import com.intellij.seam.impl.PagesModelImpl;
import com.intellij.seam.model.xml.pages.Page;
import com.intellij.seam.model.xml.pages.Pages;
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
public class PagesModelFactory extends DomModelFactory<Pages, PagesModel, PsiElement> {

  public PagesModelFactory(final Project project) {
    super(Pages.class, project, "pageflow");
  }

  protected List<PagesModel> computeAllModels(@NotNull final Module module) {
    List<PagesModel> models = new ArrayList<PagesModel>();

    final GlobalSearchScope moduleContentScope = new ModuleContentRootSearchScope(module);
    final Collection<VirtualFile> pageflowlFiles = DomService.getInstance().getDomFileCandidates(Page.class, module.getProject(), moduleContentScope);

    for (VirtualFile pageflowlFile : pageflowlFiles) {
      final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(pageflowlFile);
      if (file instanceof XmlFile) {
        final PagesModel pageflowModel = computeModel((XmlFile)file, module);
        if (pageflowModel != null) {
           models.add(pageflowModel);
        }
      }
    }

    return models;
  }

  @Nullable
  public PagesModel getModelByConfigFile(@Nullable XmlFile psiFile) {
    if (psiFile == null) {
      return null;
    }
    return computeModel(psiFile, ModuleUtil.findModuleForPsiElement(psiFile));
  }

  protected PagesModel computeModel(@NotNull XmlFile psiFile, @Nullable Module module) {
    if (module == null) return null;

    return createSingleModel(psiFile, module);
  }

  @Nullable
  private PagesModel createSingleModel(final XmlFile psiFile, final Module module) {
    final DomFileElement<Pages> componentsDomFileElement = getDomRoot(psiFile);
    if (componentsDomFileElement != null) {
      final HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);

      DomFileElement<Pages> fileElement = files.size() > 1 ? createMergedModelRoot(files) : componentsDomFileElement;

      if (fileElement != null) {
        return new PagesModelImpl(module, fileElement, files);
      }
    }
    return null;
  }

  protected PagesModel createCombinedModel(@NotNull final Set<XmlFile> configFiles,
                                              @NotNull final DomFileElement<Pages> mergedModel,
                                              final PagesModel firstModel,
                                              final Module module) {
    throw new UnsupportedOperationException();
  }

}
