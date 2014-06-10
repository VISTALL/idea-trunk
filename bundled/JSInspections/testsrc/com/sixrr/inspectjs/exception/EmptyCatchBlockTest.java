package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class EmptyCatchBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("EmptyCatchBlock/", new EmptyCatchBlockJSInspection());
  }
}
