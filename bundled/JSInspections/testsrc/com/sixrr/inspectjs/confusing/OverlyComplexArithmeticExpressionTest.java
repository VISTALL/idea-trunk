package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class OverlyComplexArithmeticExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("OverlyComplexArithmeticExpression/", new OverlyComplexArithmeticExpressionJSInspection());
  }
}
