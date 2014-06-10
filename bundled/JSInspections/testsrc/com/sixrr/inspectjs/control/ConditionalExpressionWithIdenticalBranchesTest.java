package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConditionalExpressionWithIdenticalBranchesTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConditionalExpressionWithIdenticalBranches/", new ConditionalExpressionWithIdenticalBranchesJSInspection());
  }
}
