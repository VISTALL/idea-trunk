package com.intellij.seam.utils.beans;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.jsp.el.impl.ELResolveUtil;
import com.intellij.psi.jsp.el.ELExpression;
import com.intellij.psi.jsp.el.ELExpressionHolder;
import com.intellij.psi.jsp.el.ELSelectExpression;
import com.intellij.psi.jsp.el.ELVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.seam.model.xml.components.SeamDomFactory;
import com.intellij.seam.utils.SeamCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class DomFactoryContextVariable extends ContextVariable {
  private final SeamDomFactory myFactory;
  private final Set<ContextVariable> myVars;
  private final Module myModule;
  private static Key<Boolean> IS_PROCESSING_VAR_TYPE = new Key<Boolean>("IS_PROCESSING_VAR_TYPE");

  public DomFactoryContextVariable(SeamDomFactory factory, String factoryName, Set<ContextVariable> vars, Module module) {
    super(factory, factoryName, PsiType.VOID);
    myFactory = factory;
    myVars = vars;
    myModule = module;
  }

  @NotNull
  @Override
  public PsiType getType() {
    final PsiType type = SeamCommonUtils.getFactoryType(myFactory, myVars);

    return type != null ? type : SeamCommonUtils.getObjectClassType(myModule.getProject());
  }

  @Nullable
  public PsiType getELExpressionType() {
    XmlAttributeValue context = myFactory.getValue().getXmlAttributeValue();

    if (context == null || isProcessing(context)) return null;

    final String value = myFactory.getValue().getStringValue();  // aliasing 3.2.7
    if (value != null && SeamCommonUtils.isElText(value)) {
      final Ref<PsiType> injectionType = new Ref<PsiType>();

      setProcessing(context, true);

      ((PsiLanguageInjectionHost)context).processInjectedPsi(new PsiLanguageInjectionHost.InjectedPsiVisitor() {
        public void visit(@NotNull final PsiFile injectedPsi, @NotNull final List<PsiLanguageInjectionHost.Shred> places) {
          final PsiElement at = injectedPsi.findElementAt(injectedPsi.getTextLength() - 1);
          final ELExpressionHolder holder = PsiTreeUtil.getParentOfType(at, ELExpressionHolder.class);
          if (holder != null) {
            ELExpression expression = PsiTreeUtil.getChildOfType(holder, ELExpression.class);

            if (expression != null && !isSelfReference(expression, getName())) {
              injectionType.set(ELResolveUtil.resolveContextAsType(expression));
            }
          }
        }
      });

      setProcessing(context, false);

      return injectionType.get();
    }
    return null;
  }

  private static void setProcessing(@NotNull PsiElement context, boolean b) {
    context.putUserData(IS_PROCESSING_VAR_TYPE, b);
  }

  private static boolean isProcessing(@NotNull PsiElement context) {
    final Boolean isProcessing = context.getUserData(IS_PROCESSING_VAR_TYPE);

    return isProcessing != null && isProcessing.booleanValue();
  }

  private static boolean isSelfReference(final ELExpression expression, final String myName) {
    if (expression == null) return false;

    if (expression.getText().equals(myName)) return true;

    PsiElement firstChild = expression.getFirstChild();
    ELVariable var = null;
    if (firstChild instanceof ELVariable) var = (ELVariable)firstChild;
    if (firstChild instanceof ELSelectExpression) var = ((ELSelectExpression)firstChild).getField();


    return var != null && var.getText().equals(myName);
  }
}
