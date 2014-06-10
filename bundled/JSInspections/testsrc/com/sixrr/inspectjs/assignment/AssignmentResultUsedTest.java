package com.sixrr.inspectjs.assignment;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class AssignmentResultUsedTest extends InspectionJSTestCase {

    public void test() throws Exception {

    doTest("AssignmentResultUsed/", new AssignmentResultUsedJSInspection());
  }
}
