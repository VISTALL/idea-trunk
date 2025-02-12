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

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public interface BVIcons {
  Icon LIBRARY_CONSTRAINT = IconLoader.getIcon("/resources/icons/libAnnotation.png");
  Icon BEAN_VALIDATION_ICON = IconLoader.getIcon("/resources/icons/beanValidation.png");
  Icon CONSTRAINT_TYPE = IconLoader.getIcon("/resources/icons/constraints.png"); 
  Icon CONSTRAINT_VALIDATOR_TYPE = IconLoader.getIcon("/resources/icons/constraintValidator.png");
  Icon LIBRARY_CONSTRAINT_VALIDATION_ICON = IconLoader.getIcon("/resources/icons/libValidator.png");
}
