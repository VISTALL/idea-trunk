package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class TrivialIfTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("TrivialIf/", new TrivialIfJSInspection());
  }
}
