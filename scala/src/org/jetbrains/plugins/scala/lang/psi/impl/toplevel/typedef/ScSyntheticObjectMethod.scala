package org.jetbrains.plugins.scala
package lang
package psi
package impl
package toplevel
package typedef

import api.expr.ScNewTemplateDefinition
import api.toplevel.{ScModifierListOwner, ScTyped}
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.impl.light.LightElement
import com.intellij.util.IncorrectOperationException
import java.lang.String
import javax.swing.Icon
import com.intellij.openapi.vcs.FileStatus
import java.util.List
import com.intellij.psi._
import com.intellij.psi.scope.processor.MethodResolverProcessor
import com.intellij.psi.scope.PsiScopeProcessor
import psi.stubs.elements.wrappers.DummyASTNode
import psi.stubs.ScTypeDefinitionStub
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.lang.ASTNode

import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.lexer._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElementImpl
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes

import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef._
import org.jetbrains.plugins.scala.lang.psi.api.base.ScModifierList
import synthetic.{PsiMethodFake, SyntheticNamedElement}
import types.{ScFunctionType, ScType}
import util.{PsiTreeUtil, MethodSignatureBackedByPsiMethod}
/**
 * @author ilyas
 */

class ScSyntheticObjectMethod(manager: PsiManager, field: PsiNamedElement, member: ScModifierListOwner) extends SyntheticNamedElement(manager, field.getName)
        with PsiMethod with PsiMethodFake {
  def getReturnTypeNoResolve: PsiType = PsiType.VOID

  def getMethodReceiver: PsiMethodReceiver = null

  override def toString = "Synthetic object method [" + field.getName + "]"

  override def setName(name: String): PsiElement = field.setName(name)

  override def getName = field.getName

  override def getParameterList: PsiParameterList = new LightElement(manager, ScalaFileType.SCALA_LANGUAGE) with PsiParameterList {
    def getParametersCount: Int = 0
    def getParameterIndex(parameter: PsiParameter): Int = 0
    def getParameters: Array[PsiParameter] = PsiParameter.EMPTY_ARRAY
    def copy = throw new IncorrectOperationException("nonphysical element")
    def accept(v: PsiElementVisitor) = throw new IncorrectOperationException("should not call")
    override def getContainingFile = field.getContainingFile
    def getText = ""
    override def toString = "SyntheticParameterList"
  }

  def getContainingClass = member match {
    case mem : ScMember => mem.getContainingClass
    case _ => PsiTreeUtil.getParentOfType(member, classOf[PsiClass])
  }

  def getThrowsList: PsiReferenceList = new LightElement(manager, ScalaFileType.SCALA_LANGUAGE) with PsiReferenceList {
    def copy = throw new IncorrectOperationException("nonphysical element")
    def accept(v: PsiElementVisitor) = throw new IncorrectOperationException("should not call")
    override def getContainingFile = field.getContainingFile
    def getText = ""
    override def toString = "SyntheticParameterList"
    def getReferencedTypes: Array[PsiClassType] = PsiClassType.EMPTY_ARRAY
    def getRole = PsiReferenceList.Role.THROWS_LIST
    def getReferenceElements: Array[PsiJavaCodeReferenceElement] = PsiJavaCodeReferenceElement.EMPTY_ARRAY
  }

  def getModifierList: PsiModifierList = member.getModifierList

  override def getContainingFile = field.getContainingFile

  override def hasModifierProperty(name: String): Boolean = member.hasModifierProperty(name)

  def getReturnType: PsiType = field match {
    case t: ScTyped => t.calcType match {
      case ScFunctionType(rt, _) => ScType.toPsi(rt, getProject, getResolveScope)
      case x => ScType.toPsi(x, getProject, getResolveScope)
    }
    case _ => null
  }

  def getSignature(substitutor: PsiSubstitutor) = MethodSignatureBackedByPsiMethod.create(this, substitutor)

  override def getNavigationElement: PsiElement = field

  override def isConstructor: Boolean = false

  override def getReturnTypeElement: PsiTypeElement = null

  override def copy = throw new IncorrectOperationException("nonphysical element")

  override def getPresentation: ItemPresentation = new ItemPresentation {
    def getPresentableText(): String = getName

    def getTextAttributesKey(): TextAttributesKey = null

    def getLocationString(): String = {
      val clazz = getContainingClass
      clazz match {
        case _: ScTypeDefinition => "(" + clazz.getQualifiedName + ")"
        case x: ScNewTemplateDefinition => "(<anonymous>)"
        case _ => ""
      }
    }

    override def getIcon(open: Boolean) = Icons.FUNCTION
  }

}
