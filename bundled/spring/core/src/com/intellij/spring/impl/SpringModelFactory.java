/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.facet.FacetFinder;
import com.intellij.facet.impl.FacetFinderImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringImport;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
*/
public class SpringModelFactory extends DomModelFactory<Beans, SpringModel, PsiElement> {

  protected SpringModelFactory(Project project) {
    super(Beans.class, project, "spring");
  }

  @NotNull
  public Object[] computeDependencies(@Nullable SpringModel model, @Nullable Module module) {
    final ArrayList<Object> dependencies = new ArrayList<Object>(5);
    dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
    if (module != null) {
      final Project project = module.getProject();
      final FacetFinderImpl finder = (FacetFinderImpl)FacetFinder.getInstance(project);
      if (finder != null) {
        dependencies.add(finder.getAllFacetsOfTypeModificationTracker(SpringFacet.FACET_TYPE_ID));
      }
      dependencies.add(ProjectRootManager.getInstance(project));
      final SpringFacet facet = SpringFacet.getInstance(module);
      if (facet != null){
        dependencies.add(facet.getConfiguration());
      }
    }
    return dependencies.toArray(new Object[dependencies.size()]);
  }

  protected List<SpringModel> computeAllModels(@NotNull final Module module) {

    final SpringFacet facet = SpringFacet.getInstance(module);
    if (facet == null) {
      return Collections.emptyList();
    }
    final SpringManager springManager = SpringManager.getInstance(module.getProject());
    final Set<SpringFileSet> fileSets = springManager.getAllSets(facet);
    final ArrayList<SpringModel> models = new ArrayList<SpringModel>(fileSets.size());
    for (SpringFileSet set: fileSets) {
      if (set.isRemoved()) {
        continue;
      }
      final SpringModelImpl model = createModel(set, module);
      if (model != null) {
        models.add(model);
      }
    }
    setDependencies(models);
    return models;
  }

  @Nullable
  public SpringModelImpl createModel(final SpringFileSet set, final Module module) {
    final PsiManager psiManager = PsiManager.getInstance(module.getProject());
    Set<XmlFile> files = new LinkedHashSet<XmlFile>(set.getFiles().size());
    for (VirtualFilePointer filePointer: set.getFiles()) {
      final VirtualFile file = filePointer.getFile();
      if (file == null) {
        continue;
      }
      final PsiFile psiFile = psiManager.findFile(file);
      if (psiFile instanceof XmlFile) {
        final Beans dom = getDom((XmlFile)psiFile);
        if (dom != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, dom);
        }
      }
    }
    if (files.size() > 0) {
      final DomFileElement<Beans> element = createMergedModelRoot(files);
      if (element != null) {
        return new SpringModelImpl(element, files, module, set);
      }
    }
    return null;
  }

  private static void setDependencies(final List<SpringModel> models) {
    for (SpringModel model : models) {
      final List<String> dependencies = ((SpringModelImpl)model).getFileSet().getDependencies();
      if (dependencies.size() > 0) {
        final ArrayList<SpringModel> list = new ArrayList<SpringModel>(dependencies.size());
        for (Iterator<String> i = dependencies.iterator(); i.hasNext();) {
          String dependency = i.next();
          boolean valid = false;
          for (SpringModel depModel : models) {
            if (depModel != model && depModel.getId().equals(dependency)) {
              list.add(depModel);
              valid = true;
              break;
            }
          }
          if (!valid) {
            i.remove();
          }
        }
        ((SpringModelImpl)model).setDependencies(list.toArray(new SpringModel[list.size()]));
      }
    }
  }

  protected SpringModel computeModel(@NotNull XmlFile psiFile, @Nullable Module module) {
    // trying to compute model for given module...
    SpringModel model = super.computeModel(psiFile, module);
    if (model != null) {
      return model;
    }
    final ModuleManager moduleManager = ModuleManager.getInstance(psiFile.getProject());
    if (module != null) {
      final List<Module> dependentModules = moduleManager.getModuleDependentModules(module);
      for (Module dependentModule: dependentModules) {
        model = super.computeModel(psiFile, dependentModule);
        if (model != null) {
          return model;
        }
      }
    }
    final Module[] allModules = moduleManager.getModules();
    for (Module aModule: allModules) {
      model = super.computeModel(psiFile, aModule);
      if (model != null) {
        return model;
      }
    }

    // no configuration found; compute model for single file...
    final DomFileElement<Beans> beans = getDomRoot(psiFile);
    if (beans != null) {
      final HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);
      addIncludes(files, beans.getRootElement());
      return new SpringModelImpl(files.size() > 1 ? createMergedModelRoot(files) : beans, files, module, null);
    }
    return null;
  }

  protected SpringModel createCombinedModel(@NotNull final Set<XmlFile> configFiles,
                                            @NotNull final DomFileElement<Beans> mergedModel,
                                            final SpringModel firstModel,
                                            final Module module) {
    return new SpringModelImpl(mergedModel, configFiles, module, null);
  }

  private void addIncludes(Set<XmlFile> files, final Beans dom) {
    for (SpringImport imp: dom.getImports()) {
      final PsiFile psiFile = imp.getResource().getValue();
      if (psiFile instanceof XmlFile && !files.contains(psiFile)) {
        final Beans child = getDom((XmlFile)psiFile);
        if (child != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, child);
        }
      }
    }
  }
}
