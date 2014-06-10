package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class SillyAssignmentTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("SillyAssignment/", new SillyAssignmentJSInspection());
  }
}
