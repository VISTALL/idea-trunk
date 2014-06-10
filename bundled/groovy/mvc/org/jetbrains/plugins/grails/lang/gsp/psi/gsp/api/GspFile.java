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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.GspDirectiveKind;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;

/**
 * @author ilyas
 */
public interface GspFile extends XmlFile {

  GroovyFileBase getGroovyLanguageRoot();

  @NotNull
  String[] getImportedClassesNames();

  @NotNull
  String[] getImportedPackagesNames();

  GspDirective[] getDirectiveTags(GspDirectiveKind directiveKind, boolean searchInIncludes);

  void addImportForClass(PsiClass aClass) throws IncorrectOperationException;

  void addImportStatement(GrImportStatement statement);

  PsiElement createGroovyScriptletFromText(String text) throws IncorrectOperationException;

  GspXmlRootTag getRootTag();

  @NotNull
  FileViewProvider getViewProvider();

}
