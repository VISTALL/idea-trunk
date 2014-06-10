package com.sixrr.inspectjs.dom;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class XHTMLIncompatabilitiesTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("XHTMLIncompatabilities/", new XHTMLIncompatabilitiesJSInspection());
  }
}
