package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ContinueOrBreakFromFinallyBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ContinueOrBreakFromFinallyBlock/", new ContinueOrBreakFromFinallyBlockJSInspection());
  }
}
