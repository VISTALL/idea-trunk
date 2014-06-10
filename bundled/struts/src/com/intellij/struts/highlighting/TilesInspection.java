package com.intellij.struts.highlighting;

import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class TilesInspection extends BasicDomElementsInspection<TilesDefinitions> {

  public TilesInspection() {
    super(TilesDefinitions.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return "Struts 1";
  }

  @NotNull
  @Override
  public String[] getGroupPath() {
    return new String[]{"Struts", getGroupDisplayName()};
  }

  @NotNull
  public String getDisplayName() {
    return "Tiles inspection";
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "StrutsTilesInspection";
  }
}
