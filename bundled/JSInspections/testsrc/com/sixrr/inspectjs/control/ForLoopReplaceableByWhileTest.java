package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ForLoopReplaceableByWhileTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ForLoopReplaceableByWhile/", new ForLoopReplaceableByWhileJSInspection());
  }
}
