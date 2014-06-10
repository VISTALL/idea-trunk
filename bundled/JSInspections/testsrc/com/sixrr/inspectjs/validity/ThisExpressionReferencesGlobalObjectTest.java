package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ThisExpressionReferencesGlobalObjectTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ThisExpressionReferencesGlobalObject/", new ThisExpressionReferencesGlobalObjectJSInspection());
  }
}
