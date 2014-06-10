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

import com.android.prefs.AndroidLocation;
import static com.android.prefs.AndroidLocation.FOLDER_AVD;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.avd.AvdManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidSdk;
import org.jetbrains.android.sdk.MessageBuildingSdkLog;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: May 9, 2009
 * Time: 8:16:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateAvdDialog extends DialogWrapper {
  private JTextField myNameField;
  private JComboBox myTargetBox;
  private JComboBox mySkinField;
  private JTextField mySdCardField;
  private JPanel myPanel;
  private JLabel myAvdInfoLink;
  private JLabel myAvdInfoLabel;
  private final AndroidFacet myFacet;
  private final AvdManager myAvdManager;
  private AvdManager.AvdInfo myCreatedAvd;

  private static class Size {
    final int myWidth;
    final int myHeight;

    private Size(int width, int height) {
      myWidth = width;
      myHeight = height;
    }
  }

  private static final String DEFAULT_SKIN = "HVGA";
  private static Map<String, Size> displaySizes;

  private static void initializeDisplaySizes() {
    // portrait is default
    displaySizes = new HashMap<String, Size>();
    displaySizes.put("HVGA", new Size(320, 480));
    displaySizes.put("QVGA", new Size(240, 320));
  }

  @NotNull
  private String generateAvdName() {
    String prefix = "MyAvd";
    for (int i = 0; ; i++) {
      String candidate = prefix + i;
      if (myAvdManager.getAvd(candidate, false) == null) {
        return candidate;
      }
    }
  }

  @Nullable
  private static String getSkinInfo(@NotNull String skinName) {
    int length = skinName.length();
    String nameWithoutSuffix = length > 2 ? skinName.substring(0, length - 2) : "";
    if (displaySizes == null) {
      initializeDisplaySizes();
    }
    Size size = displaySizes.get(nameWithoutSuffix);
    if (size != null) {
      if (skinName.endsWith("-L")) {
        return size.myHeight + "x" + size.myWidth + ", landscape";
      }
      if (skinName.endsWith("-P")) {
        return size.myWidth + "x" + size.myHeight + ", portrait";
      }
    }
    return null;
  }

  private static List<String> getSkinPresentations(@NotNull String[] skinNames) {
    List<String> result = new ArrayList<String>();
    for (String name : skinNames) {
      // skip default HVGA skin without suffix
      if (DEFAULT_SKIN.equals(name)) continue;
      String info = getSkinInfo(name);
      result.add(info != null ? name + " (" + info + ')' : name);
    }
    return result;
  }

  public CreateAvdDialog(@NotNull Project project,
                         @NotNull AndroidFacet facet,
                         @NotNull AvdManager manager,
                         boolean onlyCompatibleTargets,
                         boolean showAvdInfo) {
    super(project, true);
    setTitle(AndroidBundle.message("create.avd.dialog.title"));
    init();
    myFacet = facet;
    myAvdManager = manager;
    final AndroidSdk sdk = facet.getConfiguration().getAndroidSdk();
    assert sdk != null;
    IAndroidTarget[] targets = sdk.getTargets();
    myTargetBox.setModel(new DefaultComboBoxModel(targets));
    myTargetBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selected = myTargetBox.getSelectedItem();
        mySkinField.setEnabled(selected != null);
        if (selected != null) {
          IAndroidTarget target = (IAndroidTarget)selected;
          List<String> skinList = getSkinPresentations(target.getSkins());
          mySkinField.setModel(new CollectionComboBoxModel(skinList, null));
        }
      }
    });
    myTargetBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        IAndroidTarget target = (IAndroidTarget)value;
        setText(AndroidSdkUtils.getPresentableTargetName(target));
        return this;
      }
    });
    IAndroidTarget target = facet.getConfiguration().getAndroidTarget();
    if (target != null) {
      myTargetBox.setSelectedItem(target);
      if (onlyCompatibleTargets) {
        myTargetBox.setEnabled(false);
      }
    }
    else if (targets.length > 0) {
      myTargetBox.setSelectedItem(targets[0]);
    }
    myNameField.setText(generateAvdName());
    final String url = "http://developer.android.com/guide/developing/tools/avd.html";
    myAvdInfoLink.setText("<html>\n" +
                          "   <body>\n" +
                          "     <p style=\"margin-top: 0;\">\n" +
                          "<a href=\"" +
                          url +
                          "\">More information about AVDs</a>\n" +
                          "     </p>\n" +
                          "   </body>\n" +
                          " </html>");
    myAvdInfoLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
    myAvdInfoLink.setVisible(showAvdInfo);
    myAvdInfoLabel.setVisible(showAvdInfo);
    myAvdInfoLink.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isConsumed()) return;
        try {
          BrowserUtil.launchBrowser(url);
        }
        catch (IllegalThreadStateException ex) {
          /* not a problem */
        }
        e.consume();
      }
    });
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myNameField;
  }

  @Override
  protected void doOKAction() {
    if (myNameField.getText().length() == 0) {
      Messages.showErrorDialog(myPanel, AndroidBundle.message("specify.avd.name.error"));
      return;
    }
    else if (myTargetBox.getSelectedItem() == null) {
      Messages.showErrorDialog(myPanel, AndroidBundle.message("select.target.dialog.text"));
      return;
    }
    String avdName = myNameField.getText();
    AvdManager.AvdInfo info = myAvdManager.getAvd(avdName, false);
    if (info != null) {
      boolean replace = Messages
        .showYesNoDialog(myPanel, AndroidBundle.message("replace.avd.question", avdName), AndroidBundle.message("create.avd.dialog.title"),
                         Messages.getQuestionIcon()) == 0;
      if (!replace) return;
    }
    File avdFolder;
    try {
      avdFolder = new File(AndroidLocation.getFolder() + FOLDER_AVD, avdName + AvdManager.AVD_FOLDER_EXTENSION);
    }
    catch (AndroidLocation.AndroidLocationException e) {
      Messages.showErrorDialog(myPanel, e.getMessage(), "Error");
      return;
    }
    super.doOKAction();
    IAndroidTarget selectedTarget = (IAndroidTarget)myTargetBox.getSelectedItem();
    String skin = (String)mySkinField.getSelectedItem();
    String sdCard = mySdCardField.getText().length() > 0 ? mySdCardField.getText() : null;
    MessageBuildingSdkLog log = new MessageBuildingSdkLog();
    myCreatedAvd = myAvdManager.createAvd(avdFolder, avdName, selectedTarget, skin, sdCard, null, true, log);
    if (log.getErrorMessage().length() > 0) {
      Messages.showErrorDialog(myPanel, log.getErrorMessage(), AndroidBundle.message("android.avd.error.title"));
    }
  }

  @Nullable
  public AvdManager.AvdInfo getCreatedAvd() {
    return myCreatedAvd;
  }

  @Override
  protected String getHelpId() {
    return "reference.android.createAVD";
  }
}
