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
package org.jetbrains.plugins.groovy.grails.completion;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.plugins.groovy.lang.completion.CompletionTestBase;
import org.jetbrains.plugins.groovy.util.TestUtils;
import org.jetbrains.plugins.groovy.grails.GrailsTestUtil;

/**
 * @author ilyas
 */
public class GspTagReferenceCompletionTest extends CompletionTestBase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/oldCompletion/gsp/";
  }

  @Override
  protected String getExtension() {
    return "gsp";
  }

  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder fixtureBuilder) {
    fixtureBuilder.addLibraryJars("GRAILS", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GRAILS_JAR);
    fixtureBuilder.addLibraryJars("GROOVY", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GROOVY_JAR);
    String path = FileUtil.toSystemIndependentName(PathManager.getHomePath()) + "/svnPlugins/groovy/mvc-testdata/mockTagLib";
    fixtureBuilder.addContentRoot(path).addSourceRoot("");
  }

  public void testAttr1() throws Throwable { doTest(); }
  public void testContent() throws Throwable { doTest(); }
  public void testCustom1() throws Throwable { doTest(); }
  public void testCustomNamespacePrefix() throws Throwable { doTest(); }
  public void testDirname() throws Throwable { doTest(); }
  public void testG1() throws Throwable { doTest(); }
  public void testGet1() throws Throwable { doTest(); }
  public void testGroo1() throws Throwable { doTest(); }
  public void testGroo2() throws Throwable { doTest(); }
  public void testGroo3() throws Throwable { doTest(); }
  public void testGroo4() throws Throwable { doTest(); }
  public void testGroo5() throws Throwable { doTest(); }
  public void testHtml1() throws Throwable { doTest(); }
  public void testLink() throws Throwable { doTest(); }
  public void testLink2() throws Throwable { doTest(); }
  public void testMy1() throws Throwable { doTest(); }
  public void testTail1() throws Throwable { doTest(); }
  public void testTail2() throws Throwable { doTest(); }
  public void testTail3() throws Throwable { doTest(); }
  public void testTail4() throws Throwable { doTest(); }
}
