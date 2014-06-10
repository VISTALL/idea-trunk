package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class IncrementDecrementResultUsedTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("IncrementDecrementResultUsed/", new IncrementDecrementResultUsedJSInspection());
  }
}
