package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnreachableCodeTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnreachableCode/", new UnreachableCodeJSInspection());
  }
}
