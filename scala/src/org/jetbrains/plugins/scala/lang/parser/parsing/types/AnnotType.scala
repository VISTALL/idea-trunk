package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package types

import com.intellij.lang.PsiBuilder
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.parser.parsing.expressions._

/** 
* @author Alexander Podkhalyuzin
* Date: 06.02.2008
*/

/*
 * AnnotType ::= {Annotation} SimpleType
 */

object AnnotType {
  def parse(builder: PsiBuilder): Boolean = {
    val annotMarker = builder.mark
    var isAnnotation = false
    //parse Simple type
    if (SimpleType parse builder){
      val annotationsMarker = builder.mark
      while (Annotation.parse(builder)) {isAnnotation = true}

      if (isAnnotation) annotationsMarker.done(ScalaElementTypes.ANNOTATIONS) else annotationsMarker.drop
      if (isAnnotation) annotMarker.done(ScalaElementTypes.ANNOT_TYPE) else annotMarker.drop
      true
    }
    else {
      annotMarker.rollbackTo
      false
    }
  }
}