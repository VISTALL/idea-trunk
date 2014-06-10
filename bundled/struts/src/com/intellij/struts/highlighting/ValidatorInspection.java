package com.intellij.struts.highlighting;

import com.intellij.struts.dom.validator.FormValidation;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class ValidatorInspection extends BasicDomElementsInspection<FormValidation> {

  public ValidatorInspection() {
    super(FormValidation.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return "Struts 1";   // todo Dm.Avdeev: bundle string literal
  }

  @NotNull
  @Override
  public String[] getGroupPath() {
    return new String[]{"Struts", getGroupDisplayName()};
  }

  @NotNull
  public String getDisplayName() {
    return "Validator inspection";
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "StrutsValidatorInspection";
  }
}