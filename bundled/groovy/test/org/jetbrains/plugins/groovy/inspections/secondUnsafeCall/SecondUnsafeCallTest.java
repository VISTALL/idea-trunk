/*
 *  Copyright 2000-2007 JetBrains s.r.o.
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
 *
 */
package org.jetbrains.plugins.groovy.inspections.secondUnsafeCall;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.codeInspection.secondUnsafeCall.SecondUnsafeCallInspection;
import org.jetbrains.plugins.groovy.util.TestUtils;

import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 15.11.2007
 */
public class SecondUnsafeCallTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testdata/groovy/inspections/secondUnsafeCall";
  }

  public void doTest() throws Exception {
    final List<String> data = TestUtils.readInput(getTestDataPath() + "/" + getTestName(true) + ".test");

    myFixture.configureByText("a.groovy", data.get(0));
    myFixture.enableInspections(new SecondUnsafeCallInspection());
    final IntentionAction action = myFixture.findSingleIntention("Second unsafe call");
    myFixture.launchAction(action);

    myFixture.checkResult(data.get(1));
  }

  public void test4Calls() throws Throwable { doTest(); }
  public void testMethodCall() throws Throwable { doTest(); }
  public void testMethodsCalls() throws Throwable { doTest(); }
  public void testSecondUnsafeCall1() throws Throwable { doTest(); }
  public void testVarInit() throws Throwable { doTest(); }

}