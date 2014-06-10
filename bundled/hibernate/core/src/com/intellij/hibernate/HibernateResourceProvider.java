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

package com.intellij.hibernate;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;

/**
 * @author Dmitry Avdeev
 */
public class HibernateResourceProvider implements StandardResourceProvider {
  
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addStdResource("http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd",
                                                         "/resources/schemas/hibernate-mapping-3.0.dtd", getClass());
    registrar.addStdResource("http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd",
                                                         "/resources/schemas/hibernate-configuration-3.0.dtd", getClass());
    
  }
}
