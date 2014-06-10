package com.sixrr.inspectjs.bitwise;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class PointlessBitwiseExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    if (!JS_SUPPORTS_CONSTANT_EXPRESSIONS) return;
    doTest("PointlessBitwiseExpression/", new PointlessBitwiseExpressionJSInspection());
  }
}
