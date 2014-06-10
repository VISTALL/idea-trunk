package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestedFunctionCallTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestedFunctionCall/", new NestedFunctionCallJSInspection());
  }
}
