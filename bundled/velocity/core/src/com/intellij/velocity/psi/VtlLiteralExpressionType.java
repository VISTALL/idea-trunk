package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.directives.VtlFileReferenceDirective;
import com.intellij.velocity.psi.directives.VtlMacroCall;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 07.06.2008
 */
public class VtlLiteralExpressionType extends VtlCompositeElementType {
    private final String myTypeName;
    private final PsiPrimitiveType myPrimitiveType;

    public VtlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull String typeName) {
        super(debugName);
        myTypeName = typeName;
        myPrimitiveType = null;
    }

    public VtlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull PsiType primitiveType) {
        super(debugName);
        myTypeName = null;
        assert primitiveType instanceof PsiPrimitiveType;
        myPrimitiveType = (PsiPrimitiveType) primitiveType;
    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return VtlCompositeElementTypes.STRING_LITERALS.contains(this)
                ? new VtlStringLiteral(node)
                : new VtlLiteralExpression(node);
    }

    public class VtlLiteralExpression extends VtlCompositeElement implements VtlExpression {
        public VtlLiteralExpression(@NotNull final ASTNode node) {
            super(node);
        }

        public PsiType getPsiType() {
            if (myPrimitiveType != null) {
                return myPrimitiveType;
            }
            return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(myTypeName, getResolveScope());
        }
    }

    public class VtlStringLiteral extends VtlLiteralExpression {

        public VtlStringLiteral(@NotNull final ASTNode node) {
            super(node);
        }

        @NotNull
        public PsiReference[] getReferences() {
            if (isFileReference()) {
                return PsiUtil.getFileReferences(getValueText(), this, getFirstChild().getTextLength(), true);
            }
            if (isPropertyReference()) {
                return new PsiReference[]{new PropertyReference(getValueText(), this, null, true)};
            }
            return PsiReference.EMPTY_ARRAY;
        }

        private boolean isFileReference() {
            return isStringLiteralAndArgumentOf(VtlFileReferenceDirective.class);
        }

        private boolean isPropertyReference() {
            if (!isStringLiteralAndArgumentOf(VtlMacroCall.class) || getPrevSibling() != null) {
                return false;
            }
            VtlMacroCall macroCall = (VtlMacroCall) getParent().getParent();
            String macroName = macroCall.getReferenceExpression().getReferenceName();
            return "springMessageText".equals(macroName) || "springMessage".equals(macroName)
                   || "springThemeText".equals(macroName) || "springTheme".equals(macroName);
        }

        private String getValueText() {
            String text = getText();
            return text.substring(1, text.length() - 1);
        }

        private boolean isStringLiteralAndArgumentOf(Class<? extends VtlDirective> directiveClass) {
            final PsiElement parent = getParent();
            return parent instanceof VtlArgumentList && directiveClass.isInstance(parent.getParent());
        }

        public TextRange getValueRange() {
          return new TextRange(1, getText().length() - 1);
        }

        public VtlStringLiteral setStringValue(final TextRange range, final String newContent) {
          String oldText = getText();
          String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
          final PsiElement newElement = PsiUtil.createStringLiteral(getProject(), newText);
          final ASTNode newNode = newElement.getNode();
          getParent().getNode().replaceChild(getNode(), newNode);
          return (VtlStringLiteral) newNode.getPsi();
        }
    }
}
