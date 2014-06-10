/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
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
package com.intellij.j2meplugin.module;

import com.intellij.j2meplugin.module.settings.ui.J2MEModuleConfEditor;
import com.intellij.j2meplugin.module.settings.ui.MobileBuildSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

/**
 * User: anna
 * Date: Aug 16, 2004
 */
public class J2MEModuleEditorsProvider implements ModuleConfigurationEditorProvider {


  public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
    final ModifiableRootModel rootModel = state.getRootModel();
    final Module module = rootModel.getModule();
    if (module.getModuleType() != J2MEModuleType.getInstance()) return ModuleConfigurationEditor.EMPTY;

    final Project project = state.getProject();
    final DefaultModuleConfigurationEditorFactory editorFactory = DefaultModuleConfigurationEditorFactory.getInstance();

    return new ModuleConfigurationEditor[]{
      editorFactory.createModuleContentRootsEditor(state),
      editorFactory.createOutputEditor(state),
      editorFactory.createClasspathEditor(state),
      new J2MEModuleConfEditor(module, project),
      new MobileBuildSettings(module, rootModel),
    };
  }

}
