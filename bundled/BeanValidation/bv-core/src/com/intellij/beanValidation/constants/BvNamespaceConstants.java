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

package com.intellij.beanValidation.constants;

import org.jetbrains.annotations.NonNls;

/**
 * @author Konstantin Bulenkov
 */
public interface BvNamespaceConstants {
  @NonNls String MAPPING_NAMESPACE_KEY = "BeanValidation mapping namespace key";
  @NonNls String MAPPING_NAMESPACE = "http://jboss.org/xml/ns/javax/validation/mapping";

  @NonNls String CONFIG_NAMESPACE_KEY = "BeanValidation Config namespace key";
  @NonNls String CONFIG_NAMESPACE = "http://jboss.org/xml/ns/javax/validation/configuration";
}
