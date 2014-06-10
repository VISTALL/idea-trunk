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

package org.jetbrains.plugins.grails.lang.gsp.psi.html.impl;

import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.html.ScriptSupportUtil;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.xml.XmlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.GspFileViewProvider;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.api.GspLikeFile;

/**
 * @author ilyas
 */
public class GspHtmlFileImpl extends XmlFileImpl implements GspTokenTypesEx, GspLikeFile {

  public GspHtmlFileImpl(FileViewProvider fileViewProvider) {
    super(fileViewProvider, GSP_HTML_TEMPLATE_ROOT);
  }

  public XmlDocument getDocument() {
    return findChildByClass(XmlDocument.class);
  }

  public GspFile getGspLanguageRoot() {
    PsiFile psiFile = getViewProvider().getPsi(GspFileType.GSP_FILE_TYPE.getLanguage());
    assert psiFile instanceof GspFile;
    return ((GspFile) psiFile);
  }

  @NotNull
  public FileType getFileType() {
    return getViewProvider().getVirtualFile().getFileType();
  }

  @NotNull
  public Language getLanguage() {
    return getViewProvider().getTemplateDataLanguage();
  }

  @NotNull
  public GspFileViewProvider getViewProvider() {
    return (GspFileViewProvider) super.getViewProvider();
  }

  public String toString() {
    return "Gsp Html File";
  }

  protected boolean isPsiUpToDate(VirtualFile vFile) {
    final FileViewProvider viewProvider = myManager.findViewProvider(vFile);
    return viewProvider != null && viewProvider.getPsi(StdLanguages.HTML) == this;

  }

  @NotNull
  public PsiFile getOriginalFile() {
    final PsiFile original = super.getOriginalFile();
    if (original == this) {
      GspFile gspFile = GspPsiUtil.getGspFile(this);
      if (gspFile != null) {
        final PsiFile gspOriginal = gspFile.getOriginalFile();
        return gspOriginal.getViewProvider().getPsi(getLanguage());
      }
    }
    return original;
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
    if (!super.processDeclarations(processor, state, lastParent, place)) return false;

    ScriptSupportUtil.clearCaches(this);
    return ScriptSupportUtil.processDeclarations(this, processor, state, lastParent, place);
  }

  @Override
  public boolean isTemplateDataFile() {
    return true;
  }
}
