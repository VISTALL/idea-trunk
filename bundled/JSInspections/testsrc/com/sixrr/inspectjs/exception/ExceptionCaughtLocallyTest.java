package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ExceptionCaughtLocallyTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ExceptionCaughtLocally/", new ExceptionCaughtLocallyJSInspection());
  }
}
