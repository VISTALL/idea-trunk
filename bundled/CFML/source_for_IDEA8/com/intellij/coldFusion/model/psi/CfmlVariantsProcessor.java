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

package com.intellij.coldFusion.model.psi;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import static com.intellij.psi.PsiModifier.*;
import com.intellij.psi.resolve.JavaMethodResolveHelper;
import com.intellij.psi.scope.BaseScopeProcessor;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 22.04.2009
 * Time: 15:31:28
 * To change this template use File | Settings | File Templates.
 */
abstract public class CfmlVariantsProcessor<T> extends BaseScopeProcessor {

    private final Set<T> myResult = new LinkedHashSet<T>();
    private final String myReferenceName;
    private final JavaMethodResolveHelper myMethods;
    private final boolean myIsMethodCall;
    private final boolean myIsForCompletion;
    private final PsiElement myElement;

    protected CfmlVariantsProcessor(final PsiElement element, final PsiElement parent, @Nullable String referenceName) {
        myElement = element;
        myIsForCompletion = referenceName == null;
        myReferenceName = referenceName;
        myIsMethodCall = parent instanceof CfmlFunctionCallExpression ||
                parent instanceof CfmlTagFunctionCall;
        if (parent instanceof CfmlFunctionCallExpression && !myIsForCompletion) {
            final PsiType[] parameterTypes = ((CfmlFunctionCallExpression) parent).getArgumentTypes();
            myMethods = new JavaMethodResolveHelper(parent, parameterTypes);
        } else {
            myMethods = new JavaMethodResolveHelper(parent, null);
        }
    }

    public boolean execute(final PsiElement element, final ResolveState state) {
        // continue if not a definition
        if (!(element instanceof PsiNamedElement)) {
            return true;
        }

        // continue if has no name
        final PsiNamedElement namedElement = (PsiNamedElement) element;
        if (StringUtil.isEmpty(namedElement.getName())) {
            return true;
        }

        // if declared after using
        if (myElement.getContainingFile() == element.getContainingFile() && myElement.getTextRange().getStartOffset() < element.getTextRange().getStartOffset()) {
            return true;
        }

        // continue if a field or a class
        if (namedElement instanceof PsiField || namedElement instanceof PsiClass) {
            return true;
        }

        // continue if element is hidden (has private modifier, package_local or protected) (?)
        if (namedElement instanceof PsiModifierListOwner) {
            final PsiModifierListOwner owner = (PsiModifierListOwner) namedElement;
            if (owner.hasModifierProperty(PRIVATE) || owner.hasModifierProperty(PACKAGE_LOCAL)
                    || owner.hasModifierProperty(PROTECTED)) {
                return true;
            }
        }

        boolean isJavaMethodCall = namedElement instanceof PsiMethod;
        if (isJavaMethodCall) {
            final PsiMethod method = (PsiMethod) namedElement;
            // continue if constructor (?)
            if (method.isConstructor()) {
                return true;
            }
        }
        boolean isMyMethodCall = namedElement instanceof CfmlTagFunctionDefinition ||
                namedElement instanceof CfmlFunctionDefinition;

        // continue if not the same type as parent
        if (!myIsForCompletion && (isJavaMethodCall || isMyMethodCall) != myIsMethodCall) {
            return true;
        }

        // continue if names differ
        if (!myIsForCompletion && !myReferenceName.equals(namedElement.getName())) {
            return true;
        }

        if (isJavaMethodCall) {
            myMethods.addMethod((PsiMethod)namedElement, state.get(PsiSubstitutor.KEY), false);
            return true;
        }

        addIfNotNull(execute(namedElement, false), myResult);
        return myIsForCompletion || myResult.size() != 1;
    }

    @Nullable
    protected abstract T execute(final PsiNamedElement element, final boolean error);

    public T[] getVariants(T[] array) {
        if (myMethods != null) {
            for (final PsiMethod method : myMethods.getMethods()) {
                addIfNotNull(execute(method, myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE), myResult);
                // addIfNotNull(execute(method.getMethod(), myMethods.getResolveError() == JavaMethodResolveHelper.ErrorType.RESOLVE), myResult);
            }
        }
        return myResult.toArray(array);
    }
}
