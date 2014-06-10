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
package org.jetbrains.plugins.groovy.grails;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.codeInspection.untypedUnresolvedAccess.GroovyUnresolvedAccessInspection
import com.intellij.testFramework.PsiTestUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.application.ApplicationManager;

/**
 * @author peter
 */
public class GrailsHighlightingTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/highlighting/";
  }

  public void testFileReferenceWithGspInjections() throws Throwable {
    doTest();
  }

  private void doTest() throws Throwable {
    myFixture.testHighlighting(true, false, false, getTestName(false) + ".gsp");
  }

  public void testJavascriptWithGspInjections() throws Throwable {
    doTest();
  }

  public void testFinderMethods() throws Throwable {
    myFixture.enableInspections(new GroovyUnresolvedAccessInspection());
    final VirtualFile file = myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/MyDomain.groovy");
    myFixture.testHighlighting(true, false, false, file);
  }

  public void testProperties() throws Throwable {
    myFixture.enableInspections(new GroovyUnresolvedAccessInspection());
    final VirtualFile file = myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/MyDomain.groovy");
    myFixture.testHighlighting(true, false, false, file);
  }

  public void testJspTagInGsp() throws Throwable {
    myFixture.copyFileToProject("../fmt.tld", "WEB-INF/tld/fmt.tld");
    doTest();
  }

  public void testDomainFindersWithInheritedProperties() throws Throwable {
    final VirtualFile file = myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/Domains.groovy");
    myFixture.testHighlighting(true, false, false, file);
  }

  public void testCustomTaglibWithPrivateField() throws Throwable {
    def taglib = myFixture.addFileToProject("grails-app/taglib/MyTagLib.groovy", """class MyTagLib {
  static namespace = "my"
  private def foo = {}
  def getFoo() {}
}
""")
    ApplicationManager.application.runWriteAction ({ PsiTestUtil.addSourceRoot myFixture.module, taglib.virtualFile.parent } as Runnable)
    def gsp = myFixture.addFileToProject("grails-app/views/error.gsp", "<my:foo/>")
    myFixture.testHighlighting(true, false, false, gsp.virtualFile)
  }
}