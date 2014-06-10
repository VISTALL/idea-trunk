package com.sixrr.inspectjs.dom;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class InnerHTMLTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("InnerHTML/", new InnerHTMLJSInspection());
  }
}
