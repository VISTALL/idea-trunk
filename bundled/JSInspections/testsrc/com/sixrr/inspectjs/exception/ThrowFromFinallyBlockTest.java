package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ThrowFromFinallyBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ThrowFromFinallyBlock/", new ThrowFromFinallyBlockJSInspection());
  }
}
