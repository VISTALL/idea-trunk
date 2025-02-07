/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * @author ilyas
 */
public class ScalaFacetListener extends FacetManagerAdapter implements ModuleComponent {
  private MessageBusConnection myConnection;

  private Module myModule;

  public ScalaFacetListener(Module module) {
    myModule = module;
  }

  public void initComponent() {
    myConnection = myModule.getMessageBus().connect();
    myConnection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
      public void facetAdded(@NotNull final Facet facet) {
      }

      public void facetRemoved(@NotNull Facet facet) {
        if (facet.getTypeId() == ScalaFacet.ID) {
          //todo do somethig
        }
      }
    });
  }

  public void disposeComponent() {
    myConnection.disconnect();
  }

  @NotNull
  public String getComponentName() {
    return "ScalaFacetListener";
  }

  public void projectOpened() {
    // called when myProject is opened
  }

  public void projectClosed() {
    // called when myProject is being closed
  }

  public void moduleAdded() {
    // Invoked when the module corresponding to this component instance has been completely
    // loaded and added to the myProject.
  }
}
