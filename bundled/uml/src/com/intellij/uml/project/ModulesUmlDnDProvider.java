/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.uml.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.uml.UmlDnDProvider;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlDnDProvider implements UmlDnDProvider<ModuleItem> {
  public boolean isAcceptedForDnD(Object o, Project project) {
    return o instanceof Module || o instanceof Library;
  }

  public ModuleItem wrapToModelObject(Object o, Project project) {
    if (o instanceof Library) {
      return new ModuleItem((Library)o, project);
    } else if (o instanceof Module) {
      return new ModuleItem((Module)o);
    }
    return null;
  }
}
