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

package org.jetbrains.plugins.ruby.ruby.lang.findUsages;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.usages.UsageView;
import com.intellij.usages.rules.UsageGroupingRule;
import com.intellij.usages.rules.UsageGroupingRuleProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RComponents;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 2, 2007
 */
public class RubyUsagesGroupingRuleProvider implements UsageGroupingRuleProvider {
    @NotNull
    public UsageGroupingRule[] getActiveRules(Project project) {
        return new UsageGroupingRule[]{new RubyUsageGroupingRule()};
    }

    @NotNull
    public AnAction[] createGroupingActions(UsageView usageView) {
        return AnAction.EMPTY_ARRAY;
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return RComponents.RUBY_USAGE_GROUP_RULE_PROVIDER;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}