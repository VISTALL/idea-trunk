package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DuplicateConditionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DuplicateCondition/", new DuplicateConditionJSInspection());
  }
}
