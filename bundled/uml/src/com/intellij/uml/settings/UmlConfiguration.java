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

package com.intellij.uml.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Konstantin Bulenkov
 */
@State(name = "UmlConfiguration", storages = {@Storage(id = "uml", file = "$APP_CONFIG$/uml.xml")})
public class UmlConfiguration implements PersistentStateComponent<UmlConfiguration> {
  public boolean showFields = false;
  public boolean showConstructors = false;
  public boolean showMethods = false;
  public boolean showProperties = false;
  public boolean showInnerClasses = false;
  public boolean showChanges = false;
  public boolean showCamelNames = false;
  public boolean showDependencies = false;
  public boolean fitContentAfterLayout = false;
  public boolean showColors = true;
  public VisibilityLevel visibilityLevel = VisibilityLevel.PRIVATE;
  public UmlLayout layout = UmlLayout.HIERARCHIC_GROUP;

  public UmlConfiguration getState() {
    return this;
  }

  public void loadState(UmlConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
