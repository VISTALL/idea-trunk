package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NegatedConditionalExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NegatedConditionalExpression/", new NegatedConditionalExpressionJSInspection());
  }
}
