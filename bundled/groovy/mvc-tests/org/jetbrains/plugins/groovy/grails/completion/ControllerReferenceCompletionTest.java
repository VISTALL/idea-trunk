/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.plugins.groovy.lang.completion.CompletionTestBase;
import org.jetbrains.plugins.groovy.util.TestUtils;
import org.jetbrains.plugins.groovy.grails.GrailsTestUtil;

/**
 * @author ilyas
 */
public class ControllerReferenceCompletionTest extends CompletionTestBase {

  public void testContr1() throws Throwable { doTest(); }
  public void testContr2() throws Throwable { doTest(); }
  public void testContr3() throws Throwable { doTest(); }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/oldCompletion/controllers/";
  }

  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder fixtureBuilder) {
    fixtureBuilder.addLibraryJars("GRAILS", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GRAILS_JAR);
    fixtureBuilder.addLibraryJars("GROOVY", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GROOVY_JAR);
  }
}
