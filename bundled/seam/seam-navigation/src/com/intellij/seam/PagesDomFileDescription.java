package com.intellij.seam;

import com.intellij.seam.model.xml.pages.Pages;

/**
 * User: Sergey.Vasiliev
 */
public class PagesDomFileDescription extends BasicPagesDomFileDescription<Pages> {
  public PagesDomFileDescription() {
    super(Pages.class, "pages");
  }
}

