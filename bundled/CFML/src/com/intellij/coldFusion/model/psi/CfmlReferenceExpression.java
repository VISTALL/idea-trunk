package com.intellij.coldFusion.model.psi;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.coldFusion.UI.editorActions.completionProviders.CfmlTailType;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 * Date: 11.02.2009
 */
public class CfmlReferenceExpression extends AbstractQualifiedReference<CfmlReferenceExpression> implements CfmlExpression {
    protected CfmlReferenceExpression(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected boolean processVariantsInner(PsiScopeProcessor processor) {
        // getting qualifier inner
        CfmlReferenceExpression qualifier = null;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof CfmlReferenceExpression) {
                qualifier = (CfmlReferenceExpression) child;
            }
            if (child instanceof CfmlFunctionCallExpression) {
                qualifier = ((CfmlFunctionCallExpression) child).getReferenceExpression();
            }
            child = child.getNextSibling();
        }
        // qualifier = null;
        if (qualifier == null) {
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

    @Override
    public boolean isReferenceTo(PsiElement element) {
        final PsiManager manager = getManager();
        for (final ResolveResult result : multiResolve(false)) {
            final PsiElement target = result.getElement();
            if (manager.areElementsEquivalent(element, target)) {
                return true;
            }
        }
        return false;
        //return super.isReferenceTo(element);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected ResolveResult[] resolveInner() {
        final String referenceName = getReferenceName();
        if (referenceName == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        final PsiElement parent = getParent();
        if (parent instanceof CfmlAssignment) {
            CfmlAssignment assignment = (CfmlAssignment) parent;
            CfmlVariable var = assignment.getAssignedVariable();
            if (var != null && assignment.getAssignedVariableElement() == this) {
                return new ResolveResult[]{new PsiElementResolveResult(var)};
            }
        }

        final CfmlVariantsProcessor<ResolveResult> processor = new CfmlVariantsProcessor<ResolveResult>(this, getParent(), referenceName) {
            protected ResolveResult execute(final PsiNamedElement element, final boolean error) {
                return new PsiElementResolveResult(element, false);
            }
        };
        processVariantsInner(processor);
        return processor.getVariants(ResolveResult.EMPTY_ARRAY);
    }

    @NotNull
    protected CfmlReferenceExpression parseReference(String newText) {
        // TODO: to write proper code
        return null;//(TreeElement) CfmlChangeUtil.createNameIdentifier(getProject(), newText);
    }

    protected PsiElement getSeparator() {
        return findChildByType(CfscriptTokenTypes.POINT);
    }

    protected PsiElement getReferenceNameElement() {
        PsiElement identifier = findChildByType(CfscriptTokenTypes.IDENTIFIER);

        return identifier != null ? identifier : findChildByType(CfmlTokenTypes.STRING_TEXT);
    }

    public Object[] getVariants() {
        final CfmlVariantsProcessor<PsiNamedElement> processor = new CfmlVariantsProcessor<PsiNamedElement>(this, getParent(), null) {
            protected PsiNamedElement execute(final PsiNamedElement element, final boolean error) {
                return element;
            }
        };
        processVariantsInner(processor);
        PsiNamedElement[] variants = processor.getVariants(PsiNamedElement.EMPTY_ARRAY);

        Object[] result = ContainerUtil.map2Array(variants, Object.class, new Function<PsiNamedElement, Object>() {
            public Object fun(final PsiNamedElement element) {
                LookupElementBuilder lookupElement = LookupElementBuilder.create(element, element.getName());
                if (element instanceof CfmlVariable) {
                    PsiType type = ((CfmlVariable) element).getPsiType();
                    if (type != null) {
                      lookupElement = lookupElement.setTypeText(type.getPresentableText());
                    }
                } else if (element instanceof PsiMethod) {
                  return TailTypeDecorator.withTail(lookupElement, CfmlTailType.PARENTHS);
                }
                return lookupElement;
            }
        });
        return result;
    }

    public PsiType getPsiType() {
        final PsiElement element = resolve();
        if (element instanceof CfmlVariable) {
            return ((CfmlVariable)element).getPsiType();
        }
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod)element;
            return getSubstitutedType(method, method.getReturnType());
        }
        return null;
    }

    @Nullable
    private CfmlReferenceExpression getQualifierInner() {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof CfmlReferenceExpression) {
                return (CfmlReferenceExpression) child;
            }
            if (child instanceof CfmlFunctionCallExpression) {
                return ((CfmlFunctionCallExpression) child).getReferenceExpression();
            }
            child = child.getNextSibling();
        }
        return null;
    }

    // TODO: check the work
    private PsiType getSubstitutedType(PsiMethod method, PsiType result) {
        if (!(result instanceof PsiClassType)) {
            return result;
        }
        PsiClassType resultClassType = (PsiClassType) result;
        PsiClassType qualifierClassType = (PsiClassType) getQualifierInner().getPsiType();
        assert qualifierClassType != null;
        final PsiClassType.ClassResolveResult classResolveResult = qualifierClassType.resolveGenerics();
        final PsiSubstitutor substitutor = TypeConversionUtil.getSuperClassSubstitutor(method.getContainingClass(),
                classResolveResult.getElement(), classResolveResult.getSubstitutor());
        return substitutor.substitute(resultClassType);
    }
    public String toString() {
        return getNode().getElementType().toString();
    }
}
