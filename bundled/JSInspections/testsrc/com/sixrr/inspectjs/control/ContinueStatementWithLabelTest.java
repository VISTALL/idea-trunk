package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ContinueStatementWithLabelTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ContinueStatementWithLabel/", new ContinueStatementWithLabelJSInspection());
  }
}
