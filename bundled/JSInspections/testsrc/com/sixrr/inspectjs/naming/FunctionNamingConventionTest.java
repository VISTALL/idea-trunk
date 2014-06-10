package com.sixrr.inspectjs.naming;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class FunctionNamingConventionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("FunctionNamingConvention/", new FunctionNamingConventionJSInspection());
  }
}
