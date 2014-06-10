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

package com.intellij.uml.core.presentation;

import com.intellij.uml.settings.UmlPresentationSettings;
import com.intellij.uml.settings.UmlLayout;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPresentationModelImpl implements UmlPresentationSettings {
  public boolean isCamel() {
    return false;
  }

  public void setCamel(boolean camel) {
  }

  public boolean isColorManagerEnabled() {
    return true;
  }

  public void setColorManagerEnabled(boolean enabled) {
  }

  public boolean isShowDependencies() {
    return false;
  }

  public void setShowDependencies(boolean show) {
  }

  public boolean isEdgeCreationMode() {
    return false;
  }

  public void setEdgeCreationMode(boolean enable) {
  }

  public void setVcsFilterEnabled(boolean enabled) {
  }

  public boolean isVcsFilterEnabled() {
    return false;
  }

  public UmlLayout getLayout() {
    return UmlLayout.HIERARCHIC_GROUP;
  }

  public void setLayout(UmlLayout layout) {
  }

  public boolean isFitContentAfterLayout() {
    return false;
  }

  public void setFitContentAfterLayout(boolean enabled) {
  }
}
