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
package org.jetbrains.plugins.groovy.lang;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * @author peter
 */
@SuppressWarnings({"JUnitTestClassNamingConvention"})
public class Groovy16HighlightingTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testdata/highlighting/";
  }

  private void doTest(LocalInspectionTool... tools) throws Exception {
    myFixture.enableInspections(tools);
    myFixture.testHighlighting(true, false, false, getTestName(false) + ".groovy");
  }

  public void testInnerEnum() throws Exception {doTest();}
}