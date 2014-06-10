package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConstantOnLHSOfComparisonTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConstantOnLHSOfComparison/", new ConstantOnLHSOfComparisonJSInspection());
  }
}
