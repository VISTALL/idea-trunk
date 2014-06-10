package org.jetbrains.plugins.scala
package lang
package psi
package api
package base

import _root_.org.jetbrains.plugins.scala.lang.resolve._
import _root_.scala.collection.Set
import codeInspection.{ScalaElementVisitor}

import impl.ScalaPsiElementFactory
import impl.toplevel.synthetic.SyntheticClasses
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import com.intellij.psi._
import com.intellij.psi.PsiPolyVariantReference
import org.jetbrains.plugins.scala.lang.psi.types._
import com.intellij.openapi.util.TextRange
import toplevel.typedef.ScTypeDefinition
import statements.{ScFunction, ScTypeAlias}

/**
 * @author Alexander Podkhalyuzin
 * Date: 22.02.2008
 */

trait ScReferenceElement extends ScalaPsiElement with PsiPolyVariantReference {
  def bind(): Option[ScalaResolveResult] = {
    var res: Option[ScalaResolveResult] = None
    val results = multiResolve(false)
    res = results.length match {
      case 1 => Some(results(0).asInstanceOf[ScalaResolveResult])
      case _ => None
    }
    res
  }

  def resolve(): PsiElement = bind match {
    case None => null
    case Some(result) if result.isCyclicReference => null
    case Some(res) => res.element
  }

  override def getReference = this

  def nameId: PsiElement

  def refName: String = nameId.getText.replace("`", "")

  def getElement = this

  def getRangeInElement: TextRange =
    new TextRange(nameId.getTextRange.getStartOffset - getTextRange.getStartOffset, getTextLength)

  def getCanonicalText: String = null

  def isSoft(): Boolean = false

  def handleElementRename(newElementName: String): PsiElement = {
    val isQuoted = refName.startsWith("`")
    val id = nameId.getNode
    val parent = id.getTreeParent
    parent.replaceChild(id,
      ScalaPsiElementFactory.createIdentifier(if (isQuoted) "`" + newElementName + "`" else newElementName, getManager))
    return this
  }

  def isReferenceTo(element: PsiElement): Boolean = {
    val res = resolve
    if (res == null) return false
    if (res == element) return true
    element match {
      case td: ScTypeDefinition if td.getName == refName => {
        res match {
          case method: ScFunction if method.getName == "apply" || method.getName == "unapply" ||
            method.getName == "unapplySeq" => {
            val clazz = method.getContainingClass
            if (clazz == td) return true
            if (td.isInheritor(clazz, true)) return true
          }
          case _ =>
        }
      }
      case _ =>
    }
    return false
  }

  def qualifier: Option[ScalaPsiElement]

  //provides the set of possible namespace alternatives based on syntactic position 
  def getKinds(incomplete: Boolean): Set[ResolveTargets.Value]

  def getSameNameVariants: Array[ResolveResult]

  override def accept(visitor: PsiElementVisitor) {
    visitor match {
      case sev: ScalaElementVisitor => sev.visitReference(this)
      case _ => visitor.visitElement(this)
    }
  }
}