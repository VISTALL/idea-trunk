package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnterminatedStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnterminatedStatement/", new UnterminatedStatementJSInspection());
  }
}
