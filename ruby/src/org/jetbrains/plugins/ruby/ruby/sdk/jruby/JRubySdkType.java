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

package org.jetbrains.plugins.ruby.ruby.sdk.jruby;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.RComponents;
import org.jetbrains.plugins.ruby.jruby.JRubyIcons;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Aug 22, 2007
 */
public class JRubySdkType extends RubySdkType {
    @NonNls private static final String JRUBY_SDK_NAME =    "JRUBY_SDK";
    @NonNls private static final String JRUBY_WIN_EXE =     "jruby.bat";
    @NonNls private static final String JRUBY_UNIX_EXE =    "jruby";

    /**
     * @deprecated Don't use direct this constant(you can affect JRubySdkType), use getRubyExecutable()
     */
    @NonNls private static String JRUBY_EXE;
    static {
        if (SystemInfo.isWindows){
            //noinspection deprecation
            JRUBY_EXE = JRUBY_WIN_EXE;
        } else
        if (SystemInfo.isUnix){
            //noinspection deprecation
            JRUBY_EXE = JRUBY_UNIX_EXE;
        } else {
            LOG.error(RBundle.message("os.not.supported"));
        }
    }

    public String getRubyExecutable() {
        //noinspection deprecation
        return JRUBY_EXE;
    }

    public static boolean isJRubySDK(@Nullable final ProjectJdk sdk){
        return sdk != null && sdk.getSdkType() instanceof JRubySdkType;
    }

    public JRubySdkType() {
        super(JRUBY_SDK_NAME);
    }

    public String getPresentableName() {
           return RBundle.message("sdk.jruby.title");
       }

    @NotNull
    public String getComponentName() {
        return RComponents.JRUBY_SDK_TYPE;
    }

    public String suggestHomePath() {
        return RubySdkUtil.suggestJRubyHomePath();
    }

    public Icon getIconForAddAction() {
        return JRubyIcons.JRUBY_SDK_ADD_ICON;
    }

    @Override
    public Icon getIcon() {
      return JRubyIcons.JRUBY_SDK_ICON_CLOSED;
    }

    @Override
    public Icon getIconForExpandedTreeNode() {
      return JRubyIcons.JRUBY_SDK_ICON_OPEN;
    }

    public static JRubySdkType getInstance(){
        return ApplicationManager.getApplication().getComponent(JRubySdkType.class);
    }
}
