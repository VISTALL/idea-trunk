package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestedFunctionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestedFunction/", new NestedFunctionJSInspection());
  }
}
