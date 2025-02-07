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

package org.jetbrains.plugins.groovy.lang.resolve.processors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.scope.JavaScopeProcessorEvent;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.branch.GrReturnStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrGdkMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyResolveResultImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.*;

/**
 * @author ven
 */
public class MethodResolverProcessor extends ResolverProcessor {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.lang.resolve.processors.MethodResolverProcessor");
  private final PsiType myThisType;
  @Nullable
  private PsiType[] myArgumentTypes;
  private final PsiType[] myTypeArguments;

  private final Set<GroovyResolveResult> myInapplicableCandidates = new LinkedHashSet<GroovyResolveResult>();
  private final boolean myIsConstructor;

  private boolean myStopExecuting = false;

  public MethodResolverProcessor(String name, GroovyPsiElement place, boolean isConstructor, PsiType thisType, @Nullable PsiType[] argumentTypes, PsiType[] typeArguments) {
    super(name, EnumSet.of(ResolveKind.METHOD, ResolveKind.PROPERTY), place, PsiType.EMPTY_ARRAY);
    myIsConstructor = isConstructor;
    myThisType = thisType;
    myArgumentTypes = argumentTypes;
    myTypeArguments = typeArguments;
  }

  public boolean execute(PsiElement element, ResolveState state) {
    if (myStopExecuting) {
      return false;
    }
    PsiSubstitutor substitutor = state.get(PsiSubstitutor.KEY);
    if (element instanceof PsiMethod) {
      PsiMethod method = (PsiMethod) element;
      if (method.isConstructor() != myIsConstructor) return true;
      if (substitutor == null) substitutor = PsiSubstitutor.EMPTY;
      substitutor = obtainSubstitutor(substitutor, method);
      boolean isAccessible = isAccessible(method);
      boolean isStaticsOK = isStaticsOK(method);
      if (PsiUtil.isApplicable(myArgumentTypes, method, substitutor, myCurrentFileResolveContext instanceof GrMethodCallExpression)) {
        myCandidates.add(new GroovyResolveResultImpl(method, myCurrentFileResolveContext, substitutor, isAccessible, isStaticsOK));
      } else {
        myInapplicableCandidates.add(new GroovyResolveResultImpl(method, myCurrentFileResolveContext, substitutor, isAccessible, isStaticsOK));
      }

      return true;
    } else if (element instanceof PsiVariable) {
      if (element instanceof GrField && ((GrField) element).isProperty() ||
          isClosure((PsiVariable) element)) {
        return super.execute(element, state);
      } else {
        myInapplicableCandidates.add(new GroovyResolveResultImpl(element, myCurrentFileResolveContext, substitutor, isAccessible((PsiVariable)element), isStaticsOK((PsiVariable)element)));
      }
    }


    return true;
  }

  private PsiSubstitutor obtainSubstitutor(PsiSubstitutor substitutor, PsiMethod method) {
    final PsiTypeParameter[] typeParameters = method.getTypeParameters();
    if (myTypeArguments.length == typeParameters.length) {
      for (int i = 0; i < typeParameters.length; i++) {
        PsiTypeParameter typeParameter = typeParameters[i];
        final PsiType typeArgument = myTypeArguments[i];
        substitutor = substitutor.put(typeParameter, typeArgument);
      }
      return substitutor;
    }

    if (argumentsSupplied() && method.hasTypeParameters()) {
      PsiType[] argTypes = myArgumentTypes;
      if (method instanceof GrGdkMethod) {
        assert argTypes != null;
        //type inference should be performed from static method
        PsiType[] newArgTypes = new PsiType[argTypes.length + 1];
        newArgTypes[0] = myThisType;
        System.arraycopy(argTypes, 0, newArgTypes, 1, argTypes.length);
        argTypes = newArgTypes;

        method = ((GrGdkMethod) method).getStaticMethod();
        LOG.assertTrue(method.isValid());
      }
      return inferMethodTypeParameters(method, substitutor, typeParameters, argTypes);
    }

    return substitutor;
  }

  private static boolean isClosure(PsiVariable variable) {
    if (variable instanceof GrVariable) {
      final PsiType type = ((GrVariable) variable).getTypeGroovy();
      return type != null && type.equalsToText(GrClosableBlock.GROOVY_LANG_CLOSURE);
    }
    return variable.getType().equalsToText(GrClosableBlock.GROOVY_LANG_CLOSURE);
  }

  private PsiSubstitutor inferMethodTypeParameters(PsiMethod method, PsiSubstitutor partialSubstitutor, final PsiTypeParameter[] typeParameters, final PsiType[] argTypes) {
    if (typeParameters.length == 0) return partialSubstitutor;

    if (argumentsSupplied()) {
      final PsiParameter[] parameters = method.getParameterList().getParameters();
      final int max = Math.max(parameters.length, argTypes.length);
      PsiType[] parameterTypes = new PsiType[max];
      PsiType[] argumentTypes = new PsiType[max];
      for (int i = 0; i < parameterTypes.length; i++) {
        if (i < parameters.length) {
          final PsiType type = parameters[i].getType();
          if (argTypes.length == parameters.length &&
              type instanceof PsiEllipsisType &&
              !(argTypes[argTypes.length - 1] instanceof PsiArrayType)) {
            parameterTypes[i] = ((PsiEllipsisType) type).getComponentType();
          } else {
            parameterTypes[i] = type;
          }
        } else {
          if (parameters.length > 0) {
            final PsiType lastParameterType = parameters[parameters.length - 1].getType();
            if (argTypes.length > parameters.length && lastParameterType instanceof PsiEllipsisType) {
              parameterTypes[i] = ((PsiEllipsisType) lastParameterType).getComponentType();
            } else {
              parameterTypes[i] = lastParameterType;
            }
          } else {
            parameterTypes[i] = PsiType.NULL;
          }
        }
        argumentTypes[i] = i < argTypes.length ? argTypes[i] : PsiType.NULL;
      }

      final PsiResolveHelper helper = JavaPsiFacade.getInstance(method.getProject()).getResolveHelper();
      PsiSubstitutor substitutor = helper.inferTypeArguments(typeParameters, parameterTypes, argumentTypes, LanguageLevel.HIGHEST);
      for (PsiTypeParameter typeParameter : typeParameters) {
        if (!substitutor.getSubstitutionMap().containsKey(typeParameter)) {
          substitutor = inferFromContext(typeParameter, method.getReturnType(), substitutor, helper);
        }
      }

      return partialSubstitutor.putAll(substitutor);
    }

    return partialSubstitutor;
  }

  private PsiSubstitutor inferFromContext(PsiTypeParameter typeParameter, PsiType lType, PsiSubstitutor substitutor, PsiResolveHelper helper) {
    if (myPlace != null) {
      final PsiType inferred = helper.getSubstitutionForTypeParameter(typeParameter, lType, getContextType(), false, LanguageLevel.HIGHEST);
      if (inferred != PsiType.NULL) {
        return substitutor.put(typeParameter, inferred);
      }
    }
    return substitutor;
  }

  @Nullable
  private PsiType getContextType() {
    final PsiElement parent = myPlace.getParent().getParent();
    PsiType rType = null;
    if (parent instanceof GrReturnStatement) {
      final GrMethod method = PsiTreeUtil.getParentOfType(parent, GrMethod.class);
      if (method != null) rType = method.getDeclaredReturnType();
    }
    else if (parent instanceof GrAssignmentExpression && myPlace.equals(((GrAssignmentExpression)parent).getRValue())) {
      rType = ((GrAssignmentExpression)parent).getLValue().getType();
    }
    else if (parent instanceof GrVariable) {
      rType = ((GrVariable)parent).getDeclaredType();
    }
    return rType;
  }

  public GroovyResolveResult[] getCandidates() {
    if (!myCandidates.isEmpty()) {
      return filterCandidates();
    }
    if (!myInapplicableCandidates.isEmpty()) {
      return ResolveUtil.filterSameSignatureCandidates(myInapplicableCandidates);
    }
    return GroovyResolveResult.EMPTY_ARRAY;
  }

  private GroovyResolveResult[] filterCandidates() {
    GroovyResolveResult[] array = myCandidates.toArray(new GroovyResolveResult[myCandidates.size()]);
    if (array.length == 1) return array;

    List<GroovyResolveResult> result = new ArrayList<GroovyResolveResult>();
    result.add(array[0]);

    PsiManager manager = myPlace.getManager();
    GlobalSearchScope scope = myPlace.getResolveScope();

    boolean methodsPresent = array[0].getElement() instanceof PsiMethod;
    boolean propertiesPresent = !methodsPresent;
    Outer:
    for (int i = 1; i < array.length; i++) {
      PsiElement currentElement = array[i].getElement();
      if (currentElement instanceof PsiMethod) {
        methodsPresent = true;
        PsiMethod currentMethod = (PsiMethod) currentElement;
        for (Iterator<GroovyResolveResult> iterator = result.iterator(); iterator.hasNext();) {
          final GroovyResolveResult otherResolveResult = iterator.next();
          PsiElement element = otherResolveResult.getElement();
          if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            if (dominated(currentMethod, array[i].getSubstitutor(), method, otherResolveResult.getSubstitutor(), manager, scope)) {
              continue Outer;
            } else
            if (dominated(method, otherResolveResult.getSubstitutor(), currentMethod, array[i].getSubstitutor(), manager, scope)) {
              iterator.remove();
            }
          }
        }
      } else {
        propertiesPresent = true;
      }

      result.add(array[i]);
    }

    if (methodsPresent && propertiesPresent) {
      for (Iterator<GroovyResolveResult> iterator = result.iterator(); iterator.hasNext();) {
        GroovyResolveResult resolveResult = iterator.next();
        if (!(resolveResult.getElement() instanceof PsiMethod)) iterator.remove();
      }
    }

    return result.toArray(new GroovyResolveResult[result.size()]);
  }

  private boolean dominated(PsiMethod method1, PsiSubstitutor substitutor1, PsiMethod method2, PsiSubstitutor substitutor2, PsiManager manager, GlobalSearchScope scope) {  //method1 has more general parameter types thn method2
    if (!method1.getName().equals(method2.getName())) return false;

    //hack for default gdk methods
    if (method1 instanceof GrGdkMethod && method2 instanceof GrGdkMethod) {
      method1 = ((GrGdkMethod)method1).getStaticMethod();
      method2 = ((GrGdkMethod)method2).getStaticMethod();
    }
    PsiParameter[] params1 = method1.getParameterList().getParameters();
    PsiParameter[] params2 = method2.getParameterList().getParameters();
    if (myArgumentTypes == null && params1.length != params2.length) return false;

    if (params1.length < params2.length) {
      if (params1.length == 0) return false;
      final PsiType lastType = params1[params1.length - 1].getType(); //varargs applicability
      return lastType instanceof PsiArrayType;
    }

    for (int i = 0; i < params2.length; i++) {
      PsiType type1 = substitutor1.substitute(params1[i].getType());
      PsiType type2 = substitutor2.substitute(params2[i].getType());
      if (!typesAgree(manager, scope, type1, type2)) return false;
    }

    return true;
  }

  private boolean typesAgree(PsiManager manager, GlobalSearchScope scope, PsiType type1, PsiType type2) {
    if (argumentsSupplied() && type1 instanceof PsiArrayType && !(type2 instanceof PsiArrayType)) {
      type1 = ((PsiArrayType) type1).getComponentType();
    }
    return argumentsSupplied() ? //resolve, otherwise same_name_variants
        TypesUtil.isAssignable(type1, type2, manager, scope) :
        type1.equals(type2);
  }

  private boolean argumentsSupplied() {
    return myArgumentTypes != null;
  }


  public boolean hasCandidates() {
    return super.hasCandidates() || !myInapplicableCandidates.isEmpty();
  }

  @Nullable
  public PsiType[] getArgumentTypes() {
    return myArgumentTypes;
  }

  public void setArgumentTypes(@Nullable PsiType[] argumentTypes) {
    myArgumentTypes = argumentTypes;
  }

  @Override
  public void handleEvent(Event event, Object associated) {
    super.handleEvent(event, associated);
    if (JavaScopeProcessorEvent.CHANGE_LEVEL == event && myCandidates.size() > 0) {
      myStopExecuting = true;
    }
  }
}
