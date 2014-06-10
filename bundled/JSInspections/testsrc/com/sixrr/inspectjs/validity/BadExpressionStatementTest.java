package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class BadExpressionStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("BadExpressionStatement/", new BadExpressionStatementJSInspection());
  }
}
