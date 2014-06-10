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

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import com.intellij.psi.*;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspGroovyElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspGroovyFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrTopLevelDefintion;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileBaseImpl;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

/**
 * @author ilyas
 */
public class GspGroovyFileImpl extends GroovyFileBaseImpl implements GspGroovyFile {
  public GrStatement addStatementBefore(@NotNull GrStatement statement, @NotNull GrStatement anchor) throws IncorrectOperationException {
    return super.addStatementBefore(statement, anchor);
  }

  public GspGroovyFileImpl(final FileViewProvider viewProvider) {
    super(GspGroovyElementTypes.GSP_GROOVY_DECLARATIONS_ROOT, GspGroovyElementTypes.GSP_GROOVY_DECLARATIONS_ROOT, viewProvider);
  }

  public String toString() {
    return "GspGroovyDummyHolder";
  }

  public GspFile getGspLanguageRoot() {
    PsiFile psiFile = getViewProvider().getPsi(GspFileType.GSP_FILE_TYPE.getLanguage());
    assert psiFile instanceof GspFile;
    return ((GspFile) psiFile);
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
    JavaPsiFacade facade = JavaPsiFacade.getInstance(getProject());
    if (!(lastParent instanceof GrTypeDefinition)) {
      //if (!ResolveUtil.processElement(processor, getSyntheticArgsParameter())) return false;
      GlobalSearchScope resolveScope = getResolveScope();
      PsiClass scriptClass = getScriptClass();
      if (scriptClass != null && !scriptClass.processDeclarations(processor, state, lastParent, place))
        return false;
    }

    GspFile gspFile = getGspLanguageRoot();
    for (final String importedClassName : gspFile.getImportedClassesNames()) {
      if (importedClassName.matches("(\\w+\\.)*\\w+\\s+as\\s+\\w+")) {
        NameHint nameHint = processor.getHint(NameHint.KEY);
        //todo [DIANA] look more carefully
        String name = nameHint == null ? null : nameHint.getName(ResolveState.initial());
        String[] parts = importedClassName.split("\\s+");
        if (name != null && parts.length == 3 && name.equals(parts[2])) {
          PsiClass clazz = facade.findClass(parts[0], getResolveScope());
          if (clazz != null && !processor.execute(clazz, state)) return false;
        }
      }
      PsiClass clazz = facade.findClass(importedClassName, getResolveScope());
      if (clazz != null && !ResolveUtil.processElement(processor, clazz)) return false;
    }

    for (final String implicitlyImported : gspFile.getImportedPackagesNames()) {
      PsiPackage aPackage = facade.findPackage(implicitlyImported);
      if (aPackage != null && !aPackage.processDeclarations(processor, state, lastParent, place)) return false;
    }

    for (final String implicitlyImported : IMPLICITLY_IMPORTED_PACKAGES) {
      PsiPackage aPackage = facade.findPackage(implicitlyImported);
      if (aPackage != null && !aPackage.processDeclarations(processor, state, lastParent, place)) return false;
    }

    for (String implicitlyImportedClass : IMPLICITLY_IMPORTED_CLASSES) {
      PsiClass clazz = facade.findClass(implicitlyImportedClass, getResolveScope());
      if (clazz != null && !ResolveUtil.processElement(processor, clazz)) return false;
    }

    String currentPackageName = "";
    PsiPackage currentPackage = facade.findPackage(currentPackageName);
    if (currentPackage != null && !currentPackage.processDeclarations(processor, state, lastParent, place))
      return false;


    return true;
  }


  public GrImportStatement addImportForClass(PsiClass aClass) throws IncorrectOperationException {
    getGspLanguageRoot().addImportForClass(aClass);
    return null;
  }

  public GrImportStatement addImport(GrImportStatement statement) throws IncorrectOperationException {
    //todo implement me!
    return null;
  }

  // GSP page is ALWAYS Groovy script
  public boolean isScript() {
    return true;
  }

  @Nullable
  public PsiClass getScriptClass() {
    return (PsiClass) getFirstChild();
  }

  public GrTypeDefinition[] getTypeDefinitions() {
    //todo implement me!
    return GrTypeDefinition.EMPTY_ARRAY;
  }

  public GrTopLevelDefintion[] getTopLevelDefinitions() {
    //todo implement me!
    return GrTopLevelDefintion.EMPTY_ARRAY;
  }

  public GrTopStatement[] getTopStatements() {
    //todo implement me!
    return GrTopStatement.EMPTY_ARRAY;

  }

  public void removeImport(GrImportStatement importStatement) throws IncorrectOperationException {
    //todo implement me!
  }

  public String getPackageName() {
    return "";
  }

  public void setPackageName(String s) throws IncorrectOperationException {
    throw new IncorrectOperationException("Cannot set package name for gsp files");
  }
}
