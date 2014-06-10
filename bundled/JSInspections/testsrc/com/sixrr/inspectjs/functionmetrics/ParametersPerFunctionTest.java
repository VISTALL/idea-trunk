package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ParametersPerFunctionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ParametersPerFunction/", new ParametersPerFunctionJSInspection());
  }
}
