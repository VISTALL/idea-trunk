package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryLabelTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryLabel/", new UnnecessaryLabelJSInspection());
  }
}
