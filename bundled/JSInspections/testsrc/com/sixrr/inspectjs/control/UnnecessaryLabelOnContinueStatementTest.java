package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryLabelOnContinueStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryLabelOnContinueStatement/", new UnnecessaryLabelOnContinueStatementJSInspection());
  }
}
