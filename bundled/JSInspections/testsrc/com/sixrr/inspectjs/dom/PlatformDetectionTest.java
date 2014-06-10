package com.sixrr.inspectjs.dom;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class PlatformDetectionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("PlatformDetection/", new PlatformDetectionJSInspection());
  }
}
