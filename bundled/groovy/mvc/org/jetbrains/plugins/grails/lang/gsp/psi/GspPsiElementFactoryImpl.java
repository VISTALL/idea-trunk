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

package org.jetbrains.plugins.grails.lang.gsp.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.GspDirectiveKind;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterHtmlElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspScriptletTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirectiveAttribute;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;

/**
 * @author ilyas
 */
public class GspPsiElementFactoryImpl extends GspPsiElementFactory {

  Project myProject;

  public GspPsiElementFactoryImpl(Project project) {
    myProject = project;
  }

  public GspDirective createDirectiveByKind(GspDirectiveKind kind) {
    String kindName = kind.toString().toLowerCase();
    String text = "<%@ " + kindName + " %>";
    PsiFile psiFile = createDummyFile(text);
    assert psiFile instanceof GspFile;
    GspXmlRootTag rootTag = ((GspFile) psiFile).getRootTag();
    assert rootTag != null;
    assert rootTag.getFirstChild() instanceof GspDirective;
    return (GspDirective) rootTag.getFirstChild();
  }

  public GspDirectiveAttribute createDirectiveAttribute(@NotNull String name, @NotNull String value) {
    String text = "<%@ " + "page " + name + "=" + "\"" + value + "\" %>";
    PsiFile psiFile = createDummyFile(text);
    assert psiFile instanceof GspFile;
    GspXmlRootTag rootTag = ((GspFile) psiFile).getRootTag();
    assert rootTag != null;
    assert rootTag.getFirstChild() instanceof GspDirective;
    GspDirective gspDirective = (GspDirective) rootTag.getFirstChild();
    assert gspDirective != null;
    return ((GspDirectiveAttribute) gspDirective.getAttribute(name));
  }

  public GspScriptletTag createScriptletTagFromText(String s) {
    String text = "<%" + s + "%>";
    PsiFile psiFile = createDummyFile(text);
    assert psiFile instanceof GspFile;
    GspXmlRootTag rootTag = ((GspFile) psiFile).getRootTag();
    assert rootTag != null;
    assert rootTag.getFirstChild() instanceof GspScriptletTag;
    return (GspScriptletTag) rootTag.getFirstChild();
  }

  public GspOuterHtmlElement createOuterHtmlElement(String text) {
    PsiFile psiFile = createDummyFile(text);
    assert psiFile instanceof GspFile;
    GspXmlRootTag rootTag = ((GspFile) psiFile).getRootTag();
    assert rootTag != null;
    PsiElement child = rootTag.getFirstChild();
    assert child instanceof GspOuterHtmlElement;
    return (GspOuterHtmlElement) child;

  }

  private GspFile createDummyFile(String s) {
    return (GspFile) PsiFileFactory.getInstance(myProject).createFileFromText("DUMMY__." + GspFileType.GSP_FILE_TYPE.getDefaultExtension(), s);
  }


}
