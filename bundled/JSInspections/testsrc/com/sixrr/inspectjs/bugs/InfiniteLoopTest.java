package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class InfiniteLoopTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("InfiniteLoop/", new InfiniteLoopJSInspection());
  }
}
