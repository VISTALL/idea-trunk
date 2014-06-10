package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NegatedIfStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NegatedIfStatement/", new NegatedIfStatementJSInspection());
  }
}
