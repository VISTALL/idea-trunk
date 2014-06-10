package com.intellij.velocity.tests;

import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.openapi.application.PathManager;

/**
 * @author Alexey Chmutov
 *         Date: Jul 1, 2009
 *         Time: 8:17:23 PM
 */
public class VtlFormatterTest extends FormatterTestCase {

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath();
  }

  protected String getBasePath() {
    return "/svnPlugins/velocity/tests/testData/formatter";
  }

  protected String getFileExtension() {
    return "vm";
  }

  public void testVtlOnlyForeach() throws Exception {
    doTest();
  }

  public void testVtlOnlyIfElseifElseEnd() throws Exception {
    doTest();
  }

  public void testIfElseifElseEndInHtml() throws Exception {
    doTest();
  }

  public void testSimpleIfInHtml() throws Exception {
    doTest();
  }

  public void testMacroInPlainText() throws Exception {
    String testName = getTestName(true);
    String fileNameBefore = testName + ".test." + getFileExtension();
    String fileNameAfter = testName + "_after.test." + getFileExtension();
    doTextTest(loadFile(fileNameBefore, null), loadFile(fileNameAfter, null));
  }

  public void testVtlBlockRightUpToHtmlBlock() throws Exception {
    doTest();
  }

}
