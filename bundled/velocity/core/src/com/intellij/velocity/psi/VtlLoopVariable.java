/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.Icons;
import com.intellij.velocity.VelocityBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlLoopVariable extends VtlPresentableNamedElement implements VtlVariable {
    public VtlLoopVariable(final ASTNode node) {
        super(node);
    }

    public String getTypeName() {
        return VelocityBundle.message("type.name.loop.variable");
    }

  public Icon getIcon() {
        return Icons.VARIABLE_ICON;
    }

    public PsiType getPsiType() {
        return extractTypeFromIterable(getIterableExpression());
    }

    @Nullable
    public VtlExpression getIterableExpression() {
        PsiElement wouldBeIterable = getNextSibling();
        while (wouldBeIterable != null) {
            if (wouldBeIterable instanceof VtlExpression) {
                return (VtlExpression) wouldBeIterable;
            }
            wouldBeIterable = wouldBeIterable.getNextSibling();
        }
        return null;
    }

    @Nullable
    private static PsiType extractTypeFromIterable(VtlExpression expr) {
        if (expr == null) {
            return null;
        }
        PsiType type = expr.getPsiType();
        if (type == null) {
            return null;
        }
        if (type instanceof PsiArrayType) {
            return ((PsiArrayType) type).getComponentType();
        }
        if (!(type instanceof PsiClassType)) {
            return null;
        }
        PsiClassType classType = (PsiClassType) type;
        PsiElementFactory factory = JavaPsiFacade.getInstance(expr.getProject()).getElementFactory();
        GlobalSearchScope scope = expr.getResolveScope();

        for (Object[] iterable : VELOCITY_ITERABLES) {
            PsiClassType iterableClassType = factory.createTypeByFQClassName((String) iterable[0], scope);
            if (!TypeConversionUtil.isAssignable(iterableClassType, classType)) {
                continue;
            }
            final PsiClass iterableClass = iterableClassType.resolve();
            if(iterableClass == null) {
                continue;
            }
            final PsiSubstitutor substitutor = PsiUtil.getSuperClassSubstitutor(iterableClass, classType);
            PsiTypeParameter[] paremeters = iterableClass.getTypeParameters();
            int paramIndex = ((Integer)iterable[1]).intValue();
            PsiType result = paramIndex < paremeters.length ? substitutor.substitute(paremeters[paramIndex]) : null;
            return result != null ? result : factory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_OBJECT, scope);
        }
        return null;
    }

    private static final Object[][] VELOCITY_ITERABLES = {
            {"java.util.Iterator", 0},
            {CommonClassNames.JAVA_UTIL_COLLECTION, 0},
            {CommonClassNames.JAVA_UTIL_MAP, 1}
    };

    public static String[] getVelocityIterables(@NotNull String className) {
        return new String[]{
                "java.util.Iterator<" + className + ">",
                "java.util.Collection<" + className + ">",
                "java.util.Map<?, " + className + ">",
                className + "[]",
        };
    }
}