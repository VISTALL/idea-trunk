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

package com.intellij.uml;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlFileType implements FileType {
  public static final String EXTENSION = "uml";

  public static final UmlFileType INSTANCE = new UmlFileType();

  private UmlFileType() {}

  @NotNull
  public String getName() {
    return UmlBundle.message("UML");
  }

  @NotNull
  public String getDescription() {
    return UmlBundle.message("uml.class.diagrams");
  }

  @NotNull
  public String getDefaultExtension() {
    return EXTENSION;
  }

  public Icon getIcon() {
    return UmlIcons.UML_ICON;
  }

  public boolean isBinary() {
    return true;
  }

  public boolean isReadOnly() {
    return false;
  }

  public String getCharset(@NotNull VirtualFile file, byte[] content) {
    return CharsetToolkit.UTF8;
  }
}

