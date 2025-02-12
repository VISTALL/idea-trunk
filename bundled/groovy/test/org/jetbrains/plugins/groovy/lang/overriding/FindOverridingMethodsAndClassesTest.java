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

package org.jetbrains.plugins.groovy.lang.overriding;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;

import java.util.Collection;

/**
 * @author Maxim.Medvedev
 */
public class FindOverridingMethodsAndClassesTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testData/overriding/findOverridingMethodsAndClasses";
  }

  public void testSimpleCase() throws Throwable {
    doTest(1, 1);
  }

  public void testAnonymousClass() throws Throwable {
    doTest(2, 2);
  }

  private void doTest(int methodCount, int classCount) throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".groovy");
    final PsiFile file = myFixture.getFile();
    assert file instanceof GroovyFile;
    final GroovyFile groovyFile = (GroovyFile)file;
    final PsiClass psiClass = groovyFile.getClasses()[0];
    final PsiMethod method = psiClass.getMethods()[0];

    final Collection<PsiMethod> methods = OverridingMethodsSearch.search(method, psiClass.getResolveScope(), true).findAll();
    assertEquals("Method count is wrong", methodCount, methods.size());

    final Collection<PsiClass> classes = DirectClassInheritorsSearch.search(psiClass).findAll();
    assertEquals("Class count is wrong", classCount, classes.size());
  }
}
