/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.tomcat;

import com.intellij.javaee.appServerIntegrations.DefaultPersistentData;
import com.intellij.openapi.util.InvalidDataException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

public class TomcatPersistentData extends DefaultPersistentData {
  public String CATALINA_HOME = "";
  public String CATALINA_BASE = "";
  public String VERSION = VERSION50;

  @NonNls public static final String VERSION40 = "4.0";
  @NonNls public static final String VERSION50 = "5.x";
  @NonNls public static final String VERSION60 = "6.0";

  public TomcatPersistentData() {
    CATALINA_HOME = TomcatUtil.getDefaultLocation();
    CATALINA_BASE = "";
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    if (VERSION.startsWith("5.0")) {
      VERSION = TomcatPersistentData.VERSION50;
    }
  }
}