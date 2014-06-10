package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class SwitchStatementWithNoDefaultBranchTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("SwitchStatementWithNoDefaultBranch/", new SwitchStatementWithNoDefaultBranchJSInspection());
  }
}
