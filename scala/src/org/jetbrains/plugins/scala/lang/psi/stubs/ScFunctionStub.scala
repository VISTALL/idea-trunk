package org.jetbrains.plugins.scala
package lang
package psi
package stubs

import api.base.types.ScTypeElement
import api.expr.ScExpression
import api.statements.ScFunction
import com.intellij.psi.impl.cache.TypeInfo
import com.intellij.psi.PsiType
import com.intellij.psi.stubs.NamedStub

/**
 * User: Alexander Podkhalyuzin
 * Date: 14.10.2008
 */

trait ScFunctionStub extends NamedStub[ScFunction] {
  def isDeclaration: Boolean

  def getAnnotations : Array[String]

  def getReturnTypeText: String

  def getReturnTypeElement: Option[ScTypeElement]

  def getBodyExpression: Option[ScExpression]

  def getBodyText: String

  def hasAssign: Boolean
}