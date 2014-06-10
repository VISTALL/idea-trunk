package com.sixrr.inspectjs.control;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class TailRecursionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("TailRecursion/", new TailRecursionJSInspection());
  }
}
