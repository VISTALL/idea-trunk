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
package com.intellij.j2meplugin.emulator.midp.nokia;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * User: anna
 * Date: Sep 6, 2004
 */
public class ConfigurationUtil {
  @NonNls private static final String XML_FILE_TYPE = ".xml";
  @NonNls private static final String PROPERTIES_FILE_TYPE = ".properties";

  @Nullable
  private static Document getEmulatorProperties(String homeDir) {
    final File home = new File(homeDir);
    if (!home.exists() || !home.isDirectory()) return null;
    File[] xml = home.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getPath().endsWith(XML_FILE_TYPE);
      }
    });
    if (xml == null) return null;
    if (xml.length != 1) {
      xml = home.listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          if (pathname.getPath().endsWith(XML_FILE_TYPE)) {
            final String homeName = home.getName();
            final int _index = homeName.indexOf('_');
            if (_index > 0 && pathname.getName().startsWith(homeName.substring(0, _index))) {
              return true;
            }
          }
          return false;
        }
      });
    }
    if (xml == null || xml.length != 1) return null; //can't choose corresponding file
    try {
      InputStream is = new BufferedInputStream(new FileInputStream(xml[0]));
      Document document = JDOMUtil.loadDocument(is);
      is.close();
      return document;
    }
    catch (IOException e) {
      return null;
    }
    catch (JDOMException e) {
      return null;
    }
  }


  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  public static String getPreferences(String homeDir) {
    Element pref = getByNameGroup(homeDir, "preferences");
    if (pref == null) return null;
    Element property;
    Iterator<Element> iterator = pref.getChildren("property").iterator();
    while (iterator.hasNext()) {
      property = iterator.next();
      final String name = property.getAttributeValue("name");
      if (Comparing.equal(name, "prefs")) {
        String preferencePath = property.getAttributeValue("value");
        if (preferencePath == null) return null;
        final int separatorIndex = preferencePath.indexOf(File.separatorChar);
        return separatorIndex > -1 ? preferencePath.substring(separatorIndex) : null;
      }
    }
    return null;
  }

  @Nullable
  @SuppressWarnings({"HardCodedStringLiteral"})
  private static Element getByNameGroup(String homeDir, String groupName) {
    final Document emulatorProperties = getEmulatorProperties(homeDir);
    if (emulatorProperties == null) return null;
    Element group = null;
    Iterator<Element> iterator = emulatorProperties.getRootElement().getChildren("group").iterator();
    while (iterator.hasNext()) {
      group = iterator.next();
      final String name = group.getAttributeValue("name");
      if (Comparing.equal(name, groupName)) {
        return group;
      }
    }
    return group;
  }

  @Nullable
  public static Properties getProperties(String homePath) {
    Properties properties = new Properties();
    final File home = new File(homePath);
    if (!home.exists() || !home.isDirectory()) return null;
    File[] props = home.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getPath().endsWith(PROPERTIES_FILE_TYPE);
      }
    });
    if (props == null) return properties;
    if (props.length != 1) {
      props = home.listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.getPath().endsWith(PROPERTIES_FILE_TYPE) && pathname.getName().equals(home.getName() + PROPERTIES_FILE_TYPE);
        }
      });
    }
    if (props == null || props.length != 1) return properties;
    try {
      InputStream is = new BufferedInputStream(new FileInputStream(props[0]));
      properties.load(is);
      is.close();
      return properties;
    }
    catch (IOException e) {
      return null;
    }
  }

}
