package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class FunctionWithMultipleLoopsTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("FunctionWithMultipleLoops/", new FunctionWithMultipleLoopsJSInspection());
  }
}
