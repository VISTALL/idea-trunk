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

package com.intellij.beanValidation.providers;

import com.intellij.beanValidation.utils.BVUtils;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationGotoSymbolProvider extends GoToSymbolProvider {
  protected void addNames(@NotNull final Module module, final Set<String> result) {
  }

  protected void addItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
  }

  protected boolean acceptModule(final Module module) {
    return BVUtils.isModuleContainsBeanValidationFacet(module);
  }
}

