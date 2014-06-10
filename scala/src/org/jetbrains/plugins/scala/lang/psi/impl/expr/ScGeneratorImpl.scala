package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElementImpl
import com.intellij.psi.tree.TokenSet
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType;
import com.intellij.psi._
import org.jetbrains.annotations._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScPattern
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression


/** 
* @author Alexander Podkhalyuzin
* Date: 07.03.2008
*/

class ScGeneratorImpl(node: ASTNode) extends ScalaPsiElementImpl (node) with ScGenerator{

  override def toString: String = "Generator"

  def pattern = findChildByClass(classOf[ScPattern])

  def guard = findChildByClass(classOf[ScGuard])

  def rvalue = findChildByClass(classOf[ScExpression])
  
}