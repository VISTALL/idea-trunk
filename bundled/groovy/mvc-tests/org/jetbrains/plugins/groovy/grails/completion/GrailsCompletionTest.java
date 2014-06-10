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
package org.jetbrains.plugins.groovy.grails.completion;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.spring.web.SpringJavaeeApplicationComponent;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.containers.CollectionFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author peter
 */
public class GrailsCompletionTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/completion/";
  }

  public void testFinderMethodsInDomainClass() throws Throwable {
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    myFixture.testCompletionVariants(path, "findByAge", "findByFamily", "findById", "findByName", "findByVersion");
  }

  public void testAllFinderMethodsInDomainClass() throws Throwable {
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    myFixture.testCompletionVariants(path, "findAllByAge", "findAllByFamily", "findAllById", "findAllByName", "findAllByVersion");
  }

  public void testFindByMethodInDomainClass() throws Throwable {
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    myFixture.testCompletionVariants(path, "findByNameAndAge", "findByNameAndId", "findByNameAndVersion");
  }

  public void testFindByMethodWithThreeArgsInDomainClass() throws Throwable {
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    myFixture.testCompletionVariants(path);
    myFixture.checkResultByFile(getTestName(false) + ".groovy");
  }

  public void testFinderMethodsInGsp() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/Foo.groovy");
    myFixture.testCompletionVariants(getTestName(false) + ".gsp", "findByAge", "findById", "findByName", "findByVersion");
  }

  public void testUnpackagedDomainClassInGsp() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/FooBar.groovy");
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        PsiTestUtil.addSourceRoot(myModule, myFixture.getTempDirFixture().getFile("grails-app/domain"));
      }
    }.execute();
    myFixture.testCompletion(getTestName(false) + ".gsp", getTestName(false) + "_after.gsp");
  }

  public void testGspInjectedJavaScript() throws Throwable {
    myFixture.testCompletion(getTestName(false) + ".gsp", getTestName(false) + "_after.gsp");
  }

  public void testAttributeNameFinishWithEq() throws Throwable {
    myFixture.addFileToProject("grails-app/x.txt", "just to make this a Grails module");
    myFixture.copyFileToProject("grails.tld", "web-app/WEB-INF/tld/grails.tld");
    myFixture.configureByFile(getTestName(false) + ".gsp");
    assertEquals(3, myFixture.completeBasic().length);
    myFixture.type('=');
    myFixture.checkResultByFile(getTestName(false) + "_after.gsp");
  }

  public void testServiceInjection() throws Throwable {
    final PsiFile file =
      myFixture.addFileToProject("grails-app/services/BookService.groovy", "class BookService {\n" + "  def author\n" + "}");
    VirtualFile virtualFile = myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/controllers/FooController.groovy");
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        PsiTestUtil.addSourceRoot(myModule, file.getVirtualFile().getParent());
      }
    }.execute();
    myFixture.configureFromExistingVirtualFile(virtualFile);
    myFixture.completeBasic();
    myFixture.checkResultByFile(getTestName(false) + "_after.groovy");
  }

  public void testJspTagCompletion() throws Throwable {
    myFixture.copyFileToProject("../fmt.tld", "WEB-INF/tld/fmt.tld");
    myFixture.testCompletion(getTestName(false) + ".gsp", getTestName(false) + "_after.gsp");
  }

  public void testDomainClassStaticProperties() throws Throwable {
    SpringJavaeeApplicationComponent.class.hashCode();
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    final List<String> stringList = myFixture.getCompletionVariants(path);
    for (Iterator<String> iterator = stringList.iterator(); iterator.hasNext();) {
      if (PsiElementFactoryImpl.getPrimitiveType(iterator.next()) != null) {
        iterator.remove();
      }
    }
    assertTrue(
      stringList.containsAll(CollectionFactory.newTroveSet("belongsTo", "constraints", "embedded", "hasMany", "optionals", "transients")));
    myFixture.type('b');
    myFixture.type('e');
    myFixture.type('l');
    myFixture.type('o');
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after.groovy");
  }

  public void testModifiersInGspGroovyDeclaration() throws Throwable {
    final List<String> data = myFixture.getCompletionVariants(getTestName(false) + ".gsp");
    assertTrue(data.toString(), data.containsAll(Arrays.asList("final", "public", "protected", "private", "static", "int")));
  }

  public void testConstrainsPropertyInDomainClass() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + "Constraints.groovy", "src/java/DomainConstraints.groovy");
    myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/Domain.groovy");
    myFixture.testCompletion("grails-app/domain/Domain.groovy", getTestName(false) + "_after.groovy");
  }

  public void testFindersWithBelongsTo() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/Domain.groovy");
    myFixture.testCompletionVariants("grails-app/domain/Domain.groovy", "findByAuthor", "findById", "findByName", "findByVersion");
  }

  public void testFindersWithHasMany() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".groovy", "grails-app/domain/Domain.groovy");
    myFixture.testCompletionVariants("grails-app/domain/Domain.groovy", "findByBooks", "findById", "findByVersion");
  }

  public void testDomainClassDynamicMethods() throws Exception {
    final String path = "grails-app/domain/Foo.groovy";
    myFixture.copyFileToProject(getTestName(false) + ".groovy", path);
    myFixture.testCompletionVariants(path, "id", "ident");
  }

}
