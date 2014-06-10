package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class BreakStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("BreakStatement/", new BreakStatementJSInspection());
  }
}
