package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class AssignmentToForLoopParameterTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("AssignmentToForLoopParameter/", new AssignmentToForLoopParameterJSInspection());
  }
}
