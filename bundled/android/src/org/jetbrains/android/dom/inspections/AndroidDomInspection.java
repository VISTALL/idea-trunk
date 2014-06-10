package org.jetbrains.android.dom.inspections;

import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.android.dom.AndroidDomElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class AndroidDomInspection extends BasicDomElementsInspection<AndroidDomElement> {
  public AndroidDomInspection() {
    super(AndroidDomElement.class);
  }

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return "Android";
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return "Android Resources Validation";
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "AndroidDomInspection";
  }
}