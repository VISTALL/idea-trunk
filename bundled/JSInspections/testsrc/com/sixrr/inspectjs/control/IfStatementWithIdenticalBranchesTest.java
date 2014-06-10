package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class IfStatementWithIdenticalBranchesTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("IfStatementWithIdenticalBranches/", new IfStatementWithIdenticalBranchesJSInspection());
  }
}
