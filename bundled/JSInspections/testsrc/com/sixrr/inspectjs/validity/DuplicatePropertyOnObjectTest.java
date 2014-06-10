package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DuplicatePropertyOnObjectTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DuplicatePropertyOnObject/", new DuplicatePropertyOnObjectJSInspection());
  }
}
