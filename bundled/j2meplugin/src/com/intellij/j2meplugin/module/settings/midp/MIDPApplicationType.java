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
package com.intellij.j2meplugin.module.settings.midp;

import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.MobileSettingsConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;

/**
 * User: anna
 * Date: Sep 2, 2004
 */
public class MIDPApplicationType extends MobileApplicationType {
  @NonNls
  public static final String MIDLET_NAME = "MIDlet-Name";
  @NonNls
  public static final String MIDLET_JAR_SIZE_NAME = "MIDlet-Jar-Size";
  @NonNls
  public static final String MIDLET_JAR_URL = "MIDlet-Jar-URL";
  @NonNls
  public static final String MIDLET_VERSION = "MIDlet-Version";
  @NonNls
  public static final String MIDLET_VENDOR = "MIDlet-Vendor";
  @NonNls
  public static final String MIDLET_CONFIGURATION = "MicroEdition-Configuration";
  @NonNls
  public static final String MIDLET_PROFILE = "MicroEdition-Profile";

  @NonNls
  public static final String MIDLET_PREFIX = "MIDlet-";
  @NonNls
  public static final String MIDLET_DESCRIPTION = "MIDlet-Description";
  @NonNls
  public static final String MIDLET_INFO_URL = "MIDlet-Info-URL";
  @NonNls
  public static final String MIDLET_DELETE_CONFIRM = "MIDlet-Delete-Confirm";
  @NonNls
  public static final String MIDLET_INSTALL_NOTIFY = "MIDlet-Install-Notify";
  @NonNls
  public static final String MIDLET_DATA_SIZE = "MIDlet-Data-Size";
  @NonNls
  public static final String MIDLET_ICON = "MIDlet-Icon";

  @NonNls
  public static final String NAME = "MIDP";

  @SuppressWarnings({"ConstantConditions"})
  public static MIDPApplicationType getInstance() {
    final MobileApplicationType[] applicationTypes = ApplicationManager.getApplication().getExtensions(MOBILE_APPLICATION_TYPE);
    for (MobileApplicationType applicationType : applicationTypes) {
      if (applicationType instanceof MIDPApplicationType) {
        return (MIDPApplicationType)applicationType;
      }
    }
    return null;
  }

  public String getName() {
    return NAME;
  }

  public String getExtension() {
    return "jad";
  }

  public String getSeparator() {
    return ":";
  }

  public String getJarSizeSettingName() {
    return MIDLET_JAR_SIZE_NAME;
  }

  public String getBaseClassName() {
    return "javax.microedition.midlet.MIDlet";
  }

  public boolean isUserParametersEnable() {
    return true;
  }

  public String getJarUrlSettingName() {
    return MIDLET_JAR_URL;
  }

  public boolean isUserField(String name) {
    return !name.startsWith(MIDLET_PREFIX) && !name.equals(MIDLET_PROFILE) && !name.equals(MIDLET_CONFIGURATION);
  }

  public String createConfigurationByClass(String className) {
    return MIDLET_PREFIX + "1: " + className + ",," + className;
  }

  public Class<? extends MobileModuleSettings> getClassType() {
    return MIDPSettings.class;
  }

  public MobileSettingsConfigurable createConfigurable(final Project project, final Module module, final MobileModuleSettings settings) {
    return new MIDPSettingsConfigurable(module, settings, project);
  }

  public MobileModuleSettings createTempSettings(final J2MEModuleBuilder builder) {
    final MIDPSettings settings = new MIDPSettings();
    settings.initSettings(builder);
    return settings;
  }

}
