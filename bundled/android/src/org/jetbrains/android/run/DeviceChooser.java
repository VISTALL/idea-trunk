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

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.sdklib.internal.avd.AvdManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import static com.intellij.openapi.util.text.StringUtil.capitalize;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.android.util.BooleanCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Apr 3, 2009
 * Time: 6:02:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeviceChooser extends DialogWrapper implements AndroidDebugBridge.IDeviceChangeListener {
  private final AndroidFacet myFacet;
  @Nullable
  private final AvdManager myAvdManager;
  private JPanel myPanel;
  private JTable myDeviceTable;
  private static final String[] COLUMN_TITLES = new String[]{"Serial Number", "AVD name", "State", "Compatible"};

  public DeviceChooser(AndroidFacet facet) {
    super(facet.getModule().getProject(), true);
    setTitle(AndroidBundle.message("choose.device.dialog.title"));
    init();
    myFacet = facet;
    DefaultTableModel defaultTableModel = new DefaultTableModel();
    myDeviceTable.setModel(defaultTableModel);
    myDeviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    myDeviceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateOkButton();
      }
    });
    myDeviceTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && isOKActionEnabled()) {
          doOKAction();
        }
      }
    });
    myDeviceTable.setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
    myAvdManager = myFacet.getAvdManagerSlowly();
    getOKAction().setEnabled(false);
    updateTable();
    AndroidDebugBridge.addDeviceChangeListener(this);
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myDeviceTable;
  }

  private void updateTable() {
    AndroidDebugBridge bridge = myFacet.getDebugBridge();
    if (bridge == null) return;
    IDevice[] devices = bridge.getDevices();
    int selectedRow = myDeviceTable.getSelectedRow();
    myDeviceTable.setModel(new MyDeviceTableModel(devices));
    if (selectedRow < devices.length) {
      myDeviceTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    }
    updateOkButton();
  }

  private void updateOkButton() {
    IDevice device = getSelectedDevice();
    getOKAction().setEnabled(device != null && device.isOnline() && isCompatible(device));
  }

  @NotNull
  private static String getDeviceState(@NotNull IDevice device) {
    return capitalize(device.getState().name().toLowerCase());
  }

  @Override
  protected void dispose() {
    super.dispose();
    AndroidDebugBridge.removeDeviceChangeListener(this);
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  @Nullable
  public IDevice getSelectedDevice() {
    int row = myDeviceTable.getSelectedRow();
    if (row >= 0) {
      Object serial = myDeviceTable.getValueAt(row, 0);
      AndroidDebugBridge bridge = myFacet.getDebugBridge();
      if (bridge == null) return null;
      IDevice[] devices = bridge.getDevices();
      for (IDevice device : devices) {
        if (device.getSerialNumber().equals(serial.toString())) {
          return device;
        }
      }
    }
    return null;
  }

  @Override
  protected Action[] createActions() {
    return new Action[]{new RefreshAction(), new LaunchEmulatorAction(), getOKAction(), getCancelAction()};
  }

  private class LaunchEmulatorAction extends AbstractAction {
    public LaunchEmulatorAction() {
      putValue(NAME, "Launch Emulator");
    }

    public void actionPerformed(ActionEvent e) {
      AvdManager.AvdInfo avd = null;
      AvdManager manager = myFacet.getAvdManagerSlowly();
      if (manager != null) {
        AvdChooser chooser = new AvdChooser(myFacet.getModule().getProject(), myFacet, manager, true, false);
        chooser.show();
        avd = chooser.getSelectedAvd();
        if (chooser.getExitCode() != OK_EXIT_CODE) return;
        assert avd != null;
      }
      myFacet.launchEmulator(avd != null ? avd.getName() : null, "", null);
    }
  }

  private class RefreshAction extends AbstractAction {
    RefreshAction() {
      putValue(NAME, "Refresh");
    }

    public void actionPerformed(ActionEvent e) {
      updateTable();
    }
  }

  public void deviceConnected(IDevice device) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        updateTable();
      }
    });
  }

  public void deviceDisconnected(IDevice device) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        updateTable();
      }
    });
  }

  public void deviceChanged(IDevice device, int changeMask) {
    if ((changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          updateTable();
        }
      });
      updateOkButton();
    }
  }

  private boolean isCompatible(IDevice device) {
    if (myAvdManager == null) return true;
    AvdManager.AvdInfo avd = myAvdManager.getAvd(device.getAvdName(), false);
    return avd != null && myFacet.isCompatibleAvd(avd);
  }

  private class MyDeviceTableModel extends AbstractTableModel {
    private final IDevice[] myDevices;

    public MyDeviceTableModel(IDevice[] devices) {
      myDevices = devices;
    }

    @Override
    public String getColumnName(int column) {
      return COLUMN_TITLES[column];
    }

    public int getRowCount() {
      return myDevices.length;
    }

    public int getColumnCount() {
      return COLUMN_TITLES.length;
    }

    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
      IDevice device = myDevices[rowIndex];
      switch (columnIndex) {
        case 0:
          return device.getSerialNumber();
        case 1:
          return device.getAvdName();
        case 2:
          return getDeviceState(device);
        case 3:
          return isCompatible(device);
      }
      return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 3) {
        return Boolean.class;
      }
      return String.class;
    }
  }
}
