package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class VoidExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("VoidExpression/", new VoidExpressionJSInspection());
  }
}
