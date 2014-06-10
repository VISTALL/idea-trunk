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

package org.jetbrains.plugins.grails;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.completion.handlers.ControllerReferenceInsertHandler;
import org.jetbrains.plugins.groovy.extensions.completion.InsertHandlerRegistry;

/**
 * @author ilyas
 */
public class GrailsLoader implements ApplicationComponent{

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Grails loader";
  }

  public void initComponent() {
    InsertHandlerRegistry handlerRegistry = InsertHandlerRegistry.getInstance();
    handlerRegistry.registerSpecificInsertHandler(new ControllerReferenceInsertHandler());
  }

  public void disposeComponent() {
  }
}
