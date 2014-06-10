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

package org.jetbrains.plugins.grails.references.domain;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import static com.intellij.openapi.util.text.StringUtil.startsWith;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import static org.jetbrains.plugins.grails.util.DomainClassUtils.*;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GroovyScriptClass;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersProcessor;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ClassHint;

import java.util.*;

/**
 * @author maxim.medvedev
 */
public class GrailsNonCodeMembersProcessor implements NonCodeMembersProcessor {
  private static final String CONNECTIVE_REGEX;

  static {
    StringBuilder regexBuilder = new StringBuilder();
    final int length = DOMAIN_CONNECTIVES.length - 1;
    for (int i = 0; i < length; i++) {
      String connective = DOMAIN_CONNECTIVES[i];
      regexBuilder.append(connective).append('|');
    }
    regexBuilder.append(DOMAIN_CONNECTIVES[length]);
    CONNECTIVE_REGEX = regexBuilder.toString();
  }

  public boolean processNonCodeMembers(PsiType type, PsiScopeProcessor processor, PsiElement place, boolean forCompletion) {
    if (!(place instanceof GrReferenceExpression)) return true;
    if (!(type instanceof PsiClassType)) return true;

    GrReferenceExpression refExpr = (GrReferenceExpression)place;

    PsiClass psiClass = ((PsiClassType)type).resolve();
    if (!isDomainClass(psiClass)) return true;

    return multiResolve(refExpr, psiClass, forCompletion, processor);
  }

  private static boolean multiResolve(GrReferenceExpression myRefExpr,
                                      PsiClass myClass,
                                      boolean forCompletion,
                                      PsiScopeProcessor processor) {
    final DomainClassMembersProvider classMembersProvider = DomainClassMembersProvider.getInstance(myRefExpr.getProject());
    ClassHint classHint = processor.getHint(ClassHint.KEY);

    NameHint nameHint = processor.getHint(NameHint.KEY);
    String name = nameHint == null ? null : nameHint.getName(ResolveState.initial());
    forCompletion = forCompletion || name == null;

    final PsiField[] fields = classMembersProvider.getDCInstanceFields(myClass);
    final boolean isInStaticContext = PsiUtil.isInStaticContext(myRefExpr, myClass);
    if (classHint == null || classHint.shouldProcess(ClassHint.ResolveKind.PROPERTY)) {
      if (!isInStaticContext) {
        for (PsiField field : fields) {
          if (forCompletion || field.getName().equals(name)) {
            if (!processor.execute(field, ResolveState.initial())) return false;
          }
        }
      }
      if (!testForConstraintsProperty(myRefExpr, myClass, processor)) return false;
    }

    if (classHint == null || classHint.shouldProcess(ClassHint.ResolveKind.METHOD)) {
      PsiMethod[] staticMethods = classMembersProvider.getDCStaticMethods(myClass);
      for (PsiMethod method : staticMethods) {
        if (forCompletion || method.getName().equals(name)) {
          if (!processor.execute(method, ResolveState.initial())) return false;
        }
      }
      if (!isInStaticContext) {
        PsiMethod[] instanceMethods = classMembersProvider.getDCInstanceMethods(myClass);
        for (PsiMethod method : instanceMethods) {
          if (forCompletion || method.getName().equals(name)) {
            if (!processor.execute(method, ResolveState.initial())) return false;
          }
        }
      }
      if (!testForStaticFinderMethod(myRefExpr, myClass, processor, fields)) return false;
    }
    return true;
  }

  private static boolean testForConstraintsProperty(GrReferenceExpression myRefExpr, PsiClass myClass, PsiScopeProcessor processor) {
    final VirtualFile file = myClass.getContainingFile().getVirtualFile();
    if (file == null) return true;

    final Project project = myClass.getProject();
    final Module module = ModuleUtil.findModuleForFile(file, project);
    final VirtualFile sourceDirectory = GrailsUtils.findJavaSourceDirectory(module);
    if (sourceDirectory == null) return true;

    final PsiDirectory javaSourceDir = PsiManager.getInstance(project).findDirectory(sourceDirectory);
    if (javaSourceDir == null) return true;

    final GlobalSearchScope searchScope = GlobalSearchScope.directoryScope(javaSourceDir, true);

    final PsiClass psiClass =
      JavaPsiFacade.getInstance(myRefExpr.getProject()).findClass(myClass.getQualifiedName() + "Constraints", searchScope);

    if (!(psiClass instanceof GroovyScriptClass)) return true;
    final GroovyFile groovyFile = (GroovyFile)psiClass.getContainingFile();
    final GrTopStatement[] topStatements = groovyFile.getTopStatements();
    for (GrTopStatement statement : topStatements) {
      if (statement instanceof GrAssignmentExpression) {
        final GrAssignmentExpression expression = (GrAssignmentExpression)statement;
        final GrExpression value = expression.getLValue();
        if (value instanceof GrReferenceExpression) {
          final GrReferenceExpression referenceExpression = (GrReferenceExpression)value;
          if (referenceExpression.getQualifier() == null) {
            if ("constraints".equals(referenceExpression.getName())) {
              if (!processor.execute(referenceExpression, ResolveState.initial())) return false;
            }
          }
        }
      }
    }
    return true;
  }


  private static boolean testForStaticFinderMethod(GrReferenceExpression refExpr,
                                                   PsiClass aClass,
                                                   PsiScopeProcessor processor,
                                                   PsiField[] dcInstanceFields) {
    String name = refExpr.getName();
    assert name != null;
    final Map<String, PsiType> domainFields = DomainClassUtils.getDomainFields(aClass, dcInstanceFields);
    final String[] domainFieldNames = domainFields.keySet().toArray(new String[domainFields.size()]);
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(refExpr.getProject());


    if (startsWith(name, DOMAIN_LIST_ORDER)) {
      final String orderByField = name.substring(DOMAIN_LIST_ORDER.length());
      for (String fieldName : domainFieldNames) {
        if (orderByField.startsWith(StringUtil.capitalize(fieldName))) {
          final String returnType = CommonClassNames.JAVA_UTIL_LIST + '<' + aClass.getQualifiedName() + '>';
          if (!processor.execute(constructDCFinderMethod(factory, returnType, refExpr.getName(), Collections.<PsiType>emptyList(),
                                                         Collections.<String>emptyList(), refExpr), ResolveState.initial())) {
            return false;
          }
          return true;
        }
      }
    }

    String returnType;
    final String expression;
    if (name.startsWith(DOMAIN_COUNT)) {
      expression = name.substring(DOMAIN_COUNT.length());
      returnType = "int";
    }
    else if (name.startsWith(DOMAIN_FIND)) {
      expression = name.substring(DOMAIN_FIND.length());
      returnType = aClass.getQualifiedName();
    }
    else if (name.startsWith(DOMAIN_FIND_ALL)) {
      expression = name.substring(DOMAIN_FIND_ALL.length());
      returnType = CommonClassNames.JAVA_UTIL_LIST + '<' + aClass.getQualifiedName() + '>';
    }
    else {
      return true;
    }

    if (endsWithDomainConnectivity(expression)) return true;
    String[] parts = expression.split(CONNECTIVE_REGEX);
    if (parts.length > 2) return true;

    Set<String> usedFields = new HashSet<String>(domainFields.size());
    List<String> paramNames = new ArrayList<String>(domainFields.size());
    List<PsiType> paramTypes = new ArrayList<PsiType>(domainFields.size());

    for (String part : parts) {
      int finderIndex = extractFinderExpression(part);
      String fieldName;
      String finderExpr;
      if (finderIndex < 0) {
        finderExpr = null;
        fieldName = part;
      }
      else {
        finderExpr = DOMAIN_FINDER_EXPRESSIONS[finderIndex];
        fieldName = part.substring(0, part.length() - finderExpr.length());
      }

      final String capitalizedName = fieldName;
      fieldName = StringUtil.decapitalize(fieldName);
      if (domainFields.get(fieldName) == null) return true;

      if (usedFields.contains(fieldName)) return true;
      usedFields.add(fieldName);

      final PsiType propertyType = domainFields.get(fieldName);
      if (finderExpr == null || ArrayUtil.contains(finderExpr, DOMAIN_FINDER_EXPRESSIONS_WITH_ONE_PARAMETER)) {
        paramNames.add(fieldName);
        paramTypes.add(propertyType);
      }
      else if (finderExpr.equals("Between")) {
        paramNames.add("lower" + capitalizedName);
        paramTypes.add(propertyType);

        paramNames.add("upper" + capitalizedName);
        paramTypes.add(propertyType);
      }
    }

    if (!processor
      .execute(constructDCFinderMethod(factory, returnType, refExpr.getName(), paramTypes, paramNames, refExpr), ResolveState.initial())) {
      return false;
    }
    final PsiType mapType = factory.createTypeFromText(CommonClassNames.JAVA_UTIL_MAP, refExpr);
    paramTypes.add(mapType);
    paramNames.add("paginateParams");
    return processor
      .execute(constructDCFinderMethod(factory, returnType, refExpr.getName(), paramTypes, paramNames, refExpr), ResolveState.initial());
  }

  private static int extractFinderExpression(String part) {
    for (int i = 0; i < DOMAIN_FINDER_EXPRESSIONS.length; i++) {
      if (part.endsWith(DOMAIN_FINDER_EXPRESSIONS[i])) {
        return i;
      }
    }
    return -1;
  }

  private static PsiMethod constructDCFinderMethod(PsiElementFactory factory,
                                                   String returnType,
                                                   String name,
                                                   List<PsiType> paramTypes,
                                                   List<String> paramNames,
                                                   PsiElement context) {
    assert paramTypes.size() == paramNames.size();

    StringBuilder text = new StringBuilder();
    text.append("public static ").append(returnType).append(' ').append(name).append('(');
    for (int i = 0; i < paramTypes.size(); i++) {
      appendMethodParameter(paramTypes, paramNames, text, i).append(',');
    }
    text.append("){}");
    return factory.createMethodFromText(text.toString(), context);
  }

  private static StringBuilder appendMethodParameter(List<PsiType> paramTypes, List<String> paramNames, StringBuilder text, int i) {
    return text.append(paramTypes.get(i).getCanonicalText()).append(' ').append(paramNames.get(i));
  }
}