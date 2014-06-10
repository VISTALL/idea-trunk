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

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.midp.MIDPEmulatorType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: anna
 * Date: Sep 21, 2004
 */
public class Emulator implements SdkAdditionalData, JDOMExternalizable {

  @NonNls
  public static final String COMMANDS = "commands";
  @NonNls
  public static final String COMMAND = "command";
  @NonNls
  public static final String NAME = "name";

  private String[] myOTACommands;

  private EmulatorType myEmulatorType;
  private String myJavaSdkName;
  private String myHome;
  private String[] myPreverifyOptions;

  private String myProfile;
  private String myConfiguration;

  private String myCustomProfile;
  private String myCustomConfiguration;

  @NonNls
  private static final String JDK = "javaJDK";
  @NonNls
  private static final String JDK_NAME = "name";

  @NonNls
  private static final String EMULATOR_TYPE = "emulator";
  @NonNls
  private static final String EMULATOR_NAME = "name";
  @NonNls
  private static final String PROFILE = "profile";
  @NonNls
  private static final String CONFIGURATION = "configuration";
  @NonNls
  private static final String CUSTOM_PROFILE = "customProfile";
  @NonNls
  private static final String CUSTOM_CONFIGURATION = "customConfiguration";
  @NonNls
  private static final String PREVERIFY_OPTIONS = "preverify";
  @NonNls
  private static final String OPTION = "option";
  @NonNls
  private static final String OPTION_NAME = "name";
  private static final Logger LOG = Logger.getInstance("#" + Emulator.class.getName());


  public Emulator(EmulatorType emulatorType, String[] preverifyOptions, String javaSdk, String home) {
    myEmulatorType = emulatorType;
    myPreverifyOptions = preverifyOptions;
    myJavaSdkName = javaSdk;
    myHome = home;
  }

  //for external read
  public Emulator() {
  }

  public Object clone() throws CloneNotSupportedException {
    Emulator clone = new Emulator(myEmulatorType, myPreverifyOptions, myJavaSdkName, myHome);
    return clone;
  }

  public void checkValid(SdkModel sdkModel) throws ConfigurationException {
    if (getJavaSdk(sdkModel) == null){
      throw new ConfigurationException(J2MEBundle.message("jdk.already.removed", myJavaSdkName));
    }
  }

  public void readExternal(Element element) throws InvalidDataException {
    final Element jdk = element.getChild(JDK);
    if (jdk != null) {
      myJavaSdkName = jdk.getAttributeValue(JDK_NAME);
    }

    Element emulatorType = element.getChild(EMULATOR_TYPE);
    if (emulatorType != null) {
      myEmulatorType = EmulatorUtil.getEmulatorTypeByName(emulatorType.getAttributeValue(EMULATOR_NAME));
      myProfile = emulatorType.getAttributeValue(PROFILE);
      myConfiguration = emulatorType.getAttributeValue(CONFIGURATION);
      myCustomProfile = emulatorType.getAttributeValue(CUSTOM_PROFILE);
      myCustomConfiguration = emulatorType.getAttributeValue(CUSTOM_CONFIGURATION);
    }
    ArrayList<String> options = new ArrayList<String>();
    Element preverifyOptions = element.getChild(PREVERIFY_OPTIONS);
    if (preverifyOptions != null) {
      for (Iterator<Element> iterator = preverifyOptions.getChildren(OPTION).iterator(); iterator.hasNext();) {
        options.add(iterator.next().getAttributeValue(OPTION_NAME));
      }
      myPreverifyOptions = options.toArray(new String[options.size()]);
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (myJavaSdkName != null) {
      Element jdk = new Element(JDK);
      jdk.setAttribute(JDK_NAME, myJavaSdkName);
      element.addContent(jdk);
    }

    Element emulatorType = new Element(EMULATOR_TYPE);
    if (myEmulatorType != null) {
      emulatorType.setAttribute(EMULATOR_NAME, myEmulatorType.getName());
      final String profile = getProfile();
      if (profile != null) {
        emulatorType.setAttribute(PROFILE, profile);
      }
      final String configuration = getConfiguration();
      if (configuration != null) {
        emulatorType.setAttribute(CONFIGURATION, configuration);
      }
      if (myCustomProfile != null) {
        emulatorType.setAttribute(CUSTOM_PROFILE, myCustomProfile);
      }
      if (myCustomConfiguration != null) {
        emulatorType.setAttribute(CUSTOM_CONFIGURATION, myCustomConfiguration);
      }
    }
    element.addContent(emulatorType);

    Element options = new Element(PREVERIFY_OPTIONS);
    for (int i = 0; myPreverifyOptions != null && i < myPreverifyOptions.length; i++) {
      Element option = new Element(OPTION);
      option.setAttribute(OPTION_NAME, myPreverifyOptions[i]);
      options.addContent(option);
    }
    element.addContent(options);
  }


  @Nullable
  public String[] getOTACommands(String home) {
    if (myOTACommands == null && myEmulatorType != null) {
      final String[] otaCommands = myEmulatorType.getOTACommands(home);
      myOTACommands = otaCommands != null ? otaCommands : new String[0];
    }
    return myOTACommands;
  }

  @Nullable
  public String getProfile() {
    if (myProfile == null && myEmulatorType instanceof MIDPEmulatorType) {
      LOG.assertTrue(myHome != null);
      myProfile = ((MIDPEmulatorType)myEmulatorType).getDefaultProfile(myHome);
    }
    return myProfile;
  }

  @Nullable
  public String getConfiguration() {
    if (myConfiguration == null && myEmulatorType instanceof MIDPEmulatorType) {
      LOG.assertTrue(myHome != null);
      myConfiguration = ((MIDPEmulatorType)myEmulatorType).getDefaultConfiguration(myHome);
    }
    return myConfiguration;
  }

  public void setProfile(String profile) {
    myProfile = profile;
  }

  public void setConfiguration(String configuration) {
    myConfiguration = configuration;
  }

  public String[] getPreverifyOptions() {
    return myPreverifyOptions;
  }

  public String getCustomProfile() {
    return myCustomProfile;
  }

  public void setCustomProfile(String customProfile) {
    myCustomProfile = customProfile;
  }

  public String getCustomConfiguration() {
    return myCustomConfiguration;
  }

  public void setCustomConfiguration(String customConfiguration) {
    myCustomConfiguration = customConfiguration;
  }

  public String getHome() {
    return myHome;
  }

  public void setHome(String home) {
    myHome = home;
  }

  public void setPreverifyOptions(String[] preverifyOptions) {
    myPreverifyOptions = preverifyOptions;
  }

  @Nullable
  public EmulatorType getEmulatorType() {
    return myEmulatorType;
  }

  public void setEmulatorType(EmulatorType emulatorType) {
    myEmulatorType = emulatorType;
  }

  @Nullable
  public Sdk getJavaSdk() {
    final ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
    if (myJavaSdkName == null) {  //setup default jdk
      final Sdk[] projectJdks = jdkTable.getAllJdks();
      for (Sdk jdk : projectJdks) {
        if (jdk.getSdkType() == JavaSdk.getInstance()) {
          myJavaSdkName = jdk.getName();
          break;
        }
      }
    }
    return jdkTable.findJdk(myJavaSdkName);
  }

  @Nullable
  public Sdk getJavaSdk(SdkModel sdkModel) {
    return sdkModel.findSdk(myJavaSdkName);
  }


  public String getJavaSdkName() {
    return myJavaSdkName;
  }

  public void setJavaSdk(String javaSdk) {
    myJavaSdkName = javaSdk;
  }

}
