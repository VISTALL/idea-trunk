package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryReturnTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryReturn/", new UnnecessaryReturnJSInspection());
  }
}
