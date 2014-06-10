package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DefaultNotLastCaseInSwitchTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DefaultNotLastCaseInSwitch/", new DefaultNotLastCaseInSwitchJSInspection());
  }
}
