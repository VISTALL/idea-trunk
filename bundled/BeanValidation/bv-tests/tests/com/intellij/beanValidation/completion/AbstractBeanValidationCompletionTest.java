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

package com.intellij.beanValidation.completion;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.beanValidation.AbstractBeanValidationTestCase;
import org.jetbrains.annotations.NonNls;

/**
 * @author Konstantin Bulenkov
 */
public abstract class AbstractBeanValidationCompletionTest extends AbstractBeanValidationTestCase<WebModuleFixtureBuilder> {

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    addBeanValidationJars(moduleBuilder);
  }
  
  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "completion/";
  }
}
