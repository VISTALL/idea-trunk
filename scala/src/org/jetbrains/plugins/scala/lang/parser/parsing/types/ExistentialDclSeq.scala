package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package types

import com.intellij.lang.PsiBuilder, org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.lang.parser.parsing.statements.Dcl

/** 
* @author Alexander Podkhalyuzin
* Date: 28.02.2008
*/

/*
 * ExistentialDclSeq ::= ExistentialDcl {semi ExistentialDcl}
 *
 * ExistentialDcl ::= 'type' TypeDcl
 *                  | 'val' ValDcl
 */

object ExistentialDclSeq {
  def parse(builder: PsiBuilder) {
    builder.getTokenType match {
      case ScalaTokenTypes.kTYPE | ScalaTokenTypes.kVAL => {
        Dcl parse (builder,false)
      }
      case _ => {
        builder error ScalaBundle.message("wrong.existential.declaration")
        return
      }
    }
    while (builder.getTokenType == ScalaTokenTypes.tSEMICOLON
          || builder.getTokenType == ScalaTokenTypes.tLINE_TERMINATOR) {
      builder.advanceLexer //Ate semi
      builder.getTokenType match {
        case ScalaTokenTypes.kTYPE | ScalaTokenTypes.kVAL => {
          Dcl parse (builder,false)
        }
        case _ => {
          builder error ScalaBundle.message("wrong.existential.declaration")
        }
      }
    }
  }
}