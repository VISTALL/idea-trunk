package com.intellij.struts.highlighting;

import com.intellij.struts.StrutsBundle;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class StrutsInspection extends BasicDomElementsInspection<StrutsConfig> {

  public StrutsInspection() {
    super(StrutsConfig.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return StrutsBundle.message("inspections.group.name");
  }

  @NotNull
  @Override
  public String[] getGroupPath() {
    return new String[]{"Struts", getGroupDisplayName()};
  }

  @NotNull
  public String getDisplayName() {
    return StrutsBundle.message("struts.model.inspection");
  }

  protected boolean shouldCheckResolveProblems(final GenericDomValue value) {
    final String stringValue = value.getStringValue();
    if (stringValue != null) {
      return stringValue.indexOf('{') < 0;      
    }
    return true;
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "StrutsInspection";
  }
}