package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class AssignmentToFunctionParameterTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("AssignmentToFunctionParameter/", new AssignmentToFunctionParameterJSInspection());
  }
}
