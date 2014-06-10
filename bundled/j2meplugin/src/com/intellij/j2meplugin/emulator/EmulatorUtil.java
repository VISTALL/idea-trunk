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
package com.intellij.j2meplugin.emulator;

import com.intellij.j2meplugin.emulator.midp.uei.UnifiedEmulatorType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nullable;

/**
 * User: anna
 * Date: Oct 1, 2004
 */
public class EmulatorUtil {
  private EmulatorUtil() {
  }

  @Nullable
  public static EmulatorType getEmulatorTypeByName(String name) {
    EmulatorType[] knownEmulatorTypes = ApplicationManager.getApplication().getExtensions(EmulatorType.EMULATOR_TYPE_EXTENSION);
    for (int i = 0; knownEmulatorTypes != null && i < knownEmulatorTypes.length; i++) {
      if (Comparing.strEqual(knownEmulatorTypes[i].getName(), name)) return knownEmulatorTypes[i];
    }
    return null;
  }

  @Nullable
  public static EmulatorType getValidEmulatorType(String home){
    EmulatorType defaultEmulatorType = null;
    EmulatorType[] knownEmulatorTypes = ApplicationManager.getApplication().getExtensions(EmulatorType.EMULATOR_TYPE_EXTENSION);
    for (EmulatorType emulatorType : knownEmulatorTypes) {
      if (emulatorType instanceof UnifiedEmulatorType) {
        defaultEmulatorType = emulatorType;
        continue;
      }
      if (emulatorType.isValidHomeDirectory(home)) return emulatorType;
    }
    return defaultEmulatorType != null && defaultEmulatorType.isValidHomeDirectory(home) ? defaultEmulatorType : null;
  }

  @Nullable
  public static String findFirstJavaSdk(){
    ProjectJdkTable table = ProjectJdkTable.getInstance();
    final Sdk[] allJdks = table.getAllJdks();
    for (Sdk jdk : allJdks) {
      if (Comparing.equal(jdk.getSdkType(), JavaSdk.getInstance())){
        return jdk.getName();
      }
    }
    return null;
  }
}
