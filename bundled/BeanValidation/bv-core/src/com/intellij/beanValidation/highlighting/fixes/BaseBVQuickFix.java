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

package com.intellij.beanValidation.highlighting.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.util.Iconable;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.BVIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public abstract class BaseBVQuickFix implements LocalQuickFix, Iconable {
  private final String myName;

  public BaseBVQuickFix(final String name) {
    myName = name;
  }

  @NotNull
  public String getName() {    
    return myName;
  }

  @NotNull
  public String getFamilyName() {
    return BVBundle.message("bv.framework.name");
  }

  public Icon getIcon(int flags) {
    return BVIcons.BEAN_VALIDATION_ICON;
  }
}
