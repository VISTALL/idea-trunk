package org.jetbrains.plugins.scala
package lang
package psi
package api
package base
package patterns

import _root_.org.jetbrains.plugins.scala.lang.psi.types.{ScTupleType, Nothing}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement

/** 
* @author Alexander Podkhalyuzin
* Date: 28.02.2008
*/

trait ScTuplePattern extends ScPattern {
  def patternList = findChild(classOf[ScPatterns])

  override def calcType = patternList match {
    case Some(l) => new ScTupleType(collection.immutable.Seq(l.patterns.map({_.calcType}).toSeq : _*))
    case None => Nothing
  }
}