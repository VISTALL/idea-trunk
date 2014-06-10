package com.intellij.velocity.psi.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.velocity.psi.VtlCallable;
import com.intellij.velocity.psi.VtlVariable;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Alexey Chmutov
 *         Date: 11.06.2008
 */
class VtlMethod implements VtlCallable {
    private final PsiMethod myMethod;
    private final VtlVariable[] myParameters;

    public VtlMethod(PsiMethod method) {
        myMethod = method;
        PsiParameter[] parameters = method.getParameterList().getParameters();
        myParameters = new VtlVariable[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            myParameters[i] = new Parameter(parameters[i]);
        }
    }

    public VtlVariable[] getParameters() {
        return myParameters;
    }

    public boolean isDeprecated() {
        return myMethod.isDeprecated();
    }

    private static class Parameter extends FakePsiElement implements VtlVariable {
        private final String myName;
        private final PsiParameter myParent;

        public Parameter(@NotNull PsiParameter parent) {
            myName = StringUtil.notNullize(parent.getName());
            myParent = parent;
        }

        @Override
        @NotNull
        public String getName() {
            return myName;
        }

        public PsiElement getParent() {
            return myParent;
        }

        public PsiType getPsiType() {
            return myParent.getType();
        }
    }

}
