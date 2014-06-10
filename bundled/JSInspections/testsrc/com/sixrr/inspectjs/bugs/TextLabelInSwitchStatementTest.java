package com.sixrr.inspectjs.bugs;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class TextLabelInSwitchStatementTest extends InspectionJSTestCase {

  public void test() throws Exception {
    doTest("TextLabelInSwitchStatement/", new TextLabelInSwitchStatementJSInspection());
  }
}
