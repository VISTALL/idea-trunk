package org.jetbrains.plugins.scala.lang.psi.fake

import com.intellij.psi.impl.light.LightElement
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScValue
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScBindingPattern
import com.intellij.openapi.util.Key
import java.util.List
import com.intellij.psi._
import java.lang.String
import javadoc.PsiDocComment
import search.GlobalSearchScope
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTypeDefinition
import com.intellij.openapi.project.Project
import com.intellij.util.IncorrectOperationException
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTyped
import com.intellij.lang.Language
import util.{MethodSignatureBase, PsiTreeUtil, MethodSignatureBackedByPsiMethod, MethodSignature}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.synthetic.JavaIdentifier

/**
 * User: Alexander Podkhalyuzin
 * Date: 07.09.2009
 */

class FakePsiMethod(
        val navElement: PsiElement,
        name: String,
        params: Array[ScType],
        retType: ScType,
        hasModifier: String => Boolean
        ) extends {
    val project: Project = navElement.getProject
    val scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
    val manager = navElement.getManager
    val language = navElement.getLanguage
  } with LightElement(manager, language) with PsiMethod{
  def this(value: ScTyped, hasModifier: String => Boolean) = {
    this(value, value.getName, Array.empty, value.calcType, hasModifier)
  }
  override def toString: String = name + "()"

  def getContainingClass: PsiClass = PsiTreeUtil.getParentOfType(navElement, classOf[ScTypeDefinition])

  def getReturnTypeNoResolve: PsiType = ScType.toPsi(retType, project, scope)

  override def getTextOffset: Int = navElement.getTextOffset

  override def getNavigationElement: PsiElement = navElement

  override def getOriginalElement: PsiElement = navElement

  def getDocComment: PsiDocComment = null

  def isDeprecated: Boolean = false

  def hasModifierProperty(name: String): Boolean = {
    hasModifier(name)
  }

  def getTypeParameterList: PsiTypeParameterList = null

  def getTypeParameters: Array[PsiTypeParameter] = PsiTypeParameter.EMPTY_ARRAY

  def hasTypeParameters: Boolean = false

  def accept(visitor: PsiElementVisitor): Unit = {}

  def copy: PsiElement = new FakePsiMethod(navElement, name, params, retType, hasModifier)

  def getText: String = throw new IncorrectOperationException

  def getParameterList: PsiParameterList = new FakePsiParameterList(manager, language, params)

  def findDeepestSuperMethod: PsiMethod = null

  def getSignature(substitutor: PsiSubstitutor): MethodSignature = {
    new MethodSignatureBase(PsiSubstitutor.EMPTY, getParameterList.getParameters.map(_.getType), PsiTypeParameter.EMPTY_ARRAY) {
      def isRaw: Boolean = false

      def getName: String = name
    }
  }

  def findSuperMethodSignaturesIncludingStatic(checkAccess: Boolean): List[MethodSignatureBackedByPsiMethod] = null

  def findSuperMethods(checkAccess: Boolean): Array[PsiMethod] = PsiMethod.EMPTY_ARRAY

  def setName(name: String): PsiElement = throw new IncorrectOperationException

  def getModifierList: PsiModifierList = null

  def findSuperMethods(parentClass: PsiClass): Array[PsiMethod] = PsiMethod.EMPTY_ARRAY

  def getBody: PsiCodeBlock = null

  def findSuperMethods: Array[PsiMethod] = PsiMethod.EMPTY_ARRAY

  def getReturnType: PsiType = ScType.toPsi(retType, project, scope)

  def findDeepestSuperMethods: Array[PsiMethod] = PsiMethod.EMPTY_ARRAY

  def isConstructor: Boolean = false

  def getThrowsList: PsiReferenceList = new FakePsiReferenceList(manager, language, PsiReferenceList.Role.THROWS_LIST)

  def isVarArgs: Boolean = false

  def getReturnTypeElement: PsiTypeElement = null

  def getHierarchicalMethodSignature: HierarchicalMethodSignature = null

  def getName: String = name

  def getMethodReceiver: PsiMethodReceiver = null

  def getNameIdentifier: PsiIdentifier = null
}

class FakePsiTypeElement(manager: PsiManager, language: Language, tp: ScType)
        extends LightElement(manager, language) with PsiTypeElement {
  def getTypeNoResolve(context: PsiElement): PsiType = PsiType.VOID //ScType.toPsi(tp, manager.getProject, GlobalSearchScope.allScope(manager.getProject))

  def getOwner(annotation: PsiAnnotation): PsiAnnotationOwner = null

  def getInnermostComponentReferenceElement: PsiJavaCodeReferenceElement = null

  def getType: PsiType = ScType.toPsi(tp, manager.getProject, GlobalSearchScope.allScope(manager.getProject))

  def addAnnotation(qualifiedName: String): PsiAnnotation = null

  def findAnnotation(qualifiedName: String): PsiAnnotation = null

  def getApplicableAnnotations: Array[PsiAnnotation] = PsiAnnotation.EMPTY_ARRAY

  def getAnnotations: Array[PsiAnnotation] = PsiAnnotation.EMPTY_ARRAY

  def getText: String = tp.toString

  override def toString: String = tp.toString

  def accept(visitor: PsiElementVisitor): Unit = {}

  def copy: PsiElement = new FakePsiTypeElement(manager, language, tp)
}

class FakePsiParameter(manager: PsiManager, language: Language, paramType: ScType)
        extends LightElement(manager, language) with PsiParameter {
  def getDeclarationScope: PsiElement = null

  def getTypeNoResolve: PsiType = PsiType.VOID

  def setName(name: String): PsiElement = this //do nothing

  def getNameIdentifier: PsiIdentifier = null

  def computeConstantValue: AnyRef = null

  def normalizeDeclaration: Unit = {}

  def hasInitializer: Boolean = false

  def getInitializer: PsiExpression = null

  def getType: PsiType = ScType.toPsi(paramType, manager.getProject, GlobalSearchScope.allScope(manager.getProject))

  def isVarArgs: Boolean = false

  def getAnnotations: Array[PsiAnnotation] = PsiAnnotation.EMPTY_ARRAY

  def getName: String = "param"

  def copy: PsiElement = new FakePsiParameter(manager, language, paramType)

  def accept(visitor: PsiElementVisitor): Unit = {}

  def getText: String = "param: " + getTypeElement.getText

  override def toString: String = getText

  def getModifierList: PsiModifierList = null

  def hasModifierProperty(name: String): Boolean = false

  def getTypeElement: PsiTypeElement = new FakePsiTypeElement(manager, language, paramType)
}

class FakePsiParameterList(manager: PsiManager, language: Language, params: Array[ScType])
        extends LightElement(manager, language) with PsiParameterList {
  def getParameters: Array[PsiParameter] = params.map(new FakePsiParameter(manager, language, _))

  def getParametersCount: Int = params.length

  def accept(visitor: PsiElementVisitor): Unit = {}

  def getParameterIndex(parameter: PsiParameter): Int = getParameters.findIndexOf(_ == parameter)

  def getText: String = getParameters.map(_.getText).mkString("(", ", ", ")")

  override def toString: String = "FakePsiParameterList"

  def copy: PsiElement = new FakePsiParameterList(manager, language, params)
}

class FakePsiReferenceList(manager: PsiManager, language: Language, role: PsiReferenceList.Role) extends LightElement(manager, language) with PsiReferenceList {
  def getText: String = ""

  def getRole: PsiReferenceList.Role = role

  def getReferenceElements: Array[PsiJavaCodeReferenceElement] = PsiJavaCodeReferenceElement.EMPTY_ARRAY

  def getReferencedTypes: Array[PsiClassType] = PsiClassType.EMPTY_ARRAY

  override def toString: String = ""

  def accept(visitor: PsiElementVisitor): Unit = {}

  def copy: PsiElement = new FakePsiReferenceList(manager, language, role)
}