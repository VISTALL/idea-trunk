package org.jetbrains.plugins.scala
package lang
package psi

import api.base.ScStableCodeReferenceElement
import api.statements.ScDeclaredElementsHolder
import api.toplevel.ScNamedElement
import api.toplevel.typedef.ScObject
import com.intellij.psi._
import psi.impl.ScalaFileImpl
import scope._
import com.intellij.psi.stubs.StubElement
import com.intellij.openapi.progress.ProgressManager

trait ScDeclarationSequenceHolder extends ScalaPsiElement {
  override def processDeclarations(processor: PsiScopeProcessor,
      state : ResolveState,
      lastParent: PsiElement,
      place: PsiElement): Boolean = {
    import org.jetbrains.plugins.scala.lang.resolve._

    if (lastParent != null) {
      var run = lastParent
      while (run != null) {
        ProgressManager.getInstance.checkCanceled
        place match {
          case id: ScStableCodeReferenceElement => run match {
            case po: ScObject if po.isPackageObject && id.qualName == po.getQualifiedName => // do nothing
            case _ => if (!processElement(run, processor, state)) return false
          }
          case _ => if (!processElement(run, processor, state)) return false
        }
        run = ScalaPsiUtil.getPrevStubOrPsiElement(run)
      }

      //forward references are allowed (e.g. 2 local methods see each other), with highlighting errors in case of var/vals
      run = ScalaPsiUtil.getNextStubOrPsiElement(lastParent)
      while (run != null) {
        ProgressManager.getInstance.checkCanceled
        if (!processElement(run, processor, state)) return false
        run = ScalaPsiUtil.getNextStubOrPsiElement(run)
      }
    }
    true
  }

  private def processElement(e : PsiElement, processor: PsiScopeProcessor, state : ResolveState) : Boolean = e match {
    case named: ScNamedElement => processor.execute(named, state)
    case holder: ScDeclaredElementsHolder => {
      for (declared <- holder.declaredElements) {
        ProgressManager.getInstance.checkCanceled
        if (!processor.execute(declared, state)) return false
      }
      true
    }
    case _ => true
  }
}