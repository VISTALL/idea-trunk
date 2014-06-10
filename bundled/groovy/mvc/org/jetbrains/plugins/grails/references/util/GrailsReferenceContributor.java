/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.references.util;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.references.providers.GrailsReferenceExpressionProvider;
import org.jetbrains.plugins.grails.references.providers.GspImportListReferenceProvider;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

/**
 * @author ilyas
 */
public class GrailsReferenceContributor extends PsiReferenceContributor {

  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {

    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar,
        new String[]{"import"},
        new ScopeFilter(
            new ParentElementFilter(
                new AndFilter(
                    new OrFilter(
                        new AndFilter(
                            new ClassFilter(GspDirective.class),
                            new TextFilter("page")
                        )
                    ),
                    new NamespaceFilter(XmlUtil.JSP_URI)
                ),
                2
            )
        ),
        new GspImportListReferenceProvider(registrar.getProject())
    );
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(GrReferenceExpression.class), new GrailsReferenceExpressionProvider());
  }


}
