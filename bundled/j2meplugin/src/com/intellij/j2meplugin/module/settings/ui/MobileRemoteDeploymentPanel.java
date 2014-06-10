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
package com.intellij.j2meplugin.module.settings.ui;

import com.intellij.ui.FieldPanel;

import javax.swing.*;
import java.awt.*;

/**
 * User: anna
 * Date: Oct 25, 2004
 */
//dead code !!!
@SuppressWarnings({"HardCodedStringLiteral"})
public class MobileRemoteDeploymentPanel {
  private final JPanel myWholePanel = new JPanel(new GridBagLayout());
  private final JLabel myServer = new JLabel("FTP Server:");
  private final FieldPanel myServerField = new FieldPanel();

  private final JLabel myRemotePath = new JLabel("Remote path:");
  private final FieldPanel myRemoteField = new FieldPanel();

  private final JLabel myUsername = new JLabel("Username:");
  private final FieldPanel myUsernameField = new FieldPanel();

  private final JLabel myPassword = new JLabel("Password:");
  private final FieldPanel myPasswordField = new FieldPanel();

  private final JLabel myTransferMode = new JLabel("Transfer mode:");
  private final JRadioButton myBinaryTransfer = new JRadioButton("Binary");
  private final JRadioButton myASCIITransfer = new JRadioButton("ASCII");
  private final JRadioButton myAutoTransfer = new JRadioButton("Auto");
  private final ButtonGroup myTransferGroup = new ButtonGroup();

  public MobileRemoteDeploymentPanel() {
    myWholePanel.add(myServer,
                     new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                            GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    myWholePanel.add(myServerField,
                     new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 0), 0, 0));
    myServerField.createComponent();

    myWholePanel.add(myRemotePath,
                     new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                            GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    myWholePanel.add(myRemoteField,
                     new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 0), 0, 0));
    myRemoteField.createComponent();

    myWholePanel.add(myUsername,
                     new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                            GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    myWholePanel.add(myUsernameField,
                     new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 0), 0, 0));
    myUsernameField.createComponent();

    myWholePanel.add(myPassword,
                     new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                            GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    myWholePanel.add(myPasswordField,
                     new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.HORIZONTAL, new Insets(2, 0, 2, 0), 0, 0));
    myPasswordField.createComponent();

    myWholePanel.add(myTransferMode,
                     new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.EAST,
                                            GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

    myWholePanel.add(myAutoTransfer,
                     new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));


    myWholePanel.add(myASCIITransfer,
                     new GridBagConstraints(2, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));

    myWholePanel.add(myBinaryTransfer,
                     new GridBagConstraints(3, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
                                            GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));

    myTransferGroup.add(myAutoTransfer);
    myTransferGroup.add(myASCIITransfer);
    myTransferGroup.add(myBinaryTransfer);

    myAutoTransfer.setSelected(true);

  }

  public JPanel getComponent() {
    return myWholePanel;
  }

}
