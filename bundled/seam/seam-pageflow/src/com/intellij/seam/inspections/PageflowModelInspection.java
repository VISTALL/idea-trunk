package com.intellij.seam.inspections;

import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.seam.resources.messages.PageflowBundle;
import com.intellij.seam.resources.SeamInspectionBundle;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowModelInspection extends BasicDomElementsInspection<PageflowDefinition> {

  public PageflowModelInspection() {
    super(PageflowDefinition.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return SeamInspectionBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return PageflowBundle.message("model.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "PageflowModelInspection";
  }
}
