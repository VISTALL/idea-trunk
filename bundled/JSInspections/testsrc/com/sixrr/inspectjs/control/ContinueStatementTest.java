package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ContinueStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ContinueStatement/", new ContinueStatementJSInspection());
  }
}
