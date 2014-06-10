package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class WithStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("WithStatement/", new WithStatementJSInspection());
  }
}
