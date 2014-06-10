package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package params

import com.intellij.lang.PsiBuilder
import lexer.ScalaTokenTypes

/**
* @author Alexander Podkhalyuzin
* Date: 06.03.2008
*/

/*
 * TypeParamClause ::= '[' VariantTypeParam {',' VariantTypeParam} ']'
 */

object TypeParamClause {
  def parse(builder: PsiBuilder): Boolean = {
    val typeMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.tLSQBRACKET => {
        builder.advanceLexer //Ate [
      }
      case _ => {
        typeMarker.drop
        return false
      }
    }
    if (!TypeParam.parse(builder, true)) {
      builder error ScalaBundle.message("wrong.parameter")
    }
    while (builder.getTokenType == ScalaTokenTypes.tCOMMA) {
      builder.advanceLexer //Ate
      if (!TypeParam.parse(builder, true)) {
        builder error ScalaBundle.message("wrong.parameter")
      }
    }
    builder.getTokenType match {
      case ScalaTokenTypes.tRSQBRACKET => {
        builder.advanceLexer //Ate ]
      }
      case _ => {
        builder error ScalaBundle.message("rsqbracket.expected")
      }
    }
    typeMarker.done(ScalaElementTypes.TYPE_PARAM_CLAUSE)
    return true
  }
}