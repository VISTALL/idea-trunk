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

import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * @author peter
 */
public class GrailsActionsTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/actions/";
  }

  private void performCodeInsightAction(final String actionId) {
    CodeInsightAction action = (CodeInsightAction) ActionManager.getInstance().getAction(actionId);
    action.actionPerformedImpl(myFixture.getProject(), myFixture.getEditor());
  }

  public void testGspLineComment() throws Throwable { doTest(IdeActions.ACTION_COMMENT_LINE); }
  public void testGspLineUncomment() throws Throwable { doTest(IdeActions.ACTION_COMMENT_LINE); }
  public void testGspBlockComment() throws Throwable { doTest(IdeActions.ACTION_COMMENT_BLOCK); }

  private void doTest(String actionId) throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".gsp");
    performCodeInsightAction(actionId);
    myFixture.checkResultByFile(getTestName(false) + "_after.gsp");
  }

}