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

/*
 * User: anna
 * Date: 16-Jun-2009
 */
package com.siyeh.ig.fixes.bugs;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.siyeh.ig.bugs.CastConflictsWithInstanceofInspection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class IGQuickFixesTestCase {
  protected CodeInsightTestFixture myFixture;

  @Before
  public void setUp() throws Exception {
    final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
    final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = fixtureFactory.createFixtureBuilder();
    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(testFixtureBuilder.getFixture());
    myFixture.enableInspections(new CastConflictsWithInstanceofInspection());
    final String dataPath = PathManager.getHomePath() + "/svnPlugins/InspectionGadgets/test/com/siyeh/igfixes/";
    myFixture.setTestDataPath(dataPath);
    final JavaModuleFixtureBuilder builder = testFixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

    builder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("");
    builder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    myFixture.setUp();
  }

  @After
  public void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
  }

  protected void doTest(String testName, String hint) throws Throwable {
    myFixture.configureByFile(getRelativePath() + "/" + testName + ".java");
    final IntentionAction action = myFixture.findSingleIntention(hint);
    Assert.assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile(getRelativePath() + "/" + testName + ".after.java");
  }

  protected abstract String getRelativePath();
}