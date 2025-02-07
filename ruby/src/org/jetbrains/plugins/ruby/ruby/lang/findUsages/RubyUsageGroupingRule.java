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

import com.intellij.psi.PsiElement;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.rules.OrderableUsageGroupingRule;
import com.intellij.usages.rules.PsiElementUsage;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 2, 2007
 */
public class RubyUsageGroupingRule implements OrderableUsageGroupingRule {
    private RubyTextMatchedGroup TEXT_MATCHED = new RubyTextMatchedGroup();

    public UsageGroup groupUsage(Usage usage) {
        if (usage instanceof PsiElementUsage) {
            final PsiElement element = ((PsiElementUsage) usage).getElement();
            if (element!=null){
                if (RubyUsageTypeProvider.getType(element) == RubyUsageType.TEXT_MATCHED){
                    return TEXT_MATCHED;
                }
            }
        }
        return null;
    }

    public int getRank() {
        return 0;
    }
}
