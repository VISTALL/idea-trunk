package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ChainedFunctionCallTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ChainedFunctionCall/", new ChainedFunctionCallJSInspection());
  }
}
