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

package com.intellij.beanValidation;

import com.intellij.beanValidation.facet.BeanValidationFacet;
import com.intellij.beanValidation.facet.BeanValidationFacetType;
import com.intellij.facet.FacetManager;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.javaee.ejb.facet.EjbFacetType;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;

import java.io.File;

/**
 * @author Konstantin Bulenkov
 */
public abstract class AbstractBeanValidationTestCase<T extends JavaModuleFixtureBuilder> extends BaseBeanValidationTestCase {

  protected CodeInsightTestFixture myFixture;
  protected ModuleFixture myModuleTestFixture;
  protected Project myProject;
  protected Module myModule;

  protected Class<T> getModuleFixtureBuilderClass() {
    return (Class<T>)JavaModuleFixtureBuilder.class;
  }

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();

    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    final T moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());

    myFixture.setTestDataPath(getTestDataPath());

    myFixture.enableInspections(new BeanValidationApplicationComponent().getInspectionClasses());

    configureModule(moduleBuilder);
    myFixture.setUp();

    myProject = myFixture.getProject();
    myModuleTestFixture = moduleBuilder.getFixture();
    myModule = myModuleTestFixture.getModule();

    createFacet();
    BeanValidationProjectComponent.getInstance(myProject).projectOpened();
  }

  protected BeanValidationFacet createFacet() {
    final RunResult<BeanValidationFacet> runResult = new WriteCommandAction<BeanValidationFacet>(myProject) {
      protected void run(final Result<BeanValidationFacet> result) throws Throwable {
        FacetManager facetManager = FacetManager.getInstance(myModule);
        result.setResult(facetManager.addFacet(BeanValidationFacetType.INSTANCE, BeanValidationFacetType.INSTANCE.getPresentableName(), null));
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

  protected void configureModule(final T moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("");
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    addBeanValidationJars(moduleBuilder);
  }

  protected void addBeanValidationJars(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("validation-api-1.0.CR2.jar", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 getLibsDirectory() + "validation-api-1.0.CR2.jar");
  }

  private String getLibsDirectory() {
    return "libs/";
  }

  protected void tearDown() throws Exception {
    BeanValidationProjectComponent.getInstance(myProject).projectClosed();
    myFixture.tearDown();
    myFixture = null;
    myModuleTestFixture = null;
    myProject = null;
    myModule = null;

    super.tearDown();
  }

  protected XmlFile getXmlFile(final String path) {
    final String url = myFixture.getTempDirPath() + File.separatorChar + path;

    final VirtualFile[] virtualFile = new VirtualFile[1];
    new WriteCommandAction(myFixture.getProject()) {
      protected void run(final Result result) throws Throwable {
        virtualFile[0] = LocalFileSystem.getInstance().refreshAndFindFileByPath(url);
      }
    }.execute();

    assertNotNull("Cannot find file " + url, virtualFile[0]);
    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile[0]);

    assertNotNull(psiFile);
    assertTrue("'" + path + "' must be a xml file", psiFile instanceof XmlFile);

    return (XmlFile)psiFile;
  }

  protected void addJavaeeSupport() {
    new WriteCommandAction(myProject) {
      protected void run(final Result result) throws Throwable {
        JavaeeUtil.addFacet(myModule, EjbFacetType.INSTANCE);
      }
    }.execute();

    PsiTestUtil.addLibrary(myModule, "JavaEE", PathManager.getHomePath() + "/lib/", "javaee.jar", "javase-javax.jar");
  }

  protected void doTestXmlHighlighting() throws Throwable {
    final String xml = "META-INF/" + getTestName(false) + ".xml";
    myFixture.configureByFile(xml);
    myFixture.testHighlighting(xml);
  }

}

