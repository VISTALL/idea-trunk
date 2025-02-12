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

package org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui;

import com.intellij.openapi.projectRoots.ProjectJdk;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Apr 5, 2008
 */
public class TabbedSettingsContextImpl implements TabbedSettingsContext {
    private final ProjectJdk mySDK;

    public TabbedSettingsContextImpl(@Nullable final ProjectJdk sdk) {
        mySDK = sdk;
    }

    @Nullable
    public ProjectJdk getSdk() {
        return mySDK;
    }
}
