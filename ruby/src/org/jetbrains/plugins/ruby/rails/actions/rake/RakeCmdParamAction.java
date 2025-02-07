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

package org.jetbrains.plugins.ruby.rails.actions.rake;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
*
* @author: Roman Chernyatchik
* @date: 25.03.2007
*/
class RakeCmdParamAction extends ToggleAction {
    private final String myCmdArgument;
    private boolean myIsSelected;
    private boolean myIsDisabled;

    public RakeCmdParamAction(@NotNull final String cmdArgument,
                              @Nullable final String description,
                              @Nullable final Icon icon) {
        super(cmdArgument, description, icon);
        myCmdArgument = cmdArgument;
    }

    public boolean isSelected(final AnActionEvent e) {
        return myIsSelected;
    }

    public void setSelected(final AnActionEvent e, final boolean state) {
        this.myIsSelected = state;
    }

    @NotNull
    public String getMyCmdArgument() {
        return myIsSelected ? myCmdArgument : TextUtil.EMPTY_STRING;
    }

    public void disableAction() {
        setSelected(null, false);
        myIsDisabled = true;
    }

    public void update(final AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!myIsDisabled);
    }
}
