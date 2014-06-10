package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class EqualityComparisonWithCoercionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("EqualityComparisonWithCoercion/", new EqualityComparisonWithCoercionJSInspection());
  }
}
