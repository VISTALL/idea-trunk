/*
 * Copyright 2005-2006 Olivier Descout
 *
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
package org.intellij.idea.lang.javascript.intention.initialization;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.SmartPointerManager;
import com.intellij.util.IncorrectOperationException;
import org.intellij.idea.lang.javascript.intention.JSElementPredicate;
import org.intellij.idea.lang.javascript.intention.JSIntention;
import org.intellij.idea.lang.javascript.psiutil.ErrorUtil;
import org.intellij.idea.lang.javascript.psiutil.JSElementFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JSSplitDeclarationAndInitializationIntention extends JSIntention {
    @NonNls private static final String VAR_KEYWORD = "var ";

    @NotNull
    protected JSElementPredicate getElementPredicate() {
        return new Predicate();
    }

    public void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        assert (element instanceof JSVarStatement);

        StringBuilder        declarationBuffer = new StringBuilder();
        List<String>         initializations   = new ArrayList<String>();

      SmartPsiElementPointer<JSVarStatement> pointer = SmartPointerManager.getInstance(element.getProject()).
        createSmartPsiElementPointer((JSVarStatement)element);

        for (int i = 0; i < pointer.getElement().getVariables().length; i++) {
          JSVariable variable = pointer.getElement().getVariables()[i];

            declarationBuffer.append((declarationBuffer.length() == 0) ? VAR_KEYWORD : ",")
                             .append(variable.getName());

            String s = JSPsiImplUtils.getTypeFromDeclaration(variable);
            final PsiFile containingFile = element.getContainingFile();

            if (s == null && containingFile.getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4) {
                s = JSResolveUtil.getQualifiedExpressionType(variable.getInitializer(), containingFile);
                s = BaseCreateMethodsFix.importAndShortenReference(s, variable, true, true);
            }

            if (s != null) {
                declarationBuffer.append(":").append(s);
            }
            if (variable.hasInitializer()) {
                initializations.add(variable.getName() + '=' + variable.getInitializer().getText() + ';');
            }
        }
        declarationBuffer.append(';');

        // Do replacement.
        JSStatement newStatement = JSElementFactory.replaceStatement(pointer.getElement(), declarationBuffer.toString());

        for (final String initialization : initializations) {
            newStatement = JSElementFactory.addStatementAfter(newStatement, initialization);
        }
    }

    private static class Predicate implements JSElementPredicate {
        public boolean satisfiedBy(@NotNull PsiElement element) {
            PsiElement elementParent;

            if (!(element instanceof JSVarStatement) ||
                (elementParent = element.getParent()) instanceof JSForStatement ||
                elementParent instanceof JSClass
               ) {
                return false;
            }

            final JSVarStatement varStatement = (JSVarStatement) element;
            if (ErrorUtil.containsError(varStatement)) {
                return false;
            }

            for (JSVariable variable : varStatement.getVariables()) {
                if (variable.hasInitializer()) {
                    return true;
                }
            }
            return false;
        }
    }
}
