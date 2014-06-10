package com.intellij.seam.inspections;

import com.intellij.seam.model.xml.pages.Page;
import com.intellij.seam.resources.messages.PagesBundle;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PagesFileModelInspection extends BasicDomElementsInspection<Page> {

  public PagesFileModelInspection() {
    super(Page.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return PagesBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return PagesBundle.message("model.file.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "PagesFileModelInspection";
  }
}
