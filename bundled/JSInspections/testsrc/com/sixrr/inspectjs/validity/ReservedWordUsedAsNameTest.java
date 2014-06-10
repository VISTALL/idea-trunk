package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ReservedWordUsedAsNameTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ReservedWordUsedAsName/", new ReservedWordUsedAsNameJSInspection());
  }
}
