package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class TrivialConditionalTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("TrivialConditional/", new TrivialConditionalJSInspection());
  }
}
