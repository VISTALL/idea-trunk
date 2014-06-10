package com.sixrr.inspectjs.functionmetrics;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class NestingDepthTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("NestingDepth/", new NestingDepthJSInspection());
  }
}
