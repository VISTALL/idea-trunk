/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.inplace.gutter;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author davdeev
 */
public class GutterIconRendererBase extends GutterIconRenderer {

  @NotNull
  private final Icon myIcon;

  protected GutterIconRendererBase(@NotNull final Icon icon) {
    myIcon = icon;
  }

  /**
   * Returns the icon drawn in the gutter.
   *
   * @return the gutter icon.
   */
  @NotNull
  public Icon getIcon() {
    return myIcon;
  }

}