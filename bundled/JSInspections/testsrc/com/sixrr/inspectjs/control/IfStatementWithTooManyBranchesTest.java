package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class IfStatementWithTooManyBranchesTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("IfStatementWithTooManyBranches/", new IfStatementWithTooManyBranchesJSInspection());
  }
}
