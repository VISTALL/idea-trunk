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

package org.jetbrains.android.run;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.intellij.CommonBundle;
import com.intellij.diagnostic.logging.LogConsole;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.xml.GenericAttributeValue;
import org.jdom.Element;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidFacetConfiguration;
import org.jetbrains.android.logcat.AndroidLogFilterModel;
import org.jetbrains.android.logcat.AndroidLogcatFiltersPreferences;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 27, 2009
 * Time: 2:20:54 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AndroidRunConfigurationBase extends ModuleBasedConfiguration<JavaRunConfigurationModule> {
  public boolean CHOOSE_DEVICE_MANUALLY = false;
  public String PREFERRED_AVD = "";
  public String COMMAND_LINE = "";
  public boolean WIPE_USER_DATA = false;
  public boolean DISABLE_BOOT_ANIMATION = false;
  public String NETWORK_SPEED = "full";
  public String NETWORK_LATENCY = "none";
  public boolean CLEAR_LOGCAT = false;

  public AndroidRunConfigurationBase(final String name, final Project project, final ConfigurationFactory factory) {
    super(name, new JavaRunConfigurationModule(project, false), factory);
  }

  public Collection<Module> getValidModules() {
    final List<Module> result = new ArrayList<Module>();
    Module[] modules = ModuleManager.getInstance(getProject()).getModules();
    for (Module module : modules) {
      if (AndroidFacet.getInstance(module) != null) {
        result.add(module);
      }
    }
    return result;
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final Module module = getConfigurationModule().getModule();
    if (module == null) {
      throw new ExecutionException("Module is not found");
    }
    AndroidFacet facet = AndroidFacet.getInstance(module);
    if (facet == null) {
      throw new ExecutionException(AndroidBundle.message("no.facet.error", module.getName()));
    }
    AndroidFacetConfiguration configuration = facet.getConfiguration();
    AndroidPlatform platform = configuration.getAndroidPlatform();
    Project project = module.getProject();
    if (platform == null) {
      Messages.showErrorDialog(project, AndroidBundle.message("specify.platform.error"), CommonBundle.getErrorTitle());
      ModulesConfigurator.showFacetSettingsDialog(facet, null);
      return null;
    }
    else {
      Manifest manifest = facet.getManifest();
      if (manifest == null) return null;
      GenericAttributeValue<String> packageAttrValue = manifest.getPackage();
      String aPackage = packageAttrValue.getValue();
      if (aPackage == null || aPackage.length() == 0) {
        Messages.showErrorDialog(project, AndroidBundle.message("specify.main.package.error"), CommonBundle.getErrorTitle());
        XmlAttributeValue attrValue = packageAttrValue.getXmlAttributeValue();
        if (attrValue != null) {
          PsiNavigateUtil.navigate(attrValue);
        }
        else {
          PsiNavigateUtil.navigate(manifest.getXmlElement());
        }
        return null;
      }
      if (platform.getSdk().getDebugBridge(project) == null) return null;
      String deviceSerialNumber = null;
      if (CHOOSE_DEVICE_MANUALLY) {
        deviceSerialNumber = chooseDeviceManually(facet);
        if (deviceSerialNumber == null) return null;
      }
      AndroidApplicationLauncher applicationLauncher = getApplicationLauncher(facet);
      if (applicationLauncher != null) {
        return new AndroidRunningState(env, facet, deviceSerialNumber, PREFERRED_AVD.length() > 0 ? PREFERRED_AVD : null,
                                       computeCommandLine(), aPackage, applicationLauncher) {

          @NotNull
          @Override
          protected ConsoleView attachConsole() throws ExecutionException {
            return AndroidRunConfigurationBase.this.attachConsole(this);
          }
        };
      }
    }
    return null;
  }

  private String computeCommandLine() {
    StringBuilder result = new StringBuilder();
    result.append("-netspeed ").append(NETWORK_SPEED).append(' ');
    result.append("-netdelay ").append(NETWORK_LATENCY).append(' ');
    if (WIPE_USER_DATA) {
      result.append("-wipe-data ");
    }
    if (DISABLE_BOOT_ANIMATION) {
      result.append("-no-boot-anim ");
    }
    result.append(COMMAND_LINE);
    int last = result.length() - 1;
    if (result.charAt(last) == ' ') {
      result.deleteCharAt(last);
    }
    return result.toString();
  }

  @NotNull
  protected abstract ConsoleView attachConsole(AndroidRunningState state) throws ExecutionException;

  @Nullable
  protected abstract AndroidApplicationLauncher getApplicationLauncher(AndroidFacet facet);

  @Nullable
  private static String chooseDeviceManually(@NotNull AndroidFacet facet) {
    DeviceChooser chooser = new DeviceChooser(facet);
    chooser.show();
    IDevice device = chooser.getSelectedDevice();
    return chooser.getExitCode() == DeviceChooser.OK_EXIT_CODE && device != null ? device.getSerialNumber() : null;
  }

  @Override
  public void customizeLogConsole(LogConsole console) {
    final Project project = getProject();
    console.setFilterModel(new AndroidLogFilterModel(AndroidLogcatFiltersPreferences.getInstance(project).TAB_LOG_LEVEL) {
      @Override
      protected void setCustomFilter(String filter) {
        AndroidLogcatFiltersPreferences.getInstance(project).TAB_CUSTOM_FILTER = filter;
      }

      @Override
      protected void saveLogLevel(Log.LogLevel logLevel) {
        AndroidLogcatFiltersPreferences.getInstance(project).TAB_LOG_LEVEL = logLevel.name();
      }

      @Override
      public String getCustomFilter() {
        return AndroidLogcatFiltersPreferences.getInstance(project).TAB_CUSTOM_FILTER;
      }
    });
  }

  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);
    DefaultJDOMExternalizer.writeExternal(this, element);
  }
}
