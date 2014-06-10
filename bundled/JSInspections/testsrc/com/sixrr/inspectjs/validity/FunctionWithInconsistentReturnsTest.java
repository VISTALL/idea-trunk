package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class FunctionWithInconsistentReturnsTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("FunctionWithInconsistentReturns/", new FunctionWithInconsistentReturnsJSInspection());
  }
}
