package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConfusingPlusesOrMinusesTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConfusingPlusesOrMinuses/", new ConfusingPlusesOrMinusesJSInspection());
  }
}
