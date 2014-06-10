package com.intellij.seam.actions;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.constants.SeamConstants;
import com.intellij.seam.resources.SeamBundle;
import org.jetbrains.annotations.NotNull;


public class CreateSeamPagesAction extends BaseCreateSeamAction {
  public static final Logger LOG = Logger.getInstance(CreateSeamComponentsAction.class.getName());

  public CreateSeamPagesAction() {
    super(SeamBundle.message("seam.pages.new.file"), SeamBundle.message("create.new.seam.pages.file"), SeamIcons.SEAM_ICON);
  }

  @NotNull
  protected FileTemplate getTemplate(final Module module) {
    return FileTemplateManager.getInstance().getJ2eeTemplate(SeamConstants.FILE_TEMPLATE_NAME_PAGES_2_0);
  }

  @NotNull
  protected String getFileName() {
    return SeamConstants.SEAM_PAGES_FILENAME;
  }

   protected boolean isAllowedInSourceDir() {
    return false;
  }
}