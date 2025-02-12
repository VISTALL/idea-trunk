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

package org.jetbrains.plugins.ruby.ruby.inspections.resolve;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RBundle;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 25, 2007
 */
public class RubyResolveInspection extends LocalInspectionTool {
    @NonNls
    private static final String SHORT_NAME = "RubyResolve";

    @NotNull
        @Nls
        public String getGroupDisplayName() {
            return RBundle.message("inspection.group.name");
        }

        @NotNull
        @Nls
        public String getDisplayName() {
            return RBundle.message("inspection.resolve.name");
        }

        @NotNull
        @NonNls
        public String getShortName() {
            return SHORT_NAME;
        }

        public boolean isEnabledByDefault() {
            return false;
        }

        @NotNull
        public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
            return new RubyResolveVisitor(holder);
        }

        @NotNull
        public HighlightDisplayLevel getDefaultLevel() {
            return HighlightDisplayLevel.WARNING;
        }

    }

