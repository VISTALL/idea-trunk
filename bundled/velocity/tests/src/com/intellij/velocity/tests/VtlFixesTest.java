/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Condition;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.inspections.DefineInCommentIntention;

import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VtlFixesTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/velocity/tests/testData/fixes";
  }

  public void testDefineImplicitVar() throws Throwable {
    launchSingleIntention("Define implicit variable (in comment in current file)");
  }

  public void testDefineImplicitVarWithType() throws Throwable {
    launchSingleIntention("Define implicit variable (in comment in current file)");
  }

  public void testDefineImplicitVarWithType1stLineComment() throws Throwable {
    launchSingleIntention("Define implicit variable (in comment in current file)");
  }

  public void testDefineVariableExternallyLocal() throws Throwable {
    checkImplicitlyIncludedFile(getIntentions("Define implicit variable externally (for this file only)"));
  }

  public void testDefineVariableExternallyModuleWide() throws Throwable {
    checkImplicitlyIncludedFile(getIntentions("Define implicit variable externally (module-wide)"));
  }

  public void testDefineMacroLibraryLocally() throws Throwable {
    myFixture.copyFileToProject("macroLibraryForTests.vm");
    List<IntentionAction> list = getIntentions("Define macro library reference (in comment in current file)");
    myFixture.launchAction(assertOneElement(list));
    myFixture.checkResultByFile(getThisTestExpectedResultFileName());
  }

  public void testDefineMacroLibraryExternallyLocal() throws Throwable {
    checkImplicitlyIncludedFileForMacroLibrary("Define macro library reference externally (for this file only)");
  }

  public void testDefineMacroLibraryExternallyModuleWide() throws Throwable {
    checkImplicitlyIncludedFileForMacroLibrary("Define macro library reference externally (module-wide)");
  }

  public void testUnresolvedQualifier() throws Throwable {
    assertEmpty(getIntentions());
  }

  public void testDefineVelocityPropertiesForFileExternallyModuleWide() throws Throwable {
    myFixture.copyFileToProject("velocity.properties");
    myFixture.copyDirectoryToProject("runtime", "runtime");
    checkImplicitlyIncludedFile(getIntentions("Define velocity.properties file reference externally (module-wide)"));
  }

  public void testDefineVelocityPropertiesForMacroExternallyModuleWide() throws Throwable {
    myFixture.copyFileToProject("velocity.properties");
    myFixture.copyDirectoryToProject("runtime", "runtime");
    checkImplicitlyIncludedFile(getIntentions("Define velocity.properties file reference externally (module-wide)"));
  }

  private void launchSingleIntention(String hint) throws Throwable {
    List<IntentionAction> list = getIntentions(hint);
    myFixture.launchAction(assertOneElement(list));
    myFixture.checkResultByFile(getThisTestExpectedResultFileName());
  }

  private void checkImplicitlyIncludedFileForMacroLibrary(String hint) throws Throwable {
    myFixture.copyFileToProject("macroLibraryForTests.vm");
    checkImplicitlyIncludedFile(getIntentions(hint));
  }

  private void checkImplicitlyIncludedFile(List<IntentionAction> list) throws Throwable {
    myFixture.launchAction(assertOneElement(list));
    FileDocumentManager.getInstance().saveAllDocuments();
    myFixture.configureFromExistingVirtualFile(myFixture.getTempDirFixture().getFile(DefineInCommentIntention.VELOCITY_IMPLICIT_VM));
    myFixture.checkResultByFile(getThisTestExpectedResultFileName());
  }

  private String getThisTestExpectedResultFileName() {
    return Util.getExpectedResultFileName(getTestName(true));
  }

  private String getThisTestInputFileName() {
    return Util.getInputDataFileName(getTestName(true));
  }

  private List<IntentionAction> getIntentions() throws Throwable {
    return myFixture.getAvailableIntentions(getThisTestInputFileName());
  }

  private List<IntentionAction> getIntentions(final String hint) throws Throwable {
    return ContainerUtil.findAll(getIntentions(), new Condition<IntentionAction>() {
      public boolean value(final IntentionAction intentionAction) {
        return intentionAction.getText().startsWith(hint);
      }
    });
  }


}