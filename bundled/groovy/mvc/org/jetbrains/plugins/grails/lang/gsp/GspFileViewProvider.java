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

package org.jetbrains.plugins.grails.lang.gsp;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.util.ReflectionCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl.GspGroovyFileImpl;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.impl.GspHtmlFileImpl;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ilyas
 */
public class GspFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements TemplateLanguageFileViewProvider{
  private Set<Language> myViews = null;

  public GspFileViewProvider(PsiManager manager,
                             VirtualFile virtualFile,
                             boolean physical) {
    super(manager, virtualFile, physical);
  }

  @NotNull
  public Language getBaseLanguage() {
    return GspFileType.GSP_FILE_TYPE.getLanguage();
  }

  @NotNull
  @Override
  public Set<Language> getLanguages() {
    HashSet<Language> set = new HashSet<Language>();
    set.add(StdLanguages.HTML);
    set.add(getBaseLanguage());
    set.add(GroovyFileType.GROOVY_LANGUAGE);
    return set;
  }

  @NotNull
  public Language getTemplateDataLanguage() {
    return StdLanguages.HTML;
  }

  @NotNull
  public Set<Language> getRelevantLanguages() {
    if (myViews != null) return myViews;
    Set<Language> views = new HashSet<Language>();
    views.add(GspFileType.GSP_FILE_TYPE.getLanguage());
    views.add(GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
    views.add(StdLanguages.HTML);
    return myViews = views;
  }

  protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(final VirtualFile copy) {
    return new GspFileViewProvider(getManager(), copy, false);
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return super.getVirtualFile();
  }

  protected PsiFile createFile(Language language) {
    if (language == getBaseLanguage()) {
      ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
      assert parserDefinition != null;
      return parserDefinition.createFile(this);
    }
    if (language.equals(GroovyFileType.GROOVY_FILE_TYPE.getLanguage())) {
      return new GspGroovyFileImpl(this);
    }
    if (language.equals(StdLanguages.HTML)) {
      return new GspHtmlFileImpl(this);
    }
    return super.createFile(language);
  }

  protected PsiFile getPsiInner(final Language target) {
    return super.getPsiInner(target);
  }

  public PsiElement findElementAt(int offset, Class<? extends Language> lang) {
    PsiElement ret = null;
    PsiFile mainRoot = getPsi(getBaseLanguage());
    PsiElement elementInBaseRoot = findElementByLanguage(offset, lang, ret, getBaseLanguage());
    if (isMeaningfulElement(elementInBaseRoot)) {
      return elementInBaseRoot;
    }
    for (final Language language : getRelevantLanguages()) {
      PsiElement found = findElementByLanguage(offset, lang, ret, language);
      if (ret == null || getPsi(language) != mainRoot) {
        ret = found;
      }
    }
    return ret;
  }

  private PsiElement findElementByLanguage(int offset, Class<? extends Language> lang, PsiElement ret, Language language) {
    if (!ReflectionCache.isAssignable(lang, language.getClass())) return ret;
    if (lang.equals(Language.class) && !getRelevantLanguages().contains(language)) return ret;
    final PsiFile psiRoot = getPsi(language);
    final PsiElement psiElement = findElementAt(psiRoot, offset);
    if (psiElement == null || (psiElement instanceof OuterLanguageElement && !isMeaningfulElement(psiElement)))
      return ret;
    if (ret == null) {
      ret = psiElement;
    }
    return ret;
  }

  private static boolean isMeaningfulElement(PsiElement elementInBaseRoot) {
    return GspPsiUtil.isJSInjection(elementInBaseRoot);
  }
}
