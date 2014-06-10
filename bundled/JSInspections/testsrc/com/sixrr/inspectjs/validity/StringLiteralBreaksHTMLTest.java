package com.sixrr.inspectjs.validity;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class StringLiteralBreaksHTMLTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("StringLiteralBreaksHTML/", new StringLiteralBreaksHTMLJSInspection());
  }
}
