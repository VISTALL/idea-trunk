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
package com.intellij.j2meplugin.emulator.midp.wtk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: anna
 * Date: Sep 6, 2004
 */
public class ConfigurationUtil {
  @NonNls private static final String TITLE_VERSIONED_PROPERTY = "TITLE_VERSIONED";

  private ConfigurationUtil() {
  }

  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  private static Properties getWTKEmulatorProperties(String homeDir) {
    Properties properties = new Properties();
    final VirtualFile homeDirectory = LocalFileSystem.getInstance().findFileByPath(homeDir.replace(File.separatorChar, '/'));
    if (homeDirectory == null) {
      return properties;
    }
    VirtualFile ktoolsZIP = homeDirectory.findFileByRelativePath("wtklib/ktools.zip");
    if (ktoolsZIP == null) {
      return properties;
    }
    VirtualFile ktoolsZipFileContent = JarFileSystem.getInstance().findFileByPath(ktoolsZIP.getPath() + JarFileSystem.JAR_SEPARATOR);
    if (ktoolsZipFileContent == null) {
      return properties;
    }
    ZipFile i18PropertiesZIPFile;
    try {
      i18PropertiesZIPFile = JarFileSystem.getInstance().getJarFile(ktoolsZipFileContent);
    }
    catch (IOException e) {
      return properties;
    }
    if (i18PropertiesZIPFile == null) {
      return properties;
    }
    try {
      ZipEntry entry = i18PropertiesZIPFile.getEntry("I18N.properties");
      if (entry == null) {
        return properties;
      }
      InputStream is = i18PropertiesZIPFile.getInputStream(entry);
      properties.load(is);
      is.close();
      return properties;
    }
    catch (IOException e) {
      return null;
    }
  }

  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  public static Properties getApiSettings(String homeDir) {
    Properties properties = new Properties();
    try {
      ZipFile zipFile = new ZipFile(new File(homeDir + File.separator + "wtklib" + File.separator + "ktools.zip"));
      ZipEntry entry = zipFile.getEntry("com/sun/kvem/toolbar/ApiSettings.properties");
      if (entry == null) {
        return properties;
      }
      InputStream is = zipFile.getInputStream(entry);
      properties.load(is);
      is.close();
      return properties;
    }
    catch (IOException e) {
      return null;
    }
  }

  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  public static String[] getDefaultApiPath(String homePath) {
    final Properties apiSettings = getApiSettings(homePath);
    if (apiSettings == null || apiSettings.isEmpty()) {
      return null;
    }
    final String defaultConf = apiSettings.getProperty("default");
    final String[] apiPaths = defaultConf != null ? defaultConf.split(", ") : null;
    if (apiPaths == null) {
      return null;
    }
    final String optionalConf = apiSettings.getProperty("optional");
    final String [] optionalPaths = optionalConf != null ? optionalConf.split(", ") : null;
    ArrayList<String> jars = new ArrayList<String>();
    for (String apiPath : apiPaths) {
      if (optionalPaths != null && ArrayUtil.find(optionalPaths, apiPath) == -1){
        String jar = apiSettings.getProperty(apiPath + ".file");
        if (jar != null) {
          jars.add(homePath + File.separator + "lib" + File.separator + jar);
        }
      }
    }
    return jars.toArray(new String[jars.size()]);
  }

  @Nullable
  public static String getWTKVersion(String homeDir) {
    final Properties emulatorProperties = getWTKEmulatorProperties(homeDir);
    return emulatorProperties != null ? emulatorProperties.getProperty(TITLE_VERSIONED_PROPERTY) : null;
  }

  private static String getWTKConfigurations(@NotNull final String homeDir, @NonNls final String name) {
    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      @Nullable
      @SuppressWarnings({"HardCodedStringLiteral"})
      public String compute() {
        final VirtualFile homeDirectory = LocalFileSystem.getInstance().findFileByPath(homeDir);
        if (homeDirectory == null) {
          return null;
        }
        VirtualFile configProperties = homeDirectory.findFileByRelativePath("lib/system.config");
        if (configProperties == null) {
          return null;
        }
        Properties properties = new Properties();
        try {
          InputStream is = new ByteArrayInputStream(configProperties.contentsToByteArray());
          properties.load(is);
          is.close();
          return properties.getProperty(name);
        }
        catch (IOException e) {
          return null;
        }
      }
    });


  }

  public static String getProfileVersion(@NotNull String homeDir) {
    return getWTKConfigurations(homeDir, "microedition.profiles");
  }

  public static String getConfigurationVersion(@NotNull String homeDir) {
    return getWTKConfigurations(homeDir, "microedition.configuration");
  }

  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  public static String[] getWTKDevices(String homeDir) {
    ArrayList<String> result = new ArrayList<String>();
    final VirtualFile homeDirectory = LocalFileSystem.getInstance().findFileByPath(homeDir);
    if (homeDirectory == null) {
      return null;
    }
    VirtualFile devicesDirectory = homeDirectory.findFileByRelativePath("wtklib/devices");
    if (devicesDirectory == null) {
      return result.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }
    VirtualFile[] devices = devicesDirectory.getChildren();
    for (int i = 0; devices != null && i < devices.length; i++) {
      if (devices[i].isDirectory() && devices[i].findChild(devices[i].getName() + ".properties") != null) {
        result.add(devices[i].getName());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static boolean isValidWTKHome(String homeDir) {
    final Properties wtkEmulatorProperties = getWTKEmulatorProperties(homeDir);
    String property = wtkEmulatorProperties != null ? wtkEmulatorProperties.getProperty("TITLE_PROJECT") : null;
    return property != null && property.indexOf("Toolkit") > -1;
  }
}
