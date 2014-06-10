package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NonBlockStatementBodyTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NonBlockStatementBody/", new NonBlockStatementBodyJSInspection());
  }
}
