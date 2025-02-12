/*
 *  Copyright 2000-2007 JetBrains s.r.o.
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
 *
 */

package org.jetbrains.plugins.groovy.lang.resolve;

import com.intellij.psi.JavaResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaReference;
import com.intellij.psi.PsiReference;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GroovyScriptMethod;

/**
 * @author ven
 */
public class JavaToGroovyResolveTest extends GroovyResolveTestCase {
  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testdata/resolve/javaToGroovy/";
  }

  public void testField1() throws Exception {
    PsiReference ref = configureByFile("field1/A.java");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrField);
  }

  public void testAccessorRefToProperty() throws Exception {
    PsiReference ref = configureByFile("accessorRefToProperty/A.java");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrAccessorMethod);
  }

  public void testMethod1() throws Exception {
    PsiJavaReference ref = (PsiJavaReference) configureByFile("method1/A.java");
    JavaResolveResult resolveResult = ref.advancedResolve(false);
    assertTrue(resolveResult.getElement() instanceof GrMethod);
    assertTrue(resolveResult.isValidResult());
  }

  public void _testScriptMain() throws Exception {
    PsiJavaReference ref = (PsiJavaReference) configureByFile("scriptMain/A.java");
    JavaResolveResult resolveResult = ref.advancedResolve(false);
    assertTrue(resolveResult.getElement() instanceof GroovyScriptMethod);
    assertTrue(resolveResult.isValidResult());
  }

  public void _testScriptMethod() throws Exception {
    PsiJavaReference ref = (PsiJavaReference) configureByFile("scriptMethod/A.java");
    JavaResolveResult resolveResult = ref.advancedResolve(false);
    assertTrue(resolveResult.getElement() instanceof GrMethod);
    assertTrue(resolveResult.isValidResult());
  }

  public void testNoDGM() throws Exception {
    PsiJavaReference ref = (PsiJavaReference) configureByFile("noDGM/A.java");
    assertNull(ref.advancedResolve(false).getElement());
  }

}