package com.sixrr.inspectjs.exception;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnusedCatchParameterTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnusedCatchParameter/", new UnusedCatchParameterJSInspection());
  }
}
