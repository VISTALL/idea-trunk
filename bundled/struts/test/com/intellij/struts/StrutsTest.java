/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
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

package com.intellij.struts;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.struts.highlighting.StrutsInspection;
import com.intellij.struts.highlighting.TilesInspection;
import com.intellij.struts.highlighting.ValidatorInspection;
import com.intellij.struts.inplace.inspections.ValidatorFormInspection;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author Dmitry Avdeev
 */
public abstract class StrutsTest extends TestCase {

  protected CodeInsightTestFixture myFixture;
  protected WebModuleTestFixture myWebModuleTestFixture;

  private Module myModule;

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = JavaTestFixtureFactory.createFixtureBuilder();

    final WebModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(WebModuleFixtureBuilder.class);


    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    final String root = getTestDataPath();
    myFixture.setTestDataPath(root);

    final String tempDirPath = myFixture.getTempDirPath();
    moduleBuilder.addContentRoot(tempDirPath);
    moduleBuilder.addWebRoot(tempDirPath, "/");

    configure(moduleBuilder);

    myFixture.enableInspections(new ValidatorFormInspection(),
                                new StrutsInspection(),
                                new TilesInspection(),
                                new ValidatorInspection());
    
    myFixture.setUp();
    myWebModuleTestFixture = moduleBuilder.getFixture();
    myModule = myWebModuleTestFixture.getModule();
  }


  protected void tearDown() throws Exception {
    myWebModuleTestFixture = null;
    myFixture.tearDown();
    myFixture = null;
    myModule = null;
    super.tearDown();
  }

  @NonNls
  protected String getBasePath() {
    return getPluginTestDataPath() + "/struts/";
  }

  protected String getPluginTestDataPath() {
    return "/svnPlugins/struts/testData";
  }

  protected void configure(WebModuleFixtureBuilder moduleBuilder) {
    final String root = getTestDataPath();
    moduleBuilder.addContentRoot(root);
    moduleBuilder.addWebRoot(root, "/");
    moduleBuilder.setWebXml(root + "/WEB-INF/web.xml");
    addStrutsJar(moduleBuilder);
  }

  protected void addStrutsJar(final WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("struts", PathManager.getHomePath().replace(File.separatorChar, '/') + "/svnPlugins/struts/testData/lib/", getLibraries());
  }

  protected String[] getLibraries() {
    return new String[] { "struts.jar", "commons-beanutils.jar" };
  }

  @NonNls
  protected String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath();
  }

  protected Module getModule() {
    return myModule;
  }
}
