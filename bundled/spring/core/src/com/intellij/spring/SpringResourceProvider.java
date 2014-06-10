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

package com.intellij.spring;

import com.intellij.javaee.StandardResourceProvider;
import com.intellij.javaee.ResourceRegistrar;

/**
 * @author Dmitry Avdeev
 */
public class SpringResourceProvider implements StandardResourceProvider {
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addStdResource("http://www.springframework.org/dtd/spring-beans.dtd", "/resources/schemas/spring-beans.dtd", getClass());
    registrar.addStdResource("http://www.springframework.org/dtd/spring-beans-2.0.dtd", "/resources/schemas/spring-beans-2.0.dtd", getClass());

    registrar.addStdResource("http://www.springframework.org/schema/beans", "/resources/schemas/spring-beans-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/aop", "/resources/schemas/spring-aop-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/jee", "/resources/schemas/spring-jee-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/lang", "/resources/schemas/spring-lang-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/tool", "/resources/schemas/spring-tool-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/tx", "/resources/schemas/spring-tx-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/util", "/resources/schemas/spring-util-2.0.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/context", "/resources/schemas/spring-context-2.1.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/context", "/resources/schemas/spring-context-2.5.xsd", getClass());
    registrar.addStdResource("http://www.springframework.org/schema/jms", "/resources/schemas/spring-jms-2.5.xsd", getClass());
    
  }
}
