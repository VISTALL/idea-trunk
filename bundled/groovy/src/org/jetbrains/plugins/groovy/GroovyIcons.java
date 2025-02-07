/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author ilyas
 */
public interface GroovyIcons {

  Icon FILE_TYPE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_fileType.png");
  Icon GROOVY_ICON_16x16 = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_16x16.png");
  Icon GROOVY_ICON_32x32 = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_32x32.png");
  Icon CLASS = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/class.png");
  Icon ABSTRACT_CLASS = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/abstractClass.png");
  Icon INTERFACE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/interface.png");
  Icon ANNOTATION_TYPE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/annotationType.png");
  Icon ENUM = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/enum.png");
  Icon PROPERTY = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/property.png");
  Icon METHOD = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/method.png");
  Icon DYNAMIC = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/dynamicProperty.png");
  Icon DEF = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/def.png");
  Icon FIELD = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/field.png");
  Icon VARIABLE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/field.png");

  Icon NO_GROOVY_SDK = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/no_groovy_sdk.png");
  Icon GROOVY_SDK = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_sdk.png");

  Icon DYNAMIC_PROPERTY_TOOL_WINDOW_ICON = IconLoader.getIcon("/org/jetbrains/plugins/groovy/images/dynamicProperty.png");

}
