/*
 * @author max
 */
package org.jetbrains.plugins.groovy.lang;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeCopyHandler;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyReferenceAdjuster;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;

import java.util.Map;

/**
 * @author peter
 */
public class GroovyChangeUtilSupport implements TreeCopyHandler {

  public TreeElement decodeInformation(TreeElement element, final Map<Object, Object> decodingState) {
    if (element instanceof CompositeElement) {
      if (element.getElementType() == GroovyElementTypes.REFERENCE_ELEMENT ||
          element.getElementType() == GroovyElementTypes.REFERENCE_EXPRESSION) {
        GrReferenceElement ref = (GrReferenceElement)SourceTreeToPsiMap.treeElementToPsi(element);
        final PsiMember refMember = element.getCopyableUserData(REFERENCED_MEMBER_KEY);
        if (refMember != null) {
          element.putCopyableUserData(REFERENCED_MEMBER_KEY, null);
          PsiElement refElement1 = ref.resolve();
          if (!refMember.getManager().areElementsEquivalent(refMember, refElement1)) {
            try {
              if (!(refMember instanceof PsiClass) || ref.getQualifier() == null) {
                // can restore only if short (otherwise qualifier should be already restored)
                ref = (GrReferenceElement)ref.bindToElement(refMember);
              }
            }
            catch (IncorrectOperationException ignored) {
            }
            return (TreeElement)SourceTreeToPsiMap.psiElementToTree(ref);
          } else {
            // shorten references to the same package and to inner classes that can be accessed by short name
            GroovyReferenceAdjuster.INSTANCE.process(element, false, false);
          }
        }
        return element;
      }
    }
    return null;
  }

  public void encodeInformation(final TreeElement element, final ASTNode original, final Map<Object, Object> encodingState) {
    if (original instanceof CompositeElement) {
      if (original.getElementType() == GroovyElementTypes.REFERENCE_ELEMENT || original.getElementType() == GroovyElementTypes.REFERENCE_EXPRESSION) {
        final GroovyResolveResult result = ((GrReferenceElement)original.getPsi()).advancedResolve();
        if (result != null) {
          final PsiElement target = result.getElement();

          if (target instanceof PsiClass ||
            (target instanceof PsiMethod || target instanceof PsiField) &&
                   ((PsiMember) target).hasModifierProperty(PsiModifier.STATIC) &&
                    result.getCurrentFileResolveContext() instanceof GrImportStatement) {
            element.putCopyableUserData(REFERENCED_MEMBER_KEY, (PsiMember) target);
          }
        }
      }
    }
  }

  private static final Key<PsiMember> REFERENCED_MEMBER_KEY = Key.create("REFERENCED_MEMBER_KEY");
}