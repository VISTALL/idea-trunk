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

package org.jetbrains.plugins.ruby.jruby.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.jruby.codeInsight.types.JRubyNameConventions;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 11, 2008
 */
public class JRubyNamesReferenceSearcher implements QueryExecutor<PsiReference, MethodReferencesSearch.SearchParameters> {

    public boolean execute(@NotNull final MethodReferencesSearch.SearchParameters params,
                           @NotNull final Processor<PsiReference> psiReferenceProcessor) {
        final PsiMethod method = params.getMethod();
        final String name = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            public String compute() {
                return method.getName();
            }
        });
        // We should search only if JRubyName differs from name
        final String jrubyName = JRubyNameConventions.getMethodName(name).replace("=", "");
        if (name.equals(jrubyName)) {
            return true;
        }

        final JRubyOcurrenceProcessor processor = new JRubyOcurrenceProcessor(method, jrubyName, psiReferenceProcessor, true);
        short searchContext = UsageSearchContext.IN_CODE;
        return PsiManager.getInstance(method.getProject()).getSearchHelper().
                processElementsWithWord(processor, params.getScope(), jrubyName, searchContext, true);
    }
}
