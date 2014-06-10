package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestedAssignmentTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestedAssignment/", new NestedAssignmentJSInspection());
  }
}
