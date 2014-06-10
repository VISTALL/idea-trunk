package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package xml.pattern

import com.intellij.lang.PsiBuilder
import org.jetbrains.plugins.scala.lang.parser.parsing.xml._
import com.intellij.psi.xml.XmlTokenType

/**
* @author Alexander Podkhalyuzin
* Date: 18.04.2008
*/

/*
 *  ContentP ::= [CharData] {ContentP1 [CharData]}
 *
 *  ContentP1 ::= XmlPattern
 *              | CDSect
 *              | Comment
 *              | PI
 *              | Reference
 *              | ScalaPatterns
 */

object ContentP {
  def parse(builder: PsiBuilder): Boolean = {
    val contentMarker = builder.mark()
    builder.getTokenType match {
      case XmlTokenType.XML_DATA_CHARACTERS => {
        builder.advanceLexer()
      }
      case _ =>
    }
    def subparse() {
      var isReturn = false
      if (!CDSect.parse(builder) &&
        !Comment.parse(builder) &&
        !PI.parse(builder) &&
        !Reference.parse(builder) &&
        !ScalaPatterns.parse(builder) &&
        !XmlPattern.parse(builder)) isReturn = true
      builder.getTokenType match {
        case XmlTokenType.XML_DATA_CHARACTERS => {
          builder.advanceLexer()
        }
        case _ => {
          if (isReturn) return
        }
      }
      subparse()
    }
    subparse()
    contentMarker.drop
    return true
  }
}