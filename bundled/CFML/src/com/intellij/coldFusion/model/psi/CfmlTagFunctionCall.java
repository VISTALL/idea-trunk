package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
// corresponding text pattern: <cfinvoke ... > 
public class CfmlTagFunctionCall extends CfmlTag/*CfmlCompositeElement*/ implements ICfmlFunctionCall  {
    public CfmlTagFunctionCall(@NotNull ASTNode node) {
        super(node);
    }

    public PsiType getPsiType() {
        return null;
    }

    public CfmlReferenceExpression getReferenceExpression() {
        return findNotNullChildByClass(CfmlReferenceExpression.class);
    }

    public CfmlArgumentList findArgumentList() {
        return null;
    }

    public PsiType[] getArgumentTypes() {
        return new PsiType[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        CfmlReferenceExpression name = findChildByClass(CfmlReferenceExpression.class);
        return name != null ? name.getName() : null;
    }

    /*
    public CfmlVariable[] getParameters() {
        LinkedList<CfmlVariable> result = new LinkedList<CfmlVariable>();
        CfmlTag[] tags = findChildrenByClass(CfmlTag.class);
        for (CfmlTag tag : tags) {
            if ("cfinvokearguments".equals(tag.getName())) {
                result.add((CfmlVariable)tag.getNode().findChildByType(CfmlCompositeElementTypes.NAMED_ATTRIBUTE).getPsi());
            }
        }
        return result.toArray(new CfmlVariable[0]);
    }
    */
}
