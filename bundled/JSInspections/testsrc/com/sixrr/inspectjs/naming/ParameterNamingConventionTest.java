package com.sixrr.inspectjs.naming;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ParameterNamingConventionTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ParameterNamingConvention/", new ParameterNamingConventionJSInspection());
  }
}
