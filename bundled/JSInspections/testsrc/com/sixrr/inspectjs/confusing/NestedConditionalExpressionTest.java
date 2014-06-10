package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestedConditionalExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestedConditionalExpression/", new NestedConditionalExpressionJSInspection());
  }
}
