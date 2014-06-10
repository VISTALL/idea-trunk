/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.struts;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class StrutsQuickfixesTest extends StrutsTest {

  protected void configure(final WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
    addStrutsJar(moduleBuilder);
  }

  public void testFormFix() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("/WEB-INF/struts-config.xml");
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(list, "Create new Form Bean 'form'");
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile("/WEB-INF/struts-config_after.xml");
  }

  public void testActionFix() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("/testForm.jsp", "/WEB-INF/struts-config.xml", "/WEB-INF/web.xml");
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(list, "Create new Action '/unknown'");
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile("/WEB-INF/struts-config.xml", "/WEB-INF/struts-config_after_action.xml", false);
  }

  public void testForwardFix() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("/testForward.jsp", "/WEB-INF/struts-config.xml", "/WEB-INF/web.xml");
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(list, "Create new Forward 'unknown'");
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile("/WEB-INF/struts-config.xml", "/WEB-INF/struts-config_after_forward.xml", false);
  }

  public void testTilesFix() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("/testTiles.jsp", "/WEB-INF/struts-config.xml", "/WEB-INF/web.xml","/WEB-INF/tiles-defs.xml");
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(list, "Create new Definition 'unknown'");
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile("/WEB-INF/tiles-defs.xml", "/WEB-INF/tiles-defs_after.xml", false);
  }

  @NonNls
  public String getBasePath() {
    return getPluginTestDataPath() + "/quickfixes/";
  }
}
