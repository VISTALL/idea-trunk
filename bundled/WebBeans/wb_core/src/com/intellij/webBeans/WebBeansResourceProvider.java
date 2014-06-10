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

package com.intellij.webBeans;

import com.intellij.javaee.StandardResourceProvider;
import com.intellij.javaee.ResourceRegistrar;

/**
 * @author Dmitry Avdeev
 */
public class WebBeansResourceProvider implements StandardResourceProvider{
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addStdResource("http://jboss.com/products/webBeans/components-2.0.xsd", "/resources/schemas/components-2.0.xsd", getClass());
  }
}
