package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class AnonymousFunctionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("AnonymousFunction/", new AnonymousFunctionJSInspection());
  }
}
