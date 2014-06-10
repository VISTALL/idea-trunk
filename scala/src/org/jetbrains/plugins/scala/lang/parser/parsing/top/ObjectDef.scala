package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package top

import com.intellij.lang.PsiBuilder
import lexer.ScalaTokenTypes

/**
* @author Alexander Podkhalyuzin
* Date: 06.02.2008
*/

/*
 *  ObjectDef ::= id ClassTemplateOpt
 */

object ObjectDef {
  def parse(builder: PsiBuilder): Boolean = {
    builder.getTokenType match {
      case ScalaTokenTypes.tIDENTIFIER => builder.advanceLexer //Ate identifier
      case _ => {
        builder error ScalaBundle.message("identifier.expected")
        return false;
      }
    }
    //parse extends block
    ClassTemplateOpt parse builder
    return true
  }
}