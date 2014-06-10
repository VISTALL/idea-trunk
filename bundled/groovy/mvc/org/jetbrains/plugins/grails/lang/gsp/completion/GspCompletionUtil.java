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

package org.jetbrains.plugins.grails.lang.gsp.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspResolveUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspGroovyFile;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyPropertyUtils;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.lang.resolve.processors.CompletionProcessor;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ResolverProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ilyas
 */
public abstract class GspCompletionUtil {
  public static final String[] GSP_PROPERTY_NAMES = {"application", "flash", "out", "params", "request", "response", "session"};
  public static final String NAMESPACE_FIELD = "namespace";

  private static LookupElement[] getGspPageLookupProperties() {
    List<LookupElement> pageProperties = new ArrayList<LookupElement>();
    for (String propName : GSP_PROPERTY_NAMES) {
      pageProperties.add(LookupElementBuilder.create(propName).setIcon(GroovyIcons.PROPERTY));
    }
    return pageProperties.toArray(new LookupElement[GspCompletionUtil.GSP_PROPERTY_NAMES.length]);
  }

  public static Object[] getGspContextSpecificVariants(GrReferenceExpression refExpr) {
    GrExpression qualifier = refExpr.getQualifierExpression();
    if (qualifier == null) {
      String[] prefixes = GspTagLibUtil.getKnownPrefixes(refExpr, false);
      Object[] staticVariants = ArrayUtil.mergeArrays(prefixes, getGspPageLookupProperties(), Object.class);
      String name = refExpr.getReferenceName();
      if (name == null || !GspResolveUtil.isUnderExprInjection(refExpr)) {
        return staticVariants;
      }
      final ResolverProcessor processor = CompletionProcessor.createPropertyCompletionProcessor(refExpr);
      GspResolveUtil.collectGspUnqualifiedVariants(processor, refExpr);
      Object[] tags = ResolveUtil.mapToElements(processor.getCandidates());
      return ArrayUtil.mergeArrays(filterSpecificVariants(tags, refExpr), staticVariants, Object.class);
    } else {
      if (!(qualifier instanceof GrReferenceExpression)) return ArrayUtil.EMPTY_OBJECT_ARRAY;
      String prefix = ((GrReferenceExpression) qualifier).getName();
      if (prefix == null) return ArrayUtil.EMPTY_OBJECT_ARRAY;
      final ResolverProcessor processor = CompletionProcessor.createPropertyCompletionProcessor(refExpr);
      for (PsiClass tagLibClass : GspTagLibUtil.getCustomTagLibClasses(refExpr, prefix)) {
        tagLibClass.processDeclarations(processor, ResolveState.initial(), null, refExpr);
      }
      Object[] elements = ResolveUtil.mapToElements(processor.getCandidates());
      return filterSpecificVariants(elements, refExpr);
    }
  }

  public static Object[] filterSpecificVariants(Object[] variants, PsiElement place) {
    if (!(place instanceof GrReferenceExpression && place.getContainingFile() instanceof GspGroovyFile)) {
      return variants;
    } else {
      Set<Object> filteredElements = new HashSet<Object>();
      for (Object variant : variants) {
        if (variant instanceof PsiMember && GspResolveUtil.isGspTagMember(((PsiMember) variant))) {
          if (variant instanceof PsiMethod) {
            final PsiMethod method = (PsiMethod) variant;
            if (GroovyPropertyUtils.isSimplePropertyAccessor(method)) {
              String propName = PropertyUtil.getPropertyName(method);
              if (propName != null) {
                if (!PsiUtil.isValidReferenceName(propName)) {
                  propName = "'" + propName + "'";
                }
                if (!((PsiMethod) variant).hasModifierProperty(PsiModifier.STATIC) ||
                        NAMESPACE_FIELD.equals(propName)) {
                  filteredElements.add(LookupElementBuilder.create(propName).setIcon(GroovyIcons.PROPERTY));
                }
              }
            }
          } /*else if (variant instanceof PsiField && (!((PsiField) variant).hasModifierProperty(PsiModifier.STATIC) ||
              NAMESPACE_FIELD.equals(((PsiField) variant).getName()))) {
            filteredElements.add(variant);
          }*/
        }
      }
      return filteredElements.toArray(new Object[filteredElements.size()]);
    }
  }
}
