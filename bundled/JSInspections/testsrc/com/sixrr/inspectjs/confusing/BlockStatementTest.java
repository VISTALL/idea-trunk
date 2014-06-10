package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class BlockStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("BlockStatement/", new BlockStatementJSInspection());
  }
}
