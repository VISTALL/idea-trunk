package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class EmptyFinallyBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("EmptyFinallyBlock/", new EmptyFinallyBlockJSInspection());
  }
}
