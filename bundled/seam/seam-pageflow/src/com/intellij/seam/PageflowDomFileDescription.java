package com.intellij.seam;

import com.intellij.seam.constants.SeamNamespaceConstants;
import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.util.xml.DomFileDescription;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowDomFileDescription extends DomFileDescription<PageflowDefinition> {

  public PageflowDomFileDescription() {
    super(PageflowDefinition.class, "pageflow-definition");
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(SeamNamespaceConstants.PAGEFLOW_NAMESPACE_KEY, SeamNamespaceConstants.PAGEFLOW_NAMESPACE);
  }


}
