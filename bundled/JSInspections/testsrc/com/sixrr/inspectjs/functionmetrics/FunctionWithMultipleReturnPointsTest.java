package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class FunctionWithMultipleReturnPointsTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("FunctionWithMultipleReturnPoints/", new FunctionWithMultipleReturnPointsJSInspection());
  }
}
