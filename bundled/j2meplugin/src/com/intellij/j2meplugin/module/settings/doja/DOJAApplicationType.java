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
package com.intellij.j2meplugin.module.settings.doja;

import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.MobileSettingsConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;

/**
 * User: anna
 * Date: Sep 19, 2004
 */
public class DOJAApplicationType extends MobileApplicationType {
  @NonNls
  public static final String APPLICATION_NAME = "AppName";
  @NonNls
  public static final String PACKAGE_URL = "PackageUrl";
  @NonNls
  public static final String APPLICATION_SIZE = "AppSize";
  @NonNls
  public static final String APPLICATION_CLASS = "AppClass";
  @NonNls
  public static final String LAST_MODIFIED = "LastModified";
  @NonNls
  public static final String APP_VER = "AppVersion";
  @NonNls
  public static final String CONFIGURATION_VER = "ConfigurationVer";
  @NonNls
  public static final String PROFILE_VER = "ProfileVer";
  @NonNls
  public static final String SP_SIZE = "SPsize";
  @NonNls
  public static final String APP_PARAMS = "AppParam";
  @NonNls
  public static final String USE_NETWORK = "UseNetwork";
  @NonNls
  public static final String TARGET_DEVICE = "TargetDevice";
  @NonNls
  public static final String LAUNCH_AT = "LaunchAt";
  @NonNls
  public static final String APP_TRACE = "AppTrace";
  @NonNls
  public static final String DRAW_AREA = "DrawArea";
  @NonNls
  public static final String GET_UTN = "GetUtn";
  @NonNls
  public static final String NAME = "DoJa";
  public static final String[] ADDITIONAL_SETTINGS = {
    APP_VER,
    CONFIGURATION_VER,
    PROFILE_VER,
    SP_SIZE,
    APP_PARAMS,
    USE_NETWORK,
    TARGET_DEVICE,
    LAUNCH_AT,
    APP_TRACE,
    DRAW_AREA,
    GET_UTN
  };
  private final ArrayList<String> ourFields = new ArrayList<String>();


  public DOJAApplicationType() {
    ourFields.add(APPLICATION_NAME);
    ourFields.add(PACKAGE_URL);
    ourFields.add(APPLICATION_SIZE);
    ourFields.add(APPLICATION_CLASS);
    ourFields.add(LAST_MODIFIED);
    for (String setting : ADDITIONAL_SETTINGS) {
      ourFields.add(setting);
    }
  }

  @SuppressWarnings({"ConstantConditions"})
  public static DOJAApplicationType getInstance() {
    final MobileApplicationType[] applicationTypes = ApplicationManager.getApplication().getExtensions(MOBILE_APPLICATION_TYPE);
    for (MobileApplicationType applicationType : applicationTypes) {
      if (applicationType instanceof DOJAApplicationType) {
        return (DOJAApplicationType)applicationType;
      }
    }
    return null;
  }

  public String getName() {
    return NAME;
  }

  public String getExtension() {
    return "jam";
  }

  public String getSeparator() {
    return "=";
  }

  public String getJarSizeSettingName() {
    return APPLICATION_SIZE;
  }

  public String getBaseClassName() {
    return "com.nttdocomo.ui.IApplication";
  }

  public String getJarUrlSettingName() {
    return PACKAGE_URL;
  }

  public boolean isUserField(String name) {
    return !ourFields.contains(name);
  }

  public String createConfigurationByClass(String className) {
    return APPLICATION_CLASS + "=" + className;
  }

  public Class<? extends MobileModuleSettings> getClassType() {
    return DOJASettings.class;
  }

  public MobileSettingsConfigurable createConfigurable(final Project project, final Module module, final MobileModuleSettings settings) {
    return new DOJASettingsConfigurable(module, settings, project);
  }

  public MobileModuleSettings createTempSettings(final J2MEModuleBuilder builder) {
    final DOJASettings settings = new DOJASettings();
    settings.initSettings(builder);
    return settings;
  }

}
