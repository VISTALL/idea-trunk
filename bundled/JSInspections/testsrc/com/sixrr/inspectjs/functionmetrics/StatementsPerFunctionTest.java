package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class StatementsPerFunctionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("StatementsPerFunction/", new StatementsPerFunctionJSInspection());
  }
}
