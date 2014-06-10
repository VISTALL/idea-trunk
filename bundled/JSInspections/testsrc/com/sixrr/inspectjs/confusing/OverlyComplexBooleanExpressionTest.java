package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class OverlyComplexBooleanExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("OverlyComplexBooleanExpression/", new OverlyComplexBooleanExpressionJSInspection());
  }
}
