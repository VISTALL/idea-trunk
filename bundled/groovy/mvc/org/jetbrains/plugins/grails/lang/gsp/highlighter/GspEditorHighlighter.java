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

package org.jetbrains.plugins.grails.lang.gsp.highlighter;

import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.highlighter.GroovySyntaxHighlighter;

/**
 * @author ilyas
 */
public class GspEditorHighlighter extends LayeredLexerEditorHighlighter {
  private final Project myProject;
  private final VirtualFile myVirtualFile;
  private Language myTemplateLanguage;

  public GspEditorHighlighter(EditorColorsScheme scheme, Project project, VirtualFile virtualFile) {
    super(new GspSyntaxHiglighter(), scheme);

    myProject = project;
    myVirtualFile = virtualFile;

    // Register Groovy Highlighter
    SyntaxHighlighter groovyHighlighter = new GroovySyntaxHighlighter();
    final LayerDescriptor groovyLayer = new LayerDescriptor(groovyHighlighter, "\n", JspHighlighterColors.JSP_SCRIPTING_BACKGROUND);
    registerLayer(GspTokenTypesEx.GROOVY_CODE, groovyLayer);
    registerLayer(GspTokenTypesEx.GROOVY_EXPR_CODE, groovyLayer);
    registerLayer(GspTokenTypesEx.GSP_MAP_ATTR_VALUE, groovyLayer);
    registerLayer(GspTokenTypesEx.GROOVY_DECLARATION, groovyLayer);

    // Register html highlighter
    SyntaxHighlighter htmlHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(StdLanguages.HTML, project, virtualFile);
    final LayerDescriptor htmlLayer = new LayerDescriptor(htmlHighlighter, "\n", XmlHighlighterColors.HTML_TAG);
    registerLayer(GspTokenTypesEx.GSP_TEMPLATE_DATA, htmlLayer);


    final SyntaxHighlighter directiveHighlighter = new GspDirectiveHighlighter();
    final LayerDescriptor directiveLayer = new LayerDescriptor(directiveHighlighter, "\n", JspHighlighterColors.JSP_ACTION_AND_DIRECTIVE_BACKGROUND);
    registerLayer(GspElementTypes.GSP_DIRECTIVE, directiveLayer);

  }

  protected boolean updateLayers() {
    Language templateLanguage = getCurrentTemplateLanguage();
    if (!Comparing.equal(myTemplateLanguage, templateLanguage)) {
      unregisterLayer(GspTokenTypesEx.GSP_TEMPLATE_DATA);
      myTemplateLanguage = templateLanguage;

      final SyntaxHighlighter templateLanguageHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(myTemplateLanguage, myProject, myVirtualFile);
      registerLayer(GspTokenTypesEx.GSP_TEMPLATE_DATA, new LayerDescriptor(templateLanguageHighlighter, "", null));
      return true;
    }

    return false;
  }

  private Language getCurrentTemplateLanguage() {
    if (myProject != null && !myProject.isDisposed() && getDocument() != null) {
      final PsiDocumentManager instance = PsiDocumentManager.getInstance(myProject);
      final PsiFile psiFile = instance.getPsiFile(getDocument());
      if (GspPsiUtil.isInGspFile(psiFile)) {
        final GspFile gspFile = GspPsiUtil.getGspFile(psiFile);
        assert gspFile != null;
        return StdLanguages.HTML;
      }
    }

    return StdLanguages.HTML;

  }


}
