package com.sixrr.inspectjs;

import com.intellij.testFramework.InspectionFixtureTestCase;

public abstract class InspectionJSTestCase extends InspectionFixtureTestCase {
  protected static final boolean JS_SUPPORTS_CONSTANT_EXPRESSIONS = false;

  @Override
  protected String getBasePath() {
    return "/svnPlugins/JSInspections/testdata";
  }
}
