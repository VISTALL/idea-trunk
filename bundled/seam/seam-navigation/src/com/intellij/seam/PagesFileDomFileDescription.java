package com.intellij.seam;

import com.intellij.seam.model.xml.pages.Page;

/**
 * User: Sergey.Vasiliev
 */
public class PagesFileDomFileDescription extends BasicPagesDomFileDescription<Page> {
   public PagesFileDomFileDescription() {
    super(Page.class, "page");
  }
}

