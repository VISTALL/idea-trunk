package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryLabelOnBreakStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryLabelOnBreakStatement/", new UnnecessaryLabelOnBreakStatementJSInspection());
  }
}
