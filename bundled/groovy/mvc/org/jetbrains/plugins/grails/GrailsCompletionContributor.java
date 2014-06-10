/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.plugins.grails;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspDeclarationHolder;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import static org.jetbrains.plugins.grails.util.DomainClassUtils.*;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionData;

import java.util.*;

/**
 * @author Maxim.Medvedev
 */
public class GrailsCompletionContributor extends CompletionContributor {

  private static final String[] DOMAIN_CLASS_FIELDS = new String[]{
      "belongsTo",
      "hasMany",
      "embedded",
      "transients",
      "constraints",
      "optionals"
  };


  private static final PsiElementPattern.Capture<PsiElement> grReferencePattern =
    psiElement().withParent(GrReferenceExpression.class);

  public GrailsCompletionContributor() {
    extend(CompletionType.BASIC, grReferencePattern, new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final GrReferenceExpression refExpr = (GrReferenceExpression)parameters.getPosition().getParent();
        final GrExpression qualifier = refExpr.getQualifierExpression();
        if (qualifier == null) return;
        final PsiType type = qualifier.getType();
        if (!isDomainClass(type)) return;

        if (isStaticMemberReference(qualifier)) {
          for (LookupElement el : getDynamicFinderMethods(refExpr, qualifier, type, result.getPrefixMatcher().getPrefix())) {
            result.addElement(el);
          }
          for (LookupElement el : getListOrderMethods(qualifier, type)) {
            result.addElement(el);
          }
        }
      }
    });

    extend(CompletionType.BASIC, psiElement(GroovyTokenTypes.mIDENT).withParent(
      PsiJavaPatterns.psiField().withModifiers(PsiModifier.STATIC)), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiClass psiClass = PsiTreeUtil.getParentOfType(parameters.getPosition(), PsiClass.class);
        if (DomainClassUtils.isDomainClass(psiClass)) {
          TreeSet<String> definedFields = DomainClassUtils.definedDomainClassFields(psiClass);
          for (String s : DOMAIN_CLASS_FIELDS) {
            if (!definedFields.contains(s)) {
              result.addElement(TailTypeDecorator.withTail(LookupElementBuilder.create(s), TailType.EQ));
            }
          }
        }
      }
    });

    extend(CompletionType.BASIC, psiElement().withParent(psiElement(PsiErrorElement.class).withParent(GrGspDeclarationHolder.class)), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        if (GroovyCompletionUtil.isNewStatement(parameters.getPosition(), false)) {
          for (final String s : CollectionFactory.ar(PsiModifier.STATIC, PsiModifier.FINAL)) {
            result.addElement(LookupElementBuilder.create(s).setBold());
          }
          for (final String s : GroovyCompletionData.MODIFIERS) {
            result.addElement(LookupElementBuilder.create(s).setBold());
          }
          for (final String s : GroovyCompletionData.BUILT_IN_TYPES) {
            result.addElement(LookupElementBuilder.create(s).setBold());
          }
        }
      }
    });
  }

  public static boolean isStaticMemberReference(GrExpression qual) {
    final PsiReference ref = qual.getReference();
    if (ref == null) return false;
    return ref.resolve() instanceof PsiClass;
  }

  private static LookupElement[] getDynamicFinderMethods(final GrReferenceExpression refExpr,
                                                         final GrExpression qualifier,
                                                         final PsiType type,
                                                         String pref) {
    final String refName = refExpr.getName();
    if (refName == null) {
      return LookupElement.EMPTY_ARRAY;
    }

    ArrayList<LookupElement> res = new ArrayList<LookupElement>();
    if (isStaticMemberReference(qualifier)) {
      final String[] fieldNames = getDomainFieldNames(getDomainClass(qualifier));

      for (int i = 0; i < fieldNames.length; i++) {
        fieldNames[i]=StringUtil.capitalize(fieldNames[i]);
      }

      if (isFreshFinderName(pref, fieldNames)) {
        for (String prefix : FINDER_PREFICES) {
          for (String name : fieldNames) {
            res.add(createLookup(prefix + name, false, type));
          }
        }
      }
      else {
        final Ref<String> ref = new Ref<String>(pref);
        final Set<String> filteredNames = new HashSet<String>();
        final String[] names = filterFieldsByPrefix(pref, fieldNames, ref, filteredNames);
        final String suffix = ref.get();
        pref = StringUtil.trimEnd(pref, suffix);

        if (filteredNames.size() == 2) { //finder already has two arguments
          res.add(createLookup(pref, true, type));
          if (!endsBy(pref, DOMAIN_FINDER_EXPRESSIONS)) {
            for (String expression : DOMAIN_FINDER_EXPRESSIONS) {
              res.add(createLookup(pref + expression, true, type));
            }
          }
        }
        else if (names.length > 0) { //finder has one argument
          if (!endsWithDomainConnectivity(pref)) {
            res.add(createLookup(pref, true, type));
          }
          if (endsBy(pref, DOMAIN_CONNECTIVES)) { //ends by connectivity. can add field
            for (String name : names) {
              res.add(createLookup(pref + name, false, type));
            }
          }
          else if (endsBy(pref, DOMAIN_FINDER_EXPRESSIONS)) { //ends by expression. can  add connectivity+field
            for (String connectivity : DOMAIN_CONNECTIVES) {
              for (String name : names) {
                res.add(createLookup(pref + connectivity+name, false, type));
              }
            }
          }
          else if (endsBy(pref, fieldNames)) {   //ends by field name. can add expression or connectivity+field
            for (String expression : DOMAIN_FINDER_EXPRESSIONS) {
              res.add(createLookup(pref + expression, false, type));
            }

            for (String connectivity : DOMAIN_CONNECTIVES) {
              for (String name : names) {
                res.add(createLookup(pref+ connectivity+name, false, type));
              }
            }
          }
        }
      }
      return res.toArray(new LookupElement[res.size()]);
    }
    return LookupElement.EMPTY_ARRAY;
  }

  private static boolean endsBy(String s, String[] names) {
    for (String name : names) {
      if (s.endsWith(name)) return true;
    }
    return false;
  }

  static LookupElement createLookup(String text, boolean brackets, PsiType type) {
    LookupElementBuilder element= LookupElementBuilder.create(text).setIcon(GroovyIcons.METHOD);
    String typeText = "";

    if (text.startsWith("find")) typeText = "List<" + type.getCanonicalText() + ">";
    if (text.startsWith("count")) typeText = "int";
    element = element.setTypeText(typeText);

    if (brackets) {
      element = element.setTailText("()", false);
    }
    else {
      element = element.setTailText("...", true);
    }
    return element;
  }

  private static LookupElement[] getListOrderMethods(final GrExpression qualifier, final PsiType type) {
    if (isStaticMemberReference(qualifier)) {
      final String[] fieldNames = getDomainFieldNames(getDomainClass(qualifier));
      final List<String> result = new ArrayList<String>();
      for (String name : fieldNames) {
        result.add(DOMAIN_LIST_ORDER + StringUtil.capitalize(name));
      }
      return ContainerUtil.map2Array(result, LookupElement.class, new Function<String, LookupElement>() {
        public LookupElement fun(final String s) {
          return LookupElementBuilder.create(s + "()").setIcon(GroovyIcons.METHOD).setTypeText("List<" + type.getCanonicalText() + ">");
        }
      });
    }
    return LookupElement.EMPTY_ARRAY;
  }

  private static boolean isFreshFinderName(final String prefix, final String[] fieldNames) {
    for (String finderPrefice : FINDER_PREFICES) {
      if (prefix.startsWith(finderPrefice)) {
        final String suffix = StringUtil.trimStart(prefix, finderPrefice);
        for (String name : fieldNames) {
          if (suffix.startsWith(name)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static String[] filterFieldsByPrefix(final String prefix,
                                               final String[] fieldNames,
                                               final Ref<String> ref,
                                               final Set<String> filteredNames) {
    final List<String> result = new ArrayList<String>(fieldNames.length);
    result.addAll(Arrays.asList(fieldNames));
    for (String finderPrefice : FINDER_PREFICES) {
      if (prefix.startsWith(finderPrefice)) {
        String suffix = StringUtil.trimStart(prefix, finderPrefice);

        // Remove mentioned fields
        ref.set(suffix);
        filterNames(result, ref, filteredNames);
        break;
      }
    }
    return result.toArray(new String[result.size()]);
  }

  private static void filterNames(final List<String> result, Ref<String> ref, final Set<String> filteredNames) {
    String suffix = ref.get();
    if (suffix.length() == 0) return;
    String name = findLongestNameMatchingSuffix(result, suffix);
    if (name != null) {
      result.remove(name);
      filteredNames.add(name);
      suffix = StringUtil.trimStart(suffix, name);

      //remove expression
      for (String expr : DOMAIN_FINDER_EXPRESSIONS) {
        if (suffix.startsWith(expr)) {
          suffix = StringUtil.trimStart(suffix, expr);
          break;
        }
      }

      if (suffix.length() == 0) {
        ref.set(suffix);
        return;
      }

      // remove connective
      for (String connective : DOMAIN_CONNECTIVES) {
        if (suffix.startsWith(connective)) {
          suffix = StringUtil.trimStart(suffix, connective);
          break;
        }
      }
      ref.set(suffix);
      if (suffix.length() == 0) {
        return;
      }

      filterNames(result, ref, filteredNames);
    }
  }

  @Nullable
  private static String findLongestNameMatchingSuffix(Collection<String> result, String suffix) {
    int len = 0;
    String res = null;
    for (String s : result) {
      if (suffix.toLowerCase().startsWith(s.toLowerCase()) && s.length() > len) {
        len = s.length();
        res = s;
      }
    }
    return res;
  }
}
