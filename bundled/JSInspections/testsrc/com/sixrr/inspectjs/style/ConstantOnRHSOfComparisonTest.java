package com.sixrr.inspectjs.style;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConstantOnRHSOfComparisonTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConstantOnRHSOfComparison/", new ConstantOnRHSOfComparisonJSInspection());
  }
}
