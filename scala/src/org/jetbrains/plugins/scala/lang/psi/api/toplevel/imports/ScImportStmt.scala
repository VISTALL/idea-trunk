package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package imports

import com.intellij.psi.PsiElement
import packaging.ScPackaging
import expr.ScBlockStatement
import typedef.ScTypeDefinition
import lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import usages.{ImportSelectorUsed, ImportExprUsed, ImportUsed}
/**
* @author Alexander Podkhalyuzin
* Date: 20.02.2008
*/

trait ScImportStmt extends ScBlockStatement {
  def importExprs: Array[ScImportExpr]
}