package com.sixrr.inspectjs.bitwise;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class IncompatibleMaskTest extends InspectionJSTestCase {

  public void test() throws Exception {
    if (!JS_SUPPORTS_CONSTANT_EXPRESSIONS) return;
    doTest("IncompatibleMask/", new IncompatibleMaskJSInspection());
  }
}
