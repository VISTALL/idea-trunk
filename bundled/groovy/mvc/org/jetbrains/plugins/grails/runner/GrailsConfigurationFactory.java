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

package org.jetbrains.plugins.grails.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class GrailsConfigurationFactory extends ConfigurationFactory {
  private final String myTemplateName;
  private final String myCmdLine;

  public GrailsConfigurationFactory(ConfigurationType configurationType, String templateName, String cmdLine) {
    super(configurationType);
    myTemplateName = templateName;
    myCmdLine = cmdLine;
  }

  public RunConfiguration createTemplateConfiguration(Project project) {
    return new GrailsRunConfiguration(this, project, myTemplateName, myCmdLine);
  }

}