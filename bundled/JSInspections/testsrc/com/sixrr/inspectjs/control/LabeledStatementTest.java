package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class LabeledStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("LabeledStatement/", new LabeledStatementJSInspection());
  }
}
