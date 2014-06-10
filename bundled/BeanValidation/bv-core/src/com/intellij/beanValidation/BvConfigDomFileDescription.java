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

package com.intellij.beanValidation;

import static com.intellij.beanValidation.constants.BvCommonConstants.BEAN_VALIDATION_CONFIG_ROOT_TAG_NAME;
import static com.intellij.beanValidation.constants.BvNamespaceConstants.CONFIG_NAMESPACE;
import static com.intellij.beanValidation.constants.BvNamespaceConstants.CONFIG_NAMESPACE_KEY;
import com.intellij.beanValidation.model.xml.ValidationConfig;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BvConfigDomFileDescription extends DomFileDescription<ValidationConfig> {

  public BvConfigDomFileDescription() {
    super(ValidationConfig.class, BEAN_VALIDATION_CONFIG_ROOT_TAG_NAME);
  }

  protected void initializeFileDescription() {
    registerNamespacePollicies();

    registerImplementations();
  }

  private void registerImplementations() {
  }

  private void registerNamespacePollicies() {
    registerNamespacePolicy(CONFIG_NAMESPACE_KEY, CONFIG_NAMESPACE);
  }

  @NotNull
  @Override
  public List<String> getAllowedNamespaces(@NotNull final String namespaceKey, @NotNull final XmlFile file) {
    final List<String> stringList = super.getAllowedNamespaces(namespaceKey, file);
    return stringList.isEmpty() ? Collections.singletonList(namespaceKey) : stringList;
  }
}
