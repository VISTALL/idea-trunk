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

package org.jetbrains.plugins.grails.references.gsp;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.completion.GspCompletionUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspResolveUtil;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import org.jetbrains.plugins.grails.references.manager.GrailsImplicitVariableManager;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrImplicitVariable;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.processors.PropertyResolverProcessor;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ResolverProcessor;

/**
 * @author ilyas
 */
public class GspGroovyRefExprReference implements PsiPolyVariantReference {

  @NotNull
  private final GrReferenceExpression myRefExpr;

  public GspGroovyRefExprReference(@NotNull GrReferenceExpression refExpr) {
    myRefExpr = refExpr;
  }

  @NotNull
  public GrReferenceExpression getElement() {
    return myRefExpr;
  }

  public TextRange getRangeInElement() {
    return getElement().getRangeInElement();
  }

  @Nullable
  public PsiElement resolve() {
    ResolveResult[] results = ((PsiManagerEx)getElement().getManager()).getResolveCache().resolveWithCaching(this, RESOLVER, false, false);
    GrailsImplicitVariableManager manager = GrailsImplicitVariableManager.getInstance(getElement().getProject());
    PsiElement implicitVariable = null;
    if (manager != null) {
      implicitVariable = manager.getImplicitVariable(getElement().getReferenceName(), getElement().getContainingFile());
    }
    PsiElement result = results.length == 1 ? results[0].getElement() : null;

    // get exclusive resolve result
    if (result != null && implicitVariable != null) return null;
    if (implicitVariable != null) return implicitVariable;
    if (result != null) return result;
    return null;
  }

  private static final MyResolver RESOLVER = new MyResolver();

  public String getCanonicalText() {
    return myRefExpr.getCanonicalText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final PsiElement resolved = resolve();
    GrReferenceExpression element = getElement();
    if (resolved instanceof GrField && ((GrField)resolved).isProperty()) {
      final GrField field = (GrField)resolved;
      final String oldName = element.getReferenceName();
      if (!oldName.equals(field.getName())) { //was accessor reference to property
        if (oldName.startsWith("get")) {
          return doHandleElementRename("get" + StringUtil.capitalize(newElementName));
        } else if (oldName.startsWith("set")) {
          return doHandleElementRename("set" + StringUtil.capitalize(newElementName));
        }
      }
    }

    return doHandleElementRename(newElementName);
  }

  private PsiElement doHandleElementRename(String newElementName) throws IncorrectOperationException {
    if (!PsiUtil.isValidReferenceName(newElementName)) {
      GrReferenceExpression myRefExpr = getElement();
      PsiElement element = GroovyPsiElementFactory.getInstance(myRefExpr.getProject()).createStringLiteral(newElementName);
      myRefExpr.getReferenceNameElement().replace(element);
      return myRefExpr;
    }

    String name = newElementName;
    if (newElementName.length() > 3 && newElementName.substring(0, 3).equals("get")) {
      name = StringUtil.decapitalize(newElementName.substring(3));
    }

    PsiElement nameElement = getElement().getReferenceNameElement();
    if (nameElement != null) {
      ASTNode node = nameElement.getNode();
      ASTNode newNameNode = GroovyPsiElementFactory.getInstance(getElement().getProject()).createReferenceNameFromText(name).getNode();
      assert newNameNode != null && node != null;
      node.getTreeParent().replaceChild(node, newNameNode);
    }

    return getElement();
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }


  public boolean isReferenceTo(PsiElement element) {
    if (element instanceof PsiMethod && PropertyUtil.isSimplePropertyAccessor((PsiMethod)element)) {
      return getElement().getManager().areElementsEquivalent(element, resolve());
    }
    if (element instanceof GrImplicitVariable) {
      return element == resolve();
    }
    if (element instanceof GrField && ((GrField)element).isProperty()) {
      return getElement().getManager().areElementsEquivalent(element, resolve());
    }
    return false;
  }

  public Object[] getVariants() {
    return GspCompletionUtil.getGspContextSpecificVariants(getElement());
  }

  public boolean isSoft() {
    return false;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return ((PsiManagerEx)getElement().getManager()).getResolveCache()
      .resolveWithCaching(this, RESOLVER, false, incompleteCode);
  }

  private static class MyResolver implements ResolveCache.PolyVariantResolver<GspGroovyRefExprReference> {
    public ResolveResult[] resolve(final GspGroovyRefExprReference reference, final boolean incompleteCode) {
      GrReferenceExpression refExpr = reference.getElement();
      String name = refExpr.getReferenceName();
      if (name == null) return GroovyResolveResult.EMPTY_ARRAY;
      ResolverProcessor processor = new PropertyResolverProcessor(name, refExpr);
      GrExpression qualifier = refExpr.getQualifierExpression();
      if (qualifier == null) {
        if (GspResolveUtil.isUnderExprInjection(refExpr)) {
          GspResolveUtil.collectGspUnqualifiedVariants(processor, refExpr);
          GroovyResolveResult[] tags = processor.getCandidates();
          if (refExpr.getParent() instanceof GrReferenceExpression) {
            processor = new PropertyResolverProcessor(GspTagLibUtil.NAMESPACE_FIELD, refExpr);
            GspResolveUtil.collectTagLibNamespaceFields(processor, refExpr);
            GroovyResolveResult[] namespaces = processor.getCandidates();
            return ArrayUtil.mergeArrays(namespaces, tags, GroovyResolveResult.class);
          }
          return tags;
        } else {
          return GroovyResolveResult.EMPTY_ARRAY;
        }
      }
      PsiType type = qualifier.getType();
      PsiReference reference1 = qualifier.getReference();
      if (type != null &&
          type.equalsToText("java.lang.String") &&
          reference1 != null &&
          reference1.resolve() instanceof GrField &&
          qualifier instanceof GrReferenceExpression &&
          !GspTagLibUtil.DEFAULT_TAGLIB_PREFIX.equals(((GrReferenceExpression)qualifier).getReferenceName())) {
        PsiElement resolved = ((GrReferenceExpression)qualifier).resolve();
        if (resolved == null) {
          GspResolveUtil.collectGspQualifiedVariants(processor, refExpr);
        }
      }
      return processor.getCandidates();
    }
  }
}
