/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.j2meplugin.emulator.ui;

import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: anna
 * Date: Dec 5, 2004
 */
public abstract class MobileApiSettingsEditor extends SettingsEditor<Emulator> {
  protected boolean myModified = false;

  @NotNull
  public abstract JComponent createEditor();

  public abstract void resetEditorFrom(Emulator s);

  public abstract void applyEditorTo(Emulator s) throws ConfigurationException;


  public boolean isModified() {
    return myModified;
  }

}
