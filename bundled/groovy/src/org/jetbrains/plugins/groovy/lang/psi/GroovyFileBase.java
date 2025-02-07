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

package org.jetbrains.plugins.groovy.lang.psi;

import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrVariableDeclarationOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrStatementOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrDeclarationHolder;

/**
 * @author ilyas
 */
public interface GroovyFileBase extends PsiFile, GrVariableDeclarationOwner, GrStatementOwner, PsiClassOwner, GrControlFlowOwner, PsiImportHolder,
                                        GrDeclarationHolder {
  String SCRIPT_BASE_CLASS_NAME = "groovy.lang.Script";
  String[] IMPLICITLY_IMPORTED_PACKAGES = {
      "java.lang",
      "java.util",
      "java.io",
      "java.net",
      "groovy.lang",
      "groovy.util",
  };
  String[] IMPLICITLY_IMPORTED_CLASSES = {
      "java.math.BigInteger",
      "java.math.BigDecimal",
  };

  GrTypeDefinition[] getTypeDefinitions();

  GrTopLevelDefintion[] getTopLevelDefinitions();

  GrMethod[] getTopLevelMethods();

  GrVariableDeclaration[] getTopLevelVariableDeclarations();

  GrTopStatement[] getTopStatements();

  GrImportStatement addImportForClass(PsiClass aClass) throws IncorrectOperationException;

  void removeImport(GrImportStatement importStatement) throws IncorrectOperationException;

  GrImportStatement addImport(GrImportStatement statement) throws IncorrectOperationException;

  boolean isScript();

  @Nullable
  PsiClass getScriptClass();
}
