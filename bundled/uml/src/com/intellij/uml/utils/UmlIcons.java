/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public interface UmlIcons {
  Icon UML_ICON = IconLoader.getIcon("/resources/icons/uml.png");
  Icon DEPENDENCY_ICON = IconLoader.getIcon("/resources/icons/dependencies.png");
  Icon CAMEL_CASE_ICON = IconLoader.getIcon("/resources/icons/camelcase.png");
  Icon FIELD_ICON = IconLoader.getIcon("/resources/icons/field.png");
  Icon METHOD_ICON = IconLoader.getIcon("/resources/icons/method.png");
  Icon CONSTRUCTOR_ICON = IconLoader.getIcon("/resources/icons/constructor.png");
  Icon COLOR_MGR_ICON = IconLoader.getIcon("/resources/icons/colormanager.png");
  Icon HELP_ICON = IconLoader.getIcon("/resources/icons/help.png");
  Icon INNER_CLASS_ICON = IconLoader.getIcon("/resources/icons/innerclass.png");
  Icon EDGE_MODE_ICON = IconLoader.getIcon("/resources/icons/edgemode.png");
  Icon SELECTED = IconLoader.getIcon("/resources/icons/selected.png");
  Icon DESELECTED = IconLoader.getIcon("/resources/icons/deselected.png");
  Icon VISIBILITY = IconLoader.getIcon("/resources/icons/visibility.png");
  Icon PROPERTY = IconLoader.getIcon("/resources/icons/property.png");
}
