package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class LoopStatementThatDoesntLoopTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("LoopStatementThatDoesntLoop/", new LoopStatementThatDoesntLoopJSInspection());
  }
}
