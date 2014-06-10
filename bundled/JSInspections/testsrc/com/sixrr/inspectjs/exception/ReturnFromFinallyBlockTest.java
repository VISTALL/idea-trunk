package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ReturnFromFinallyBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ReturnFromFinallyBlock/", new ReturnFromFinallyBlockJSInspection());
  }
}
