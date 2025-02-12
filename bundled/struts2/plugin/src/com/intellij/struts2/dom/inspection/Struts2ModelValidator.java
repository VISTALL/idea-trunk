/*
 * Copyright 2007 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.dom.inspection;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.util.InspectionValidatorUtil;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FactoryMap;
import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

/**
 * Validator for struts.xml files when running "Make Project".
 *
 * @author Yann C&eacute;bron
 */
public class Struts2ModelValidator extends ValidatorBase {

  public Struts2ModelValidator() {
    super(StrutsBundle.message("inspections.struts2.model.validator"),
          StrutsBundle.message("inspections.struts2.model.validator.progress"), Struts2ModelInspection.class);
  }

  public Collection<VirtualFile> getFilesToProcess(final Project project, final CompileContext context) {
    final StrutsManager strutsManager = StrutsManager.getInstance(project);
    final PsiManager psiManager = PsiManager.getInstance(project);

    // cache validation settings per module
    final FactoryMap<Module, Boolean> enabledForModule = new FactoryMap<Module, Boolean>() {
      protected Boolean create(final Module module) {
        return isEnabledForModule(module);
      }
    };

    final Set<VirtualFile> files = new THashSet<VirtualFile>();
    for (final VirtualFile file : context.getCompileScope().getFiles(StdFileTypes.XML, false)) {
      final Module module = context.getModuleByFile(file);
      if (module != null &&
          enabledForModule.get(module)) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile instanceof XmlFile) {
          final StrutsModel model = strutsManager.getModelByFile((XmlFile) psiFile);
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