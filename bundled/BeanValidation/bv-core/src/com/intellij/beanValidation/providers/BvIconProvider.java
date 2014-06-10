/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.providers;

import com.intellij.beanValidation.BVIcons;
import static com.intellij.beanValidation.constants.BvCommonConstants.BEAN_VALIDATION_CONFIG_FILENAME;
import static com.intellij.beanValidation.constants.BvCommonConstants.BEAN_VALIDATION_CONFIG_ROOT_TAG_NAME;
import com.intellij.beanValidation.model.xml.BvMappingsDomElement;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomIconProvider;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class BvIconProvider extends DomIconProvider implements DumbAware {
  public Icon getIcon(@NotNull DomElement element, int flags) {
    if (element instanceof BvMappingsDomElement) {
      return BVIcons.BEAN_VALIDATION_ICON;
    }
    return null;
  }

  @Nullable
  public Icon getIcon(@NotNull PsiElement element, int flags) {
    if (element instanceof XmlFile) {
      XmlFile xmlFile = (XmlFile)element;
      DomManager domManager = DomManager.getDomManager(element.getProject());
      final DomFileElement<BvMappingsDomElement> domFileElement = domManager.getFileElement((XmlFile)element, BvMappingsDomElement.class);
      if (domFileElement != null) {
        return BVIcons.BEAN_VALIDATION_ICON;
      } else {
        if (BEAN_VALIDATION_CONFIG_FILENAME.equals(xmlFile.getName())) {
          final XmlDocument document = xmlFile.getDocument();
          if (document != null) {
            final XmlTag rootTag = document.getRootTag();
            if (rootTag != null && BEAN_VALIDATION_CONFIG_ROOT_TAG_NAME.equals(rootTag.getName())) {
              return BVIcons.BEAN_VALIDATION_ICON;
            }
          }
        }
      }
    }
    return super.getIcon(element, flags);
  }
}
