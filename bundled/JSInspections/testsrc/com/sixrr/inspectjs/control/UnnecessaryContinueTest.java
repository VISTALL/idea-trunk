package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryContinueTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryContinue/", new UnnecessaryContinueJSInspection());
  }
}
