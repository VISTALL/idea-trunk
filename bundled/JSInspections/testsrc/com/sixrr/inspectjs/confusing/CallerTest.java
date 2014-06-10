package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class CallerTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("Caller/", new CallerJSInspection());
  }
}
