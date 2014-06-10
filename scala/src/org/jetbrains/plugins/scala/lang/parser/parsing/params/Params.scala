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
 * Params ::= Param {',' Param}
 */

object Params {
  def parse(builder: PsiBuilder): Boolean = {
    if (!Param.parse(builder)) {
      return false
    }
    while (builder.getTokenType == ScalaTokenTypes.tCOMMA) {
      builder.advanceLexer //Ate ,
      if (!Param.parse(builder)) {
        builder error ScalaBundle.message("wrong.parameter")
      }
    }
    return true
  }
}