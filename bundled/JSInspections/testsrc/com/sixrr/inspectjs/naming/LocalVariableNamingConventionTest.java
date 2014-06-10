package com.sixrr.inspectjs.naming;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class LocalVariableNamingConventionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("LocalVariableNamingConvention/", new LocalVariableNamingConventionJSInspection());
  }
}
