package com.sixrr.inspectjs.dom;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class DocumentWriteTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("DocumentWrite/", new DocumentWriteJSInspection());
  }
}
