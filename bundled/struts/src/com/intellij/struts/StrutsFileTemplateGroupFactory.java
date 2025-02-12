/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import static com.intellij.struts.StrutsIcons.OVERLAY_ICON_OFFSET_X;
import static com.intellij.struts.StrutsIcons.OVERLAY_ICON_OFFSET_Y;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NonNls;

/**
 * Adds Struts related file templates.
 */
public class StrutsFileTemplateGroupFactory implements FileTemplateGroupDescriptorFactory {

  @NonNls
  public static final String STRUTS_CONFIG_XML = "struts-config.xml";
  @NonNls
  public static final String TILES_DEFS_XML = "tiles-defs.xml";
  @NonNls
  public static final String VALIDATION_XML = "validation.xml";
  @NonNls
  public static final String VALIDATOR_RULES_XML = "validator-rules.xml";
  @NonNls
  public static final String MESSAGE_RESOURCES_PROPERTIES = "MessageResources.properties";

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor groupDescriptor = new FileTemplateGroupDescriptor("Struts templates",
                                                                                        StrutsIcons.ACTION_ICON);
    LayeredIcon strutsIcon = new LayeredIcon(2);
    strutsIcon.setIcon(StdFileTypes.XML.getIcon(), 0);
    strutsIcon.setIcon(StrutsIcons.ACTION_SMALL_ICON, 1, OVERLAY_ICON_OFFSET_X, OVERLAY_ICON_OFFSET_Y);
    groupDescriptor.addTemplate(new FileTemplateDescriptor(STRUTS_CONFIG_XML, strutsIcon));

    LayeredIcon tilesIcon = new LayeredIcon(2);
    tilesIcon.setIcon(StdFileTypes.XML.getIcon(), 0);
    tilesIcon.setIcon(StrutsIcons.TILES_SMALL_ICON, 1, OVERLAY_ICON_OFFSET_X, OVERLAY_ICON_OFFSET_Y);
    groupDescriptor.addTemplate(new FileTemplateDescriptor(TILES_DEFS_XML, tilesIcon));


    LayeredIcon validatorIcon = new LayeredIcon(2);
    validatorIcon.setIcon(StdFileTypes.XML.getIcon(), 0);
    validatorIcon.setIcon(StrutsIcons.VALIDATOR_SMALL_ICON, 1, OVERLAY_ICON_OFFSET_X, OVERLAY_ICON_OFFSET_Y);
    groupDescriptor.addTemplate(new FileTemplateDescriptor(VALIDATION_XML, validatorIcon));
    groupDescriptor.addTemplate(new FileTemplateDescriptor(VALIDATOR_RULES_XML, validatorIcon));

    groupDescriptor.addTemplate(MESSAGE_RESOURCES_PROPERTIES);
    return groupDescriptor;
  }

}