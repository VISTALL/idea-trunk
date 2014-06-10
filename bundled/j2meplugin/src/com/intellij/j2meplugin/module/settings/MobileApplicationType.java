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
package com.intellij.j2meplugin.module.settings;

import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;


/**
 * User: anna
 * Date: Sep 1, 2004
 */
public abstract class MobileApplicationType {
  public static final ExtensionPointName<MobileApplicationType> MOBILE_APPLICATION_TYPE = ExtensionPointName.create("J2ME.com.intellij.applicationType");

  @NonNls
  public abstract String getName();

  @NonNls
  public abstract String getExtension();

  public abstract String getSeparator();

  @NonNls
  public abstract String getJarSizeSettingName();

  @NonNls
  public abstract String getBaseClassName();

  public boolean isUserParametersEnable() {
    return false;
  }

  public String getPresentableClassName() {
    return getBaseClassName().substring(getBaseClassName().lastIndexOf(".") + 1);
  }
  @NonNls
  public abstract String getJarUrlSettingName();

  public abstract boolean isUserField(String name);

  public abstract String createConfigurationByClass(String className);

  public abstract Class<? extends MobileModuleSettings> getClassType();

  public abstract MobileSettingsConfigurable createConfigurable(final Project project, final Module module, final MobileModuleSettings settings);

  public abstract MobileModuleSettings createTempSettings(J2MEModuleBuilder builder);
}
