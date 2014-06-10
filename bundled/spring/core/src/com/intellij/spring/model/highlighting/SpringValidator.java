/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.highlighting;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.util.InspectionValidator;
import com.intellij.openapi.compiler.util.InspectionValidatorUtil;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SpringValidator extends InspectionValidator {

  public SpringValidator(SpringApplicationComponent component) {
    super(SpringBundle.message("model.inspection.validator.description.text"), SpringBundle.message("model.inspection.validator.progress.text"), component);
  }

  private static boolean isAvailableOnModule(Module module) {
    return SpringFacet.getInstance(module) != null;
  }

  public boolean isAvailableOnScope(@NotNull CompileScope scope) {
    for (Module module: scope.getAffectedModules()) {
      if (isAvailableOnModule(module)) {
        return true;
      }
    }
    return false;
  }

  public Collection<VirtualFile> getFilesToProcess(final Project project, final CompileContext context) {
    Set<VirtualFile> files = new HashSet<VirtualFile>();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final SpringManager springManager = SpringManager.getInstance(project);

    for (final VirtualFile file : context.getCompileScope().getFiles(StdFileTypes.XML, false)) {
      final Module module = context.getModuleByFile(file);
      if (module != null && isAvailableOnModule(module)) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile instanceof XmlFile) {
          final SpringModel model = springManager.getSpringModelByFile((XmlFile)psiFile);
          if (model != null) {
            for (final XmlFile configFile : model.getConfigFiles()) {
              ContainerUtil.addIfNotNull(configFile.getVirtualFile(), files);
            }
          }
        }
      }
    }
    InspectionValidatorUtil.expandCompileScopeIfNeeded(files, context);
    return files;
  }

}
