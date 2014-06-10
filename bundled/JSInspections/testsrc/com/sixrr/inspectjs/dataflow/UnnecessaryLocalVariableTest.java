package com.sixrr.inspectjs.dataflow;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class UnnecessaryLocalVariableTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("UnnecessaryLocalVariable/", new UnnecessaryLocalVariableJSInspection());
  }
}
