/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.highlighter;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.plugins.groovy.lang.groovydoc.highlighter.GroovyDocSyntaxHighlighter;
import org.jetbrains.plugins.groovy.lang.groovydoc.parser.GroovyDocElementTypes;

/**
 * @author ilyas
 */
public class GroovyEditorHighlighter extends LayeredLexerEditorHighlighter {

  public GroovyEditorHighlighter(EditorColorsScheme scheme, Project project, VirtualFile virtualFile) {
    super(new GroovySyntaxHighlighter(), scheme);
    registerGroovydocHighlighter();
  }

  private void registerGroovydocHighlighter() {
    // Register GroovyDoc Highlighter
    SyntaxHighlighter groovyDocHighlighter = new GroovyDocSyntaxHighlighter();
    final LayerDescriptor groovyDocLayer = new LayerDescriptor(groovyDocHighlighter, "\n", DefaultHighlighter.DOC_COMMENT_CONTENT);
    registerLayer(GroovyDocElementTypes.GROOVY_DOC_COMMENT, groovyDocLayer);
  }

}