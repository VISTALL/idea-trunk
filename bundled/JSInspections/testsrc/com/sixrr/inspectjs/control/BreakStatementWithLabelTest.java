package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class BreakStatementWithLabelTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("BreakStatementWithLabel/", new BreakStatementWithLabelJSInspection());
  }
}
