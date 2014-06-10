package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ReplaceAssignmentWithOperatorAssignmentTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ReplaceAssignmentWithOperatorAssignment/", new ReplaceAssignmentWithOperatorAssignmentJSInspection());
  }
}
