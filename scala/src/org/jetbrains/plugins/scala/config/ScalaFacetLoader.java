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

package org.jetbrains.plugins.scala.config;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class ScalaFacetLoader implements ApplicationComponent{

  public static final String PLUGIN_MODULE_ID = "PLUGIN_MODULE";


  public static ScalaFacetLoader getInstance() {
    return ApplicationManager.getApplication().getComponent(ScalaFacetLoader.class);
  }

  public ScalaFacetLoader() {
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(ScalaFacetType.INSTANCE);
  }

  public void disposeComponent() {
    FacetTypeRegistry instance = FacetTypeRegistry.getInstance();
    instance.unregisterFacetType(instance.findFacetType(ScalaFacet.ID));
  }

  @NotNull
  public String getComponentName() {
    return "ScalaFacetLoader";
  }


}
