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

package org.jetbrains.plugins.ruby.ruby;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.module.RubyModuleType;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 30.09.2006
 */
public class RubyUtil {
    @NonNls
    public static final String MODULES_PATH_SEPARATOR = "::";
    @NonNls
    public static final char RUBY_PATH_SEPARATOR = '/';

    /**
     * This arguments uses to obtain correct output when ruby script
     * should be run in console.
     */
    @NonNls
    protected static String RUN_IN_CONSOLE_HACK = "STDOUT.sync=true;STDERR.sync=true;load($0=ARGV.shift)";
    @NonNls
    public static String[] RUN_IN_CONSOLE_HACK_ARGUMENTS =
            SystemInfo.isWindows
                ? new String[]{"-e", "\"" + RUN_IN_CONSOLE_HACK + "\""}
                : new String[]{"-e", RUN_IN_CONSOLE_HACK};
    public static String[] RUN_IN_CONSOLE_HACK_ARGUMENTS_NO_SHIFT
            = new String[]{"-e", "STDOUT.sync=true;STDERR.sync=true"};

    /**
     * Check if module is Ruby module
     *
     * @param module some module
     * @return true if is ruby module.
     */
    public static boolean isRubyModuleType(@Nullable final Module module) {
        return module != null && RubyModuleType.getInstance() == module.getModuleType();
    }

    public static boolean isRubyModuleType(final ModuleType moduleType) {
        return RubyModuleType.getInstance() == moduleType;
    }
}
