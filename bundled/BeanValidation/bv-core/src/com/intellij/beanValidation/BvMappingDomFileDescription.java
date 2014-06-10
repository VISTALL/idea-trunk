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

import static com.intellij.beanValidation.constants.BvCommonConstants.BEAN_VALIDATION_CONSTRAINT_MAPPINGS_ROOT_TAG;
import static com.intellij.beanValidation.constants.BvNamespaceConstants.*;
import com.intellij.beanValidation.model.xml.ConstraintMappings;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BvMappingDomFileDescription extends DomFileDescription<ConstraintMappings> {

  public BvMappingDomFileDescription() {
    super(ConstraintMappings.class, BEAN_VALIDATION_CONSTRAINT_MAPPINGS_ROOT_TAG);
  }

  protected void initializeFileDescription() {
    registerNamespacePollicies();

    registerImplementations();
  }

  private void registerImplementations() {
  }

  private void registerNamespacePollicies() {
    registerNamespacePolicy(MAPPING_NAMESPACE_KEY, MAPPING_NAMESPACE);
  }

  @NotNull
  @Override
  public List<String> getAllowedNamespaces(@NotNull final String namespaceKey, @NotNull final XmlFile file) {
    final List<String> stringList = super.getAllowedNamespaces(namespaceKey, file);
    return stringList.isEmpty() ? Collections.singletonList(namespaceKey) : stringList;
  }

}
