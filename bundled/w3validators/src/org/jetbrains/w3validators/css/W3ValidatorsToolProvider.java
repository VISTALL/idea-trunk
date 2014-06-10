package org.jetbrains.w3validators.css;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author spleaner
 */
public class W3ValidatorsToolProvider implements InspectionToolProvider {

  public Class[] getInspectionClasses() {
    return new Class[] {W3CssValidatorInspection.class};
  }
}
