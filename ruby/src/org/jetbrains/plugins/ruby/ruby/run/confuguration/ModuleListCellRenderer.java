/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import com.intellij.openapi.module.Module;
import org.jetbrains.plugins.ruby.RBundle;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
*
* @author: Roman Chernyatchik
* @date: 04.08.2007
*/
public class ModuleListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            setText(((Module) value).getName());
            setIcon(((Module) value).getModuleType().getNodeIcon(true));
        } else {
            setText(RBundle.message("run.configuration.messages.none"));
        }
        return this;
    }
}
