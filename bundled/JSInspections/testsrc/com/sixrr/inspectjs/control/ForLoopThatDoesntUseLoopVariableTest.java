package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ForLoopThatDoesntUseLoopVariableTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ForLoopThatDoesntUseLoopVariable/", new ForLoopThatDoesntUseLoopVariableJSInspection());
  }
}
