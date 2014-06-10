package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class PointlessArithmeticExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("PointlessArithmeticExpression/", new PointlessArithmeticExpressionJSInspection());
  }
}
