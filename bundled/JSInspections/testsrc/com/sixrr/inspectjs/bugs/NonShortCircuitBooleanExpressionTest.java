package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NonShortCircuitBooleanExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NonShortCircuitBooleanExpression/", new NonShortCircuitBooleanExpressionJSInspection());
  }
}
