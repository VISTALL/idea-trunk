package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DuplicateCaseLabelTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DuplicateCaseLabel/", new DuplicateCaseLabelJSInspection());
  }
}
