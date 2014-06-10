package com.advancedtools.webservices.utils.ui;

import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.WSBundle;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author maxim
 * Date: 25.12.2005
 */
public abstract class GenerateDialogBase extends MyDialogWrapper {
  public GenerateDialogBase(Project project) {
    super(project);
  }

  protected void configureTypeMappingVersion(JComboBox typeMappingVersion,JLabel typeMappingText) {
    configureComboBox(typeMappingVersion, Arrays.asList(AxisUtil.TYPE_MAPPING_1_1,AxisUtil.TYPE_MAPPING_1_2));
    doInitFor(typeMappingText,typeMappingVersion,'v');
  }

  public static String validatePackagePrefix(String packagePrefix) {
    if (!ValidationUtils.validatePackageName(packagePrefix)) {
      return WSBundle.message("package.prefix.is.not.valid.validation.message");
    }

    return null;
  }
}
