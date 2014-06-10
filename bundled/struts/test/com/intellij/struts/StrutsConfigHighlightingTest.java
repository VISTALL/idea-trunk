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

import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;

/**
 * @author Dmitry Avdeev
 */
public class StrutsConfigHighlightingTest extends StrutsTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Project project = myFixture.getProject();
    ((StartupManagerImpl)StartupManager.getInstance(project)).runPostStartupActivities();
  }

  public void testStrutsConfig() throws Throwable {
    final long duration = myFixture.testHighlighting("/WEB-INF/struts-config.xml");
    System.out.println("testStrutsConfig() Duration: " + duration + " ms");
  }

  public void testStrutsConfigWildcards() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/struts-config-wildcards.xml");
  }

  public void testTilesConfig() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/tiles-defs.xml");
  }

  public void testValidation() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/validation.xml");
    myFixture.testHighlighting("/WEB-INF/validation13.xml");
    myFixture.testHighlighting("/WEB-INF/validator-rules.xml");
  }

  public void testStrutsPage() throws Throwable {
    final long duration = myFixture.testHighlighting("/index.jsp");
    System.out.println("Duration: " + duration + " ms");
  }

  public void testTilesPage() throws Throwable {
    myFixture.testHighlighting("/tilesPage.jsp");
//    myFixture.testHighlighting("/resources/pages/tilesInsertTest.jsp");
  }

  public void testFileReferencePerformance() throws Throwable {
    final long duration = myFixture.testHighlighting("/fileReferencePerformance.jsp");
    System.out.println("testFileReferencePerformance() Duration: " + duration + " ms");
  }

  public void testHtmlTags() throws Throwable {
    final long duration = myFixture.testHighlighting("/html_taglib.jsp");
    System.out.println("testHtmlTags() Duration = " + duration + " ms");
  }

  public void testJavascriptInjection() throws Throwable {
    try {
      JavaScriptIndex.getInstance(myFixture.getProject()).projectOpened();
      ((StartupManagerImpl)StartupManager.getInstance(myFixture.getProject())).runStartupActivities();
      myFixture.testHighlighting(true, false, true, "/testJSinjected.jsp");
    }
    finally {
      JavaScriptIndex.getInstance(myFixture.getProject()).projectClosed();
    }
  }

  public void testWeirdAttribute() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/weird-attribute.xml");
  }

  protected void configure(WebModuleFixtureBuilder moduleBuilder) {
    super.configure(moduleBuilder);
    moduleBuilder.addSourceRoot("src");
  }

  protected String[] getLibraries() {
    return new String[] { "struts.jar", "struts-el.jar", "sslext.jar", "commons-dbcp.jar", "commons-beanutils.jar"};
  }
}
