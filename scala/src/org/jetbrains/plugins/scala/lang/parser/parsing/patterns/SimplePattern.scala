package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package patterns

import com.intellij.lang.PsiBuilder
import expressions.Literal
import lexer.ScalaTokenTypes
import types.StableId
import util.ParserUtils
import xml.pattern.XmlPattern

/**
* @author Alexander Podkhalyuzin
* Date: 29.02.2008
*/

/*
 * SimplePattern ::= '_'
 *                 | varid
 *                 | Literal
 *                 | StableId
 *                 | StableId '(' [Patterns [',']] ')'
 *                 | StableId '(' [Patterns ','] [varid '@'] '_' '*'')'
 *                 |'(' [Patterns [',']] ')'
 *                 | XmlPattern
 */

object SimplePattern extends ParserNode {
  def parse(builder: PsiBuilder): Boolean = {
    def isVarId = builder.getTokenText.substring(0, 1).toLowerCase ==
            builder.getTokenText.substring(0, 1) && !(
            builder.getTokenText.apply(0) == '`' && builder.getTokenText.apply(builder.getTokenText.length - 1) == '`'
            )
    val simplePatternMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.tUNDER => {
        builder.advanceLexer //Ate _
        builder getTokenText match {
          case "*" => {
            simplePatternMarker.rollbackTo
            return false
          }
          case _ => {}
        }
        simplePatternMarker.done(ScalaElementTypes.WILDCARD_PATTERN)
        return true
      }
      case ScalaTokenTypes.tLPARENTHESIS => {
        builder.advanceLexer //Ate (
        builder.getTokenType match {
          case ScalaTokenTypes.tRPARENTHESIS => {
            builder.advanceLexer //Ate )
            simplePatternMarker.done(ScalaElementTypes.TUPLE_PATTERN)
            return true
          }
          case _ => {}
        }
        if (Patterns parse builder) {
          builder.getTokenType match {
            case ScalaTokenTypes.tRPARENTHESIS => {
              builder.advanceLexer //Ate )
              simplePatternMarker.done(ScalaElementTypes.TUPLE_PATTERN)
              return true
            }
            case _ => {
              builder error ScalaBundle.message("rparenthesis.expected")
              simplePatternMarker.done(ScalaElementTypes.TUPLE_PATTERN)
              return true
            }
          }
        }
        if (Pattern parse builder) {
          builder.getTokenType match {
            case ScalaTokenTypes.tRPARENTHESIS => {
              builder.advanceLexer //Ate )
            }
            case _ => {
              builder error ScalaBundle.message("rparenthesis.expected")
            }
          }
          simplePatternMarker.done(ScalaElementTypes.PATTERN_IN_PARENTHESIS)
          return true
        }
      }
      case _ => {}
    }
    if (Literal parse builder) {
      simplePatternMarker.done(ScalaElementTypes.LITERAL_PATTERN)
      return true
    }
    if (XmlPattern.parse(builder)) {
      simplePatternMarker.drop
      return true
    }
    if (lookAhead(builder, ScalaTokenTypes.tIDENTIFIER) &&
            !lookAhead(builder, ScalaTokenTypes.tIDENTIFIER, ScalaTokenTypes.tDOT) &&
            !lookAhead(builder, ScalaTokenTypes.tIDENTIFIER, ScalaTokenTypes.tLPARENTHESIS) &&
            isVarId) {
      val rpm = builder.mark
      builder.getTokenText
      builder.advanceLexer
      rpm.done(ScalaElementTypes.REFERENCE_PATTERN)
      simplePatternMarker.drop
      return true
    }

    val rb1 = builder.mark
    if (StableId parse (builder, ScalaElementTypes.REFERENCE_EXPRESSION)) {
      builder.getTokenType match {
        case ScalaTokenTypes.tLPARENTHESIS => {
          rb1.rollbackTo
          StableId parse (builder, ScalaElementTypes.REFERENCE)
          val args = builder.mark
          builder.advanceLexer //Ate (

          def parseSeqWildcard(withComma: Boolean): Boolean = {
            if (if (withComma)
              lookAhead(builder, ScalaTokenTypes.tCOMMA, ScalaTokenTypes.tUNDER, ScalaTokenTypes.tIDENTIFIER)
            else lookAhead(builder, ScalaTokenTypes.tUNDER, ScalaTokenTypes.tIDENTIFIER)) {
              val wild = builder.mark
              if (withComma) builder.advanceLexer
              builder.getTokenType()
              builder.advanceLexer
              if (builder.getTokenType == ScalaTokenTypes.tIDENTIFIER && "*".equals(builder.getTokenText)) {
                builder.advanceLexer
                wild.done(ScalaElementTypes.SEQ_WILDCARD)
                true
              } else {
                wild.rollbackTo
                false
              }
            } else {
              false
            }
          }

          def parseSeqWildcardBinding(withComma: Boolean): Boolean = {
            if (if (withComma) lookAhead(builder, ScalaTokenTypes.tCOMMA, ScalaTokenTypes.tIDENTIFIER, ScalaTokenTypes.tAT,
            ScalaTokenTypes.tUNDER, ScalaTokenTypes.tIDENTIFIER)
            else lookAhead(builder, ScalaTokenTypes.tIDENTIFIER, ScalaTokenTypes.tAT,
            ScalaTokenTypes.tUNDER, ScalaTokenTypes.tIDENTIFIER)) {
              val wild = builder.mark
              if (withComma) builder.advanceLexer // ,
              builder.getTokenType
              if (isVarId) {
                builder.advanceLexer // id
              } else {
                wild.rollbackTo
                return false
              }
              builder.getTokenType
              builder.advanceLexer // @
              builder.getTokenType
              if (ParserUtils.eatSeqWildcardNext(builder)) {
                wild.done(ScalaElementTypes.NAMING_PATTERN)
                return true
              }
              else {
                wild.rollbackTo
                return false
              }
            }
            return false
          }

          if (!parseSeqWildcard(false) && !parseSeqWildcardBinding(false) && Pattern.parse(builder)) {
            while (builder.getTokenType == ScalaTokenTypes.tCOMMA && !parseSeqWildcard(true) && !parseSeqWildcardBinding(true)) {
              builder.advanceLexer // eat comma
              Pattern.parse(builder)
            }
          }
          builder.getTokenType match {
            case ScalaTokenTypes.tRPARENTHESIS => {
              builder.advanceLexer //Ate )
            }
            case _ => {
              builder error ErrMsg("rparenthesis.expected")
            }
          }
          args.done(ScalaElementTypes.PATTERN_ARGS)
          simplePatternMarker.done(ScalaElementTypes.CONSTRUCTOR_PATTERN)
          return true
        }
        case _ => {
          rb1.drop
          simplePatternMarker.done(ScalaElementTypes.STABLE_REFERENCE_PATTERN)
          return true
        }
      }
    }
    simplePatternMarker.rollbackTo
    return false
  }
}