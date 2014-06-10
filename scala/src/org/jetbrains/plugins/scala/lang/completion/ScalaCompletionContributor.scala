package org.jetbrains.plugins.scala
package lang
package completion

import com.intellij.codeInsight.completion._
import psi.api.expr.{ScPostfixExpr, ScInfixExpr, ScReferenceExpression}
import psi.api.ScalaFile
import psi.api.base.ScReferenceElement
import com.intellij.util.ProcessingContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.{PlatformPatterns}
import lexer.ScalaTokenTypes
import scala.util.Random
import resolve.ResolveUtils
import com.intellij.psi.{PsiMember, PsiElement}
import psi.api.statements.ScFun
import psi.api.base.patterns.ScBindingPattern
import psi.ScalaPsiUtil;

/**
 * @author Alexander Podkhalyuzin
 * Date: 16.05.2008
 */

class ScalaCompletionContributor extends CompletionContributor {
  extend(CompletionType.BASIC, PlatformPatterns.psiElement(ScalaTokenTypes.tIDENTIFIER), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      parameters.getPosition.getParent match {
        case ref: ScReferenceElement => {
          val variants = ref.getVariants
          for (variant <- variants) {
            variant match {
              case (el: LookupElement, elem: PsiElement, _) => {
                elem match {
                  case fun: ScFun => result.addElement(el)
                  case memb: PsiMember => {
                    if (parameters.getInvocationCount > 1 ||
                            ResolveUtils.isAccessible(memb, parameters.getPosition)) result.addElement(el)
                  }
                  case patt: ScBindingPattern => {
                    val context = ScalaPsiUtil.nameContext(patt)
                    context match {
                      case memb: PsiMember => {
                        if (parameters.getInvocationCount > 1 ||
                            ResolveUtils.isAccessible(memb, parameters.getPosition)) result.addElement(el)
                      }
                      case _ => result.addElement(el)
                    }
                  }
                  case _ => result.addElement(el)
                }
              }
              case _ =>
            }
          }
          result.stopHere
        }
        case _ =>
      }
    }
  })

  override def advertise(parameters: CompletionParameters): String = {
    if (!parameters.getOriginalFile.isInstanceOf[ScalaFile]) return null
    val messages = Array[String](
      null
    )
    messages apply (new Random).nextInt(messages.size)
  }

  override def beforeCompletion(context: CompletionInitializationContext) = {
    val rulezzz = CompletionInitializationContext.DUMMY_IDENTIFIER
    val offset = context.getStartOffset() - 1
    val file = context.getFile
    val element = file.findElementAt(offset);
    if (element != null && file.findReferenceAt(offset) != null && specialOperator(element.getParent)) {
      context.setFileCopyPatcher(new DummyIdentifierPatcher("+"));
    }
    super.beforeCompletion(context)
  }

  private def specialOperator(elem: PsiElement) = (elem match {
    case ref: ScReferenceExpression => ref.getParent match {
      case inf: ScInfixExpr if ref eq inf.operation => true
      case pos: ScPostfixExpr if ref eq pos.operation => true
      case _ => false
    }
    case _ => false
  }) && !Character.isJavaIdentifierPart(elem.getText.charAt(0))
}