package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class FallthroughInSwitchStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("FallthroughInSwitchStatement/", new FallthroughInSwitchStatementJSInspection());
  }
}
