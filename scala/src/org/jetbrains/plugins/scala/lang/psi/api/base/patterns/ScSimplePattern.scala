package org.jetbrains.plugins.scala
package lang
package psi
package api
package base
package patterns

import _root_.org.jetbrains.plugins.scala.lang.psi.types.ScType
import statements.params.ScParameter
import resolve.ScalaResolveResult
import psi.ScalaPsiElement

/** 
* @author Alexander Podkhalyuzin
* Patterns, introduced by case classes or extractors
*/
trait ScConstructorPattern extends ScPattern {
  def args: ScPatternArgumentList
  def ref = findChildByClassScala(classOf[ScStableCodeReferenceElement])
  def bindParamTypes() : Option[Seq[ScType]]
}