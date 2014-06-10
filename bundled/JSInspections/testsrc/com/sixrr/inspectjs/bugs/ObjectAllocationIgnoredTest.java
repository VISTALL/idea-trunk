package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ObjectAllocationIgnoredTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ObjectAllocationIgnored/", new ObjectAllocationIgnoredJSInspection());
  }
}
