package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class CommaExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("CommaExpression/", new CommaExpressionJSInspection());
  }
}
