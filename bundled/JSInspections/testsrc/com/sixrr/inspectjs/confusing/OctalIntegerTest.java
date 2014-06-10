package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class OctalIntegerTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("OctalInteger/", new OctalIntegerJSInspection());
  }
}
