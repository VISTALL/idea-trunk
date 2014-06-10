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

import com.intellij.uml.UmlNodeBase;
import com.intellij.uml.UmlProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlNode extends UmlNodeBase<ModuleItem> {
  private final ModuleItem myItem;

  public ModulesUmlNode(final ModuleItem item, @NotNull UmlProvider<ModuleItem> provider) {
    super(provider);
    myItem = item;
  }

  public String getName() {
    return myItem.getName();
  }

  public Icon getIcon() {
    return myItem.getIcon();
  }

  @NotNull
  public ModuleItem getIdentifyingElement() {
    return myItem;
  }
}
