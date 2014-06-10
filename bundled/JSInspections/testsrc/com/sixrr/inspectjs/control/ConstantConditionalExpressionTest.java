package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConstantConditionalExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConstantConditionalExpression/", new ConstantConditionalExpressionJSInspection());
  }
}
