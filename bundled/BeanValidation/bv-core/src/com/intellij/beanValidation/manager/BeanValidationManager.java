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

package com.intellij.beanValidation.manager;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationManager {
  private final Module myModule;

  public BeanValidationManager(@NotNull Module module) {
    myModule = module;
  }

  @NotNull
  public static BeanValidationManager getService(@NotNull Module module) {
    synchronized (module) {
      return ModuleServiceManager.getService(module, BeanValidationManager.class);
    }
  }


  public Module getModule() {
    return myModule;
  }
}
