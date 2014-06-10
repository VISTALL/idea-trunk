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

package com.intellij.beanValidation.highlighting;

import com.intellij.beanValidation.model.xml.BvConfigDomElement;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class BvConfigDomInspection extends BasicDomElementsInspection<BvConfigDomElement> {
  public BvConfigDomInspection() {
    super(BvConfigDomElement.class);
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return BVInspectionBundle.message("model.inspection.group.name");
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return BVBundle.message("constraints.config.inspection");
  }

  @NotNull
  @Override
  public String getShortName() {
    return getClass().getSimpleName();
  }

}
