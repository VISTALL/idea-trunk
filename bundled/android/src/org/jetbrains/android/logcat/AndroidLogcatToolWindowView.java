package org.jetbrains.android.logcat;/*
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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.intellij.CommonBundle;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidPlatformsComboBox;
import org.jetbrains.android.util.AndroidBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
public abstract class AndroidLogcatToolWindowView implements Disposable {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.logcat.AndroidLogcatToolWindowView");

  private final Project myProject;
  private JComboBox myDeviceCombo;
  private JPanel myConsoleWrapper;
  private JPanel myPanel;
  private AndroidPlatformsComboBox myAndroidPlatformsCombo;
  private JPanel myAndroidPlatformPanel;
  private JButton myClearLogButton;
  private JPanel mySearchComponentWrapper;
  private volatile IDevice myDevice;
  private final Object myLock = new Object();
  private volatile LogConsoleBase myLogConsole;
  private volatile Reader myCurrentReader;

  private static Set<Library> getAndroidPlatformLibraries(Project project) {
    ModuleManager manager = ModuleManager.getInstance(project);
    Set<Library> result = new HashSet<Library>();
    for (Module module : manager.getModules()) {
      AndroidFacet facet = AndroidFacet.getInstance(module);
      if (facet != null) {
        AndroidPlatform platform = facet.getConfiguration().getAndroidPlatform();
        if (platform != null) {
          result.add(platform.getLibrary());
        }
      }
    }
    return result;
  }

  private class MyLoggingReader extends Reader {

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      synchronized (myLock) {
        return myCurrentReader != null ? myCurrentReader.read(cbuf, off, len) : -1;
      }
    }

    @Override
    public boolean ready() throws IOException {
      return myCurrentReader != null ? myCurrentReader.ready() : false;
    }

    @Override
    public void close() throws IOException {
      if (myCurrentReader != null) myCurrentReader.close();
    }
  }

  @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
  public AndroidLogcatToolWindowView(final Project project) {
    myProject = project;
    Disposer.register(myProject, this);

    final Set<Library> librarySet = getAndroidPlatformLibraries(project);
    myAndroidPlatformsCombo.setFilter(new Condition<Library>() {
      public boolean value(Library library) {
        return librarySet.contains(library);
      }
    });
    myAndroidPlatformsCombo.rebuildPlatforms();
    if (librarySet.size() < 2) {
      if (librarySet.size() == 0) {
        Messages.showErrorDialog(project, AndroidBundle.message("specify.platform.error"), CommonBundle.getErrorTitle());
        return;
      }
      myAndroidPlatformPanel.setVisible(false);
    }
    myDeviceCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateLogConsole();
      }
    });
    myDeviceCombo.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
          setText("<html><font color='red'>[none]</font></html>");
        }
        return this;
      }
    });
    LogFilterModel logFilterModel = new AndroidLogFilterModel(AndroidLogcatFiltersPreferences.getInstance(project).TOOL_WINDOW_LOG_LEVEL) {
      @Override
      protected void setCustomFilter(String filter) {
        AndroidLogcatFiltersPreferences.getInstance(project).TOOL_WINDOW_CUSTOM_FILTER = filter;
      }

      @Override
      protected void saveLogLevel(Log.LogLevel logLevel) {
        AndroidLogcatFiltersPreferences.getInstance(project).TOOL_WINDOW_LOG_LEVEL = logLevel.name();
      }

      @Override
      public String getCustomFilter() {
        return AndroidLogcatFiltersPreferences.getInstance(project).TOOL_WINDOW_CUSTOM_FILTER;
      }
    };
    myLogConsole = new LogConsoleBase(project, new MyLoggingReader(), null, false, logFilterModel) {
      @Override
      public boolean isActive() {
        return AndroidLogcatToolWindowView.this.isActive();
      }
    };
    mySearchComponentWrapper.add(myLogConsole.getSearchComponent());
    final JComponent tbComp =
      ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, myLogConsole.getToolbarActions(), false).getComponent();
    myConsoleWrapper.add(tbComp, BorderLayout.WEST);
    myConsoleWrapper.add(myLogConsole.getComponent(), BorderLayout.CENTER);
    Disposer.register(this, myLogConsole);
    myClearLogButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        IDevice device = (IDevice)myDeviceCombo.getSelectedItem();
        if (device != null) {
          AndroidLogcatUtil.clearLogcat(project, device);
          myLogConsole.clear();
        }
      }
    });
    updateDevices();
    updateLogConsole();
  }

  protected abstract boolean isActive();

  public void activate() {
    updateDevices();
    updateLogConsole();
    myLogConsole.activate();
  }

  private void updateLogConsole() {
    IDevice device = (IDevice)myDeviceCombo.getSelectedItem();
    if (device == null) return;
    String serialNumber = device.getSerialNumber();
    if (myDevice == null || !serialNumber.equals(myDevice.getSerialNumber())) {
      synchronized (myLock) {
        myLogConsole.clear();
        myDevice = device;
        if (myCurrentReader != null) {
          try {
            myCurrentReader.close();
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
        myCurrentReader = AndroidLogcatUtil.startLoggingThread(myProject, device, false);
      }
    }
  }

  private void updateDevices() {
    AndroidPlatform platform = myAndroidPlatformsCombo.getSelectedPlatform();
    assert platform != null;
    AndroidDebugBridge debugBridge = platform.getSdk().getDebugBridge(myProject);
    if (debugBridge != null) {
      IDevice[] devices = debugBridge.getDevices();
      Object temp = myDeviceCombo.getSelectedItem();
      myDeviceCombo.setModel(new DefaultComboBoxModel(devices));
      if (devices.length > 0 && temp == null) {
        temp = devices[0];
      }
      myDeviceCombo.setSelectedItem(temp);
    }
  }

  public JPanel getContentPanel() {
    return myPanel;
  }

  private void createUIComponents() {
    LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable();
    myAndroidPlatformsCombo = new AndroidPlatformsComboBox(table.getModifiableModel(), null);
  }

  public void dispose() {
  }

}
