package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestedSwitchStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestedSwitchStatement/", new NestedSwitchStatementJSInspection());
  }
}
