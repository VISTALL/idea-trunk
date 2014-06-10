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

import com.intellij.uml.AbstractUmlNodeContentManager;
import com.intellij.uml.UmlCategory;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlCategoryManager extends AbstractUmlNodeContentManager {
  public UmlCategory[] getContentCategories() {
    return UmlCategory.EMPTY_ARRAY;
  }

  public boolean isInCategory(Object element, UmlCategory category) {
    return false;
  }
}
