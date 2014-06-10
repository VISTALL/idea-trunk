/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.spring.facet.SpringSchemaVersion;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringBeansTemplatesFactory implements FileTemplateGroupDescriptorFactory, ApplicationComponent {

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(SpringBundle.message("spring"), SpringIcons.FILESET);

    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_1_DTD.getTemplateName(), SpringIcons.CONFIG_FILE));
    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_2_DTD.getTemplateName(), SpringIcons.CONFIG_FILE));
    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_2_Schema.getTemplateName(), SpringIcons.CONFIG_FILE));
    return group;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "SpringBeansTemplatesFactory";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}
