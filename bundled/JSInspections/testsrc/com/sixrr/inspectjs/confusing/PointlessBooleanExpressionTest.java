package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class PointlessBooleanExpressionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("PointlessBooleanExpression/", new PointlessBooleanExpressionJSInspection());
  }
}
