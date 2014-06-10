package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DivideByZeroTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DivideByZero/", new DivideByZeroJSInspection());
  }
}
