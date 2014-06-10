package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DebuggerStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DebuggerStatement/", new DebuggerStatementJSInspection());
  }
}
