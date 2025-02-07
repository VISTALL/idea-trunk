package org.jetbrains.plugins.scala
package lang
package psi
package impl
package base

import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import com.intellij.lang.ASTNode
import psi.stubs.ScPrimaryConstructorStub
import java.lang.String
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod
import com.intellij.psi.util.MethodSignature
import com.intellij.psi._
import javadoc.PsiDocComment
import org.jetbrains.plugins.scala.lang.psi.api.base._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef._
import org.jetbrains.plugins.scala.lang.psi.api.statements.params._
import api.expr.ScAnnotations
import toplevel.synthetic.JavaIdentifier
import java.util.{ArrayList, List}

/**
* @author Alexander Podkhalyuzin
* Date: 07.03.2008
*/

class ScPrimaryConstructorImpl extends ScalaStubBasedElementImpl[ScPrimaryConstructor] with ScPrimaryConstructor {
  def this(node: ASTNode) = {this(); setNode(node)}
  def this(stub: ScPrimaryConstructorStub) = {this(); setStub(stub); setNode(null)}

  override def hasAnnotation: Boolean = {
    return !(getNode.getFirstChildNode.getFirstChildNode == null)
  }

  //todo rewrite me!
  override def hasModifier: Boolean = false

  def getClassNameText: String = {
    return getNode.getTreeParent.getPsi.asInstanceOf[ScTypeDefinition].getName
  }

  override def toString: String = "PrimaryConstructor"


  def parameterList: ScParameters = {
    getStubOrPsiChild(ScalaElementTypes.PARAM_CLAUSES)
  }

  override def getName: String = "this"

  def getNameIdentifier: PsiIdentifier = null

  def getReturnTypeElement = null

  def getHierarchicalMethodSignature = null

  def findSuperMethods(parentClass: PsiClass) = PsiMethod.EMPTY_ARRAY

  def findSuperMethods(checkAccess: Boolean) = PsiMethod.EMPTY_ARRAY

  def findSuperMethods = PsiMethod.EMPTY_ARRAY

  def findDeepestSuperMethod = null

  def findDeepestSuperMethods = PsiMethod.EMPTY_ARRAY

  def getMethodReceiver: PsiMethodReceiver = null

  def getReturnTypeNoResolve: PsiType = PsiType.VOID

  def findSuperMethodSignaturesIncludingStatic(checkAccess: Boolean) =
    new ArrayList[MethodSignatureBackedByPsiMethod]()

  def getSignature(substitutor: PsiSubstitutor) = MethodSignatureBackedByPsiMethod.create(this, substitutor)

  def getTypeParameters = PsiTypeParameter.EMPTY_ARRAY

  //todo implement me!
  def isVarArgs = false

  def isConstructor = true

  def getBody = null

  def getThrowsList = findChildByClass(classOf[ScAnnotations])

  def getTypeParameterList = null

  def hasTypeParameters = false

  def getParameterList: ScParameters = parameterList

  def setName(name: String): PsiElement = this

  def getReturnType: PsiType = PsiType.VOID

  def getDocComment: PsiDocComment = null

  def isDeprecated: Boolean = false
}