package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ChainedEqualityTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ChainedEquality/", new ChainedEqualityJSInspection());
  }
}
