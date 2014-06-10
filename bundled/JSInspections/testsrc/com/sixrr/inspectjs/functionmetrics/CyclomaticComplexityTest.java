package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class CyclomaticComplexityTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("CyclomaticComplexity/", new CyclomaticComplexityJSInspection());
  }
}
