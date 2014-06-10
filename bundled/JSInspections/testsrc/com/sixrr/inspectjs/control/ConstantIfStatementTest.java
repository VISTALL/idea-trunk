package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConstantIfStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConstantIfStatement/", new ConstantIfStatementJSInspection());
  }
}
