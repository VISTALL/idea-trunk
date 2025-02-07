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

package org.jetbrains.plugins.ruby;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.ri.RubyDocManager;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 16.01.2007
 */

/**
 * This ruby and rails module component is used to inform each project component
 * about adding and removing ruby or rails modules from project.
 * Used to inform RDocManager
 */
public class RIModuleComponent implements ModuleComponent {
    final private Module myModule;

    public RIModuleComponent(@NotNull Module module){
        myModule = module;
    }

    public void projectOpened() {}

    public void projectClosed() {}

    public void moduleAdded() {
        RubyDocManager.getInstance(myModule.getProject()).addRModule(myModule);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return RComponents.RI_MODULE_COMPONENT;
    }

    public void initComponent() {}
    public void disposeComponent() {
        RubyDocManager.getInstance(myModule.getProject()).removeRModule(myModule);
    }
}
