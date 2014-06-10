package com.intellij.seam.actions;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.constants.SeamConstants;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.seam.utils.SeamCommonUtils;
import org.jetbrains.annotations.NotNull;

public class CreateSeamComponentsAction extends BaseCreateSeamAction {
  public static final Logger LOG = Logger.getInstance(CreateSeamComponentsAction.class.getName());

  public CreateSeamComponentsAction() {
    super(SeamBundle.message("seam.components.new.file"), SeamBundle.message("create.new.seam.components.file"), SeamIcons.SEAM_ICON);
  }

  @NotNull
  protected FileTemplate getTemplate(final Module module) {
    return SeamCommonUtils.chooseTemplate(module);
  }

  @NotNull
  protected String getFileName() {
    return SeamConstants.SEAM_CONFIG_FILENAME;
  }
}

