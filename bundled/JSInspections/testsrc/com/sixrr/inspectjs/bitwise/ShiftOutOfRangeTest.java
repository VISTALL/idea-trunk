package com.sixrr.inspectjs.bitwise;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ShiftOutOfRangeTest extends InspectionJSTestCase {

  public void test() throws Exception {
    if (!JS_SUPPORTS_CONSTANT_EXPRESSIONS) return;
    doTest("ShiftOutOfRange/", new ShiftOutOfRangeJSInspection());
  }
}
