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

package com.intellij.uml.presentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.uml.settings.UmlLayout;

/**
 * @author Konstantin Bulenkov
 */
public interface UmlDiagramPresentation {
  boolean isCamel();
  void setCamel(boolean camel);

  boolean isFieldsVisible();
  void setFieldsVisible(boolean visible);

  boolean isConstructorsVisible();
  void setConstructorVisible(boolean visible);

  boolean isMethodsVisible();
  void setMethodsVisible(boolean visible);

  void setHighlightedPackage(String packageName);
  @Nullable String getHighlightedPackage();

  boolean isColorManagerEnabled();
  void setColorManagerEnabled(boolean enabled);

  boolean isShowDependencies();
  void setShowDependencies(boolean show);

  boolean isShowInnerClasses();
  void setShowInnerClasses(boolean visible);

  boolean isEdgeCreationMode();
  void setEdgeCreationMode(boolean enable);

  void setVisibilityLevel(@NotNull VisibilityLevel level);
  @NotNull VisibilityLevel getVisibilityLevel();

  void setPropertiesVisible(boolean visible);
  boolean isPropertiesVisible();

  void setVcsFilterEnabled(boolean enabled);
  boolean isVcsFilterEnabled();

  UmlLayout getLayout();
  void setLayout(UmlLayout layout);

  boolean isFitContentAfterLayout();
  void setFitContentAfterLayout(boolean enabled);
}
