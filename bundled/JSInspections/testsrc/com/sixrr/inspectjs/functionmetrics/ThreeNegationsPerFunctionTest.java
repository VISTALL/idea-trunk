package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ThreeNegationsPerFunctionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ThreeNegationsPerFunction/", new ThreeNegationsPerFunctionJSInspection());
  }
}
