package com.intellij.seam.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.impl.model.xml.SeamDomModelImpl;
import com.intellij.seam.model.xml.components.SeamComponents;
import com.intellij.seam.utils.SeamConfigFileUtils;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.model.impl.DomModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeamDomModelFactory extends DomModelFactory<SeamComponents, SeamDomModel, PsiElement> {

  public SeamDomModelFactory(Project project) {
    super(SeamComponents.class, project, "Seam");
  }

  @NotNull
  public Object[] computeDependencies(@Nullable SeamDomModel model, @Nullable Module module) {
    final ArrayList<Object> dependencies = new ArrayList<Object>();
    dependencies.add(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
    if (module != null) {
      dependencies.add(ProjectRootManager.getInstance(module.getProject()));
    }
    return ArrayUtil.toObjectArray(dependencies);
  }

  protected List<SeamDomModel> computeAllModels(@NotNull final Module module) {
    final ArrayList<SeamDomModel> models = new ArrayList<SeamDomModel>();
    for (XmlFile xmlFile : SeamConfigFileUtils.getConfigurationFiles(module)) {
      SeamDomModel seamModel = createSingleModel(xmlFile, module);
      if (seamModel != null) {
        models.add(seamModel);
      }
    }

    return models;
  }

  protected SeamDomModel computeModel(@NotNull XmlFile psiFile, @Nullable Module module) {
    if (module == null) return null;

    SeamDomModel model = super.computeModel(psiFile, module);

    return model != null ? model : createSingleModel(psiFile, module);
  }

  @Nullable
  private SeamDomModel createSingleModel(final XmlFile psiFile, final Module module) {
    final DomFileElement<SeamComponents> componentsDomFileElement = getDomRoot(psiFile);
    if (componentsDomFileElement != null) {
      final HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);

      addImports(files, componentsDomFileElement.getRootElement());

      DomFileElement<SeamComponents> fileElement = files.size() > 1 ? createMergedModelRoot(files) : componentsDomFileElement;

      return fileElement == null ? null : new SeamDomModelImpl(module, fileElement, files);
    }
    return null;
  }

  protected SeamDomModel createCombinedModel(@NotNull final Set<XmlFile> configFiles,
                                          @NotNull final DomFileElement<SeamComponents> mergedModel,
                                          final SeamDomModel firstModel,
                                          final Module module) {
    return new SeamDomModelImpl(module, mergedModel, configFiles);
  }

  private static void addImports(Set<XmlFile> files, final SeamComponents components) {
    for (GenericDomValue<String> domValue : components.getImports()) {
      // todo
    }
  }
}
