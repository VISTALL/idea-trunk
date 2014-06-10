package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class InfiniteRecursionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("InfiniteRecursion/", new InfiniteRecursionJSInspection());
  }
}
