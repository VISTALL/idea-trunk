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

package com.intellij.beanValidation.toolWindow;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.beanValidation.utils.BVUtils;

/**
 * @author Konstantin Bulenkov
 */
public class BVToolWindowCondition implements Condition<Project> {
  public boolean value(Project project) {
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (BVUtils.isModuleContainsBeanValidationFacet(module)) {
        return true;
      }
    }
    return false;
  }
}
