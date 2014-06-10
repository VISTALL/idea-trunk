package com.sixrr.inspectjs.dataflow;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ReuseOfLocalVariableTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ReuseOfLocalVariable/", new ReuseOfLocalVariableJSInspection());
  }
}
