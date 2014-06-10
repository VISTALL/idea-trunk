package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class EmptyTryBlockTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("EmptyTryBlock/", new EmptyTryBlockJSInspection());
  }
}
