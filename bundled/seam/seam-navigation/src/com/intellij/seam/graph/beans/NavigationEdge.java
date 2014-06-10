package com.intellij.seam.graph.beans;

import com.intellij.psi.xml.XmlElement;
import com.intellij.seam.model.xml.pages.Navigation;
import com.intellij.seam.model.xml.pages.PagesViewIdOwner;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class NavigationEdge extends BasicPagesEdge<Navigation> {
  public NavigationEdge(@NotNull final BasicPagesNode source, @NotNull final BasicPagesNode target, PagesViewIdOwner viewId, @NotNull final Navigation parentElement) {
    super(source, target, viewId, parentElement);
  }

  public String getName() {
    final XmlElement xmlElement = getParentElement().getFromAction().getXmlElement();
    if (xmlElement != null) return xmlElement.getText();

    return super.getName();
  }
}
