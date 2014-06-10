/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

// Generated on Wed Apr 05 15:29:47 MSD 2006
// DTD/Schema  :    tiles-config_1_3.dtd

package com.intellij.struts.dom.tiles;

import org.jetbrains.annotations.NonNls;
import com.intellij.util.xml.NamedEnum;

/**
 * tiles-config_1_3.dtd:ContentType enumeration.
 * Type ContentType documentation
 * <pre>
 *  A "ContentType" is the content type of an attribute passed to a tile
 *      component.
 * </pre>
 */
public enum Content implements NamedEnum {
  DEFINITION("definition"),
  PAGE("page"),
  STRING("string"),
  TEMPLATE("template");

  private final String value;

  private Content(@NonNls String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
