package com.sixrr.inspectjs.confusing;

import com.sixrr.inspectjs.InspectionJSTestCase;

public class EmptyStatementBodyTest extends InspectionJSTestCase {

  public void test() throws Exception {
      final EmptyStatementBodyJSInspection inspection = new EmptyStatementBodyJSInspection();
      inspection.m_reportEmptyBlocks = true;
      doTest("EmptyStatementBody/", inspection);
  }
}
