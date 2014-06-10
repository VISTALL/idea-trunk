package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package expressions

import com.intellij.lang.PsiBuilder
import lexer.ScalaTokenTypes
/**
* @author Alexander Podkhalyuzin
* Date: 06.03.2008
*/

/*
 * Bindings ::= '(' Binding {',' Binding } ')'
 */

object Bindings {
  def parse(builder: PsiBuilder): Boolean = {
    val bindingsMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.tLPARENTHESIS => {
        builder.advanceLexer //Ate (
      }
      case _ => {
        bindingsMarker.drop
        return false
      }
    }
    Binding parse builder
    while (builder.getTokenType == ScalaTokenTypes.tCOMMA) {
      builder.advanceLexer //Ate ,
      if (!Binding.parse(builder)) {
        builder error ErrMsg("wrong.binding")
      }
    }
    builder.getTokenType match {
      case ScalaTokenTypes.tRPARENTHESIS => {
        builder.advanceLexer //Ate )
      }
      case _ => {
        bindingsMarker.rollbackTo
        return false
      }
    }
    val pm = bindingsMarker.precede
    bindingsMarker.done(ScalaElementTypes.PARAM_CLAUSE)
    pm.done(ScalaElementTypes.PARAM_CLAUSES)
    return true
  }
}