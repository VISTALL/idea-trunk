/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.fileType;

import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.lang.gsp.GspLanguage;
import org.jetbrains.plugins.grails.lang.gsp.highlighter.GspEditorHighlighter;

import javax.swing.*;

public class GspFileType extends XmlLikeFileType {

  public static final String GSP_EXTENSION = "gsp";
  public static final GspFileType GSP_FILE_TYPE = new GspFileType();
  public static final Icon GSP_LOGO = GrailsIcons.GSP_FILE_TYPE;


  private GspFileType() {
    super(GspLanguage.INSTANCE);
  }

  @NotNull
  public String getComponentName() {
    return "GspFileType";
  }

  @NotNull
  public String getDefaultExtension() {
    return GSP_EXTENSION;
  }

  @NotNull
  public String getDescription() {
    return "Groovy Server Pages";
  }

  public Icon getIcon() {
    return GSP_LOGO;
  }

  @NotNull
  public String getName() {
    return "GSP";
  }

  public boolean isJVMDebuggingSupported() {
    return true;
  }

  public EditorHighlighter getEditorHighlighter(@Nullable final Project project,
                                                @Nullable final VirtualFile virtualFile,
                                                @NotNull final EditorColorsScheme colors) {
//    return super.getEditorHighlighter(myProject, virtualFile, colors);
    return new GspEditorHighlighter(colors, project, virtualFile);
  }


}
