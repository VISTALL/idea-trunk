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
package org.jetbrains.idea.tomcat;

import com.intellij.javaee.appServerIntegrations.*;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;

public class TomcatApplicationServerHelper implements ApplicationServerHelper {
  @NonNls protected static final String COMMON_DIR = "common";
  @NonNls protected static final String LIB_DIR = "lib";
  @NonNls protected static final String SHARED_DIR = "shared";

  public ApplicationServerInfo getApplicationServerInfo(ApplicationServerPersistentData persistentData)
    throws CantFindApplicationServerJarsException {
    TomcatPersistentData tomcatPersistentData = (TomcatPersistentData)persistentData;
    File tomcatHome = new File(tomcatPersistentData.CATALINA_HOME.replace('/', File.separatorChar)).getAbsoluteFile();
    File tomcatLib;
    if (TomcatPersistentData.VERSION60.equals(tomcatPersistentData.VERSION)) {
      tomcatLib = new File(tomcatHome, LIB_DIR);
    }
    else {
      tomcatLib = new File(new File(tomcatHome, COMMON_DIR), LIB_DIR);
    }

    if (!tomcatLib.isDirectory()) {
      throw new CantFindApplicationServerJarsException(TomcatBundle.message("message.text.cant.find.directory", tomcatLib.getAbsolutePath()));
    }

    ArrayList<File> files = new ArrayList<File>();
    File[] filesInLib = tomcatLib.listFiles();
    if (filesInLib != null) {
      for (int i = 0; i < filesInLib.length; i++) {
        File file = filesInLib[i];
        if (file.isFile()) {
          files.add(file);
        }
      }
    }

    File tomcatShared = new File(new File(tomcatHome, SHARED_DIR), LIB_DIR);
    if (tomcatShared.isDirectory()) {
      final File[] shared = tomcatShared.listFiles();
      if (shared != null) {
        for (int i = 0; i < shared.length; i++) {
          File file = shared[i];
          files.add(file);
        }
      }
    }

    String version = tomcatPersistentData.VERSION;
    if (version == null) {
      version = "";
    }
    else {
      version = version.substring(0, 1);
    }
    return new ApplicationServerInfo(files.toArray(new File[files.size()]), TomcatBundle.message("default.application.server.name", version));

  }

  public ApplicationServerPersistentData createPersistentDataEmptyInstance() {
    return new TomcatPersistentData();
  }

  public ApplicationServerPersistentDataEditor createConfigurable() {
    return new TomcatDataEditor();
  }
}