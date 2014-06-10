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

package com.intellij.velocity.psi.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.lang.ASTNode;
import static com.intellij.openapi.util.text.StringUtil.join;
import com.intellij.psi.*;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import static com.intellij.velocity.VelocityBundle.message;
import com.intellij.velocity.editorActions.VtlTailType;
import com.intellij.velocity.psi.*;
import static com.intellij.velocity.psi.PsiUtil.createVtlReferenceExpression;
import static com.intellij.velocity.psi.PsiUtil.getPresentableText;
import static com.intellij.velocity.psi.VtlElementTypes.JAVA_DOT;
import com.intellij.velocity.psi.directives.VtlAssignment;
import com.intellij.velocity.psi.directives.VtlMacroCall;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlReferenceExpression extends AbstractQualifiedReference<VtlReferenceExpression> implements VtlExpression {

    public VtlReferenceExpression(@NotNull final ASTNode node) {
        super(node);
    }

    @Override
    protected boolean processVariantsInner(PsiScopeProcessor processor) {
        final VtlReferenceExpression qualifier = getQualifierInner();
        if (qualifier == null) {
            PsiElement parent = getParent();
            if (parent instanceof VtlMacroCall) {
                final VtlFile containingFile = ((VtlMacroCall) parent).getContainingFile();
                return containingFile.processAllMacrosInScope(processor, ResolveState.initial());
            }
            return processUnqualifiedVariants(processor);
        }
        final PsiType type = qualifier.getPsiType();
        if (type instanceof PsiClassType) {
            final PsiClass psiClass = com.intellij.psi.util.PsiUtil.resolveClassInType(type);
            if (psiClass != null && !psiClass.processDeclarations(processor, ResolveState.initial(), null, this)) {
                return false;
            }
        }
        final PsiElement psiElement = qualifier.resolve();
        return psiElement == null || psiElement.processDeclarations(processor, ResolveState.initial(), null, this);
    }

    public boolean hasQualifier() {
        return getQualifierInner() != null;
    }

    public boolean isQualifierResolved() {
        VtlReferenceExpression qualifier = getQualifierInner();
        return qualifier == null || qualifier.resolve() != null;
    }

    @Nullable
    private VtlReferenceExpression getQualifierInner() {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof VtlReferenceExpression) {
                return (VtlReferenceExpression) child;
            }
            if (child instanceof VtlMethodCallExpression) {
                return ((VtlMethodCallExpression) child).getReferenceExpression();
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Nullable
    public VtlReferenceExpression getParentReferenceExpression() {
        PsiElement parent = getParent();
        if (parent instanceof VtlReferenceExpression) {
            return (VtlReferenceExpression) parent;
        }
        return null;
    }

    protected ResolveResult[] resolveInner() {
        final String referenceName = getReferenceName();
        if (referenceName == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        final PsiElement parent = getParent();
        if (parent instanceof VtlAssignment) {
            VtlAssignment assignment = (VtlAssignment) parent;
            VtlVariable var = assignment.getAssignedVariable();
            if (var != null && assignment.getAssignedVariableElement() == this) {
                return new ResolveResult[]{new PsiElementResolveResult(var)};
            }
        }

        final VtlVariantsProcessor<ResolveResult> processor = new VtlVariantsProcessor<ResolveResult>(parent, referenceName, false) {
            protected ResolveResult execute(final PsiNamedElement element, final boolean error) {
                if (element instanceof BeanPropertyElement) {
                    return new PsiElementResolveResult(((BeanPropertyElement) element).getMethod());
                }
                return new PsiElementResolveResult(element, !error);
            }
        };
        processVariantsInner(processor);
        return processor.getVariants(ResolveResult.EMPTY_ARRAY, Character.isLowerCase(referenceName.charAt(0)));
    }

    @NotNull
    public String getUnresolvedMessage(boolean resolvedWithError) {
        final String referenceName = getReferenceName();
        PsiElement parent = getParent();
        if (parent instanceof VtlMacroCall) {
            String msgKey = resolvedWithError ? "error.wrong.number.of.arguments.for.macro" : "error.cannot.resolve.macro";
            return message(msgKey, referenceName);
        }
        final VtlReferenceExpression qualifier = getQualifierInner();
        if (qualifier == null) {
            return message("error.cannot.resolve.variable", referenceName);
        }
        String typeName = getPresentableText(qualifier.getPsiType());
        if (!(parent instanceof VtlMethodCallExpression)) {
            return message("error.cannot.resolve.property", referenceName, typeName);
        }
        if (!resolvedWithError) {
            return message("error.cannot.resolve.method", referenceName, typeName);
        }
        String argumentTypes = join(((VtlMethodCallExpression) parent).getArgumentTypes(), new Function<PsiType, String>() {
            public String fun(final PsiType psiType) {
                return getPresentableText(psiType);
            }
        }, ", ");
        return message("error.no.applicable.method", referenceName, typeName, "(" + argumentTypes + ")");
    }

    @NotNull
    protected VtlReferenceExpression parseReference(String newText) {
        if (!(getParent() instanceof VtlMethodCallExpression)) {
            final String propertyName = VelocityNamingUtil.getPropertyName(newText, isFirstCharInLowerCase());
            if (propertyName != null) {
                newText = propertyName;
            }
        }
        return createVtlReferenceExpression(newText, getProject());
    }


    protected PsiElement getSeparator() {
        return findChildByType(JAVA_DOT);
    }

    @Override
    public String toString() {
        return getNode().getElementType().toString();
    }

    public PsiElement getReferenceNameElement() {
        return findChildByType(VtlElementTypes.IDENTIFIER);
    }

    @Override
    public boolean isReferenceTo(final PsiElement element) {
        final PsiManager manager = getManager();
        for (final ResolveResult result : multiResolve(false)) {
            final PsiElement target = result.getElement();
            if (manager.areElementsEquivalent(element, target)) {
                return true;
            }
            if (target instanceof BeanPropertyElement
                    && manager.areElementsEquivalent(element, ((BeanPropertyElement) target).getMethod())) {
                return true;
            }
        }
        return false;
    }

    public Object[] getVariants() {
        return getVariants(false);
    }

    public Object[] getVariants(boolean propertiesOnly) {
        final VtlVariantsProcessor<PsiNamedElement> processor = new VtlVariantsProcessor<PsiNamedElement>(getParent(), null, propertiesOnly) {
            protected PsiNamedElement execute(final PsiNamedElement element, final boolean error) {
                return element;
            }
        };
        processVariantsInner(processor);
        PsiNamedElement[] variants = processor.getVariants(PsiNamedElement.EMPTY_ARRAY, isFirstCharInLowerCase());

        return ContainerUtil.map2Array(variants, Object.class, new Function<PsiNamedElement, Object>() {
            public Object fun(final PsiNamedElement element) {
                LookupElementBuilder lookupElement = LookupElementBuilder.create(element);
                if (element instanceof VtlVariable) {
                    PsiType type = ((VtlVariable) element).getPsiType();
                    if (type != null) {
                        lookupElement = lookupElement.setTypeText(type.getPresentableText());
                    }
                } else if (element instanceof BeanPropertyElement) {
                  final PsiType type = ((BeanPropertyElement)element).getPropertyType();
                  if (type != null) {
                    lookupElement = lookupElement.setTypeText(type.getPresentableText());
                  }
                }
                else if (element instanceof PsiMethod) {
                  return TailTypeDecorator.withTail(lookupElement, VtlTailType.METHOD_CALL_TAIL_TYPE);
                }
                else if (element instanceof VtlMacro) {
                  PsiElement sibling = getPrevSibling();
                  boolean closingBraceNeeded = sibling != null && "{".equals(sibling.getText());
                  return TailTypeDecorator.withTail(lookupElement, new VtlTailType(closingBraceNeeded));
                }
                return lookupElement;
            }
        });
    }

    protected boolean isFirstCharInLowerCase() {
        final String referenceName = getReferenceName();
        return referenceName == null || Character.isLowerCase(referenceName.charAt(0));
    }

    @NotNull
    public VtlCallable[] getCallableCandidates() {
        final String referenceName = getReferenceName();
        if (referenceName == null) {
            return VtlCallable.EMPTY_ARRAY;
        }
        VtlVariantsProcessor<VtlCallable> processor = new VtlVariantsProcessor<VtlCallable>(getParent(), null, false) {
            protected VtlCallable execute(final PsiNamedElement element, final boolean error) {
                if (!referenceName.equals(element.getName())) {
                    return null;
                }
                if (element instanceof VtlMacro) {
                    return (VtlMacro) element;
                }
                if (element instanceof PsiMethod) {
                    return new VtlMethod((PsiMethod) element);
                }
                return null;
            }
        };
        processVariantsInner(processor);
        return processor.getVariants(VtlCallable.EMPTY_ARRAY, Character.isLowerCase(referenceName.charAt(0)));
    }

    @Nullable
    public PsiType getPsiType() {
        final PsiElement element = resolve();
        if (element instanceof VtlVariable) {
            return ((VtlVariable) element).getPsiType();
        }
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            return getSubstitutedType(method, method.getReturnType());
        }
        if (element instanceof BeanProperty) {
            final BeanProperty beanProperty = (BeanProperty) element;
            return getSubstitutedType(beanProperty.getMethod(), beanProperty.getPropertyType());
        }
        return null;
    }

    private PsiType getSubstitutedType(PsiMethod method, PsiType result) {
        if (!(result instanceof PsiClassType)) {
            return result;
        }
        PsiClassType resultClassType = (PsiClassType) result;
//        if (!resultClassType.hasParameters()) { disabled due to parameter type in itself returns false from hasParameters()
//            return result;
//        }
        PsiClassType qualifierClassType = (PsiClassType) getQualifierInner().getPsiType();
        assert qualifierClassType != null;
        final PsiSubstitutor substitutor = PsiUtil.getSuperClassSubstitutor(method.getContainingClass(), qualifierClassType);
        return substitutor.substitute(resultClassType);
    }

    @Override
    public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
        CheckUtil.checkWritable(this);
        final PsiElement newReferenceName = parseReference(newElementName).getReferenceNameElement();
        getNode().replaceChild(getReferenceNameElement().getNode(), newReferenceName.getNode());
        return this;
    }

}

