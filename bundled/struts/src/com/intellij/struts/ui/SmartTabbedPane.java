/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: DAvdeev
 * Date: 12.12.2005
 * Time: 17:02:25
 * To change this template use File | Settings | File Templates.
 */
public class SmartTabbedPane extends JTabbedPane {

  private final HashMap tabs = new HashMap();

  public void addTab(String title, Icon icon, Component component, int virtualIndex) {
    tabs.put(new Integer(this.getComponentCount()), new Integer(virtualIndex));
    addTab(title, icon, component);
  }

  public int getVirtualIndex() {
    int i = getSelectedIndex();
    Integer v = (Integer)tabs.get(new Integer(i));
    return v == null ? i : v.intValue();
  }

  public void setVirtualIndex(int i) {
    Integer v = (Integer)tabs.get(new Integer(i));
    this.setSelectedIndex(v == null ? i : v.intValue());
  }
}
