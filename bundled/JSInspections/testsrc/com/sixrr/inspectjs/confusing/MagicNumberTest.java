package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class MagicNumberTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("MagicNumber/", new MagicNumberJSInspection());
  }
}
