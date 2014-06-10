package com.intellij.seam.inspections;

import com.intellij.seam.model.xml.pages.Pages;
import com.intellij.seam.resources.messages.PagesBundle;
import com.intellij.seam.resources.SeamInspectionBundle;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PagesModelInspection extends BasicDomElementsInspection<Pages> {

  public PagesModelInspection() {
    super(Pages.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return SeamInspectionBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return PagesBundle.message("model.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "PagesModelInspection";
  }
}

