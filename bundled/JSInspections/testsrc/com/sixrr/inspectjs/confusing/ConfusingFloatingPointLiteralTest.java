package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class ConfusingFloatingPointLiteralTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("ConfusingFloatingPointLiteral/", new ConfusingFloatingPointLiteralJSInspection());
  }
}
