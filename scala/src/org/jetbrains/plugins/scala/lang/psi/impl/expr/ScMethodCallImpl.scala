package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import api.base.ScStableCodeReferenceElement
import api.statements.{ScFunction, ScFun}
import api.toplevel.ScTyped
import api.toplevel.typedef.{ScClass, ScObject}
import psi.ScalaPsiElementImpl
import com.intellij.lang.ASTNode
import api.expr._
import toplevel.synthetic.{ScSyntheticClass, ScSyntheticFunction}
import lang.resolve.{MethodResolveProcessor, ScalaResolveResult}
import api.base.types.ScTypeElement
import types._
import com.intellij.psi._

/**
 * @author Alexander Podkhalyuzin
 * Date: 06.03.2008
 */

class ScMethodCallImpl(node: ASTNode) extends ScalaPsiElementImpl(node) with ScMethodCall {
  override def toString: String = "MethodCall"

  protected override def innerType: ScType = {
    /**
     * Utility method to get type for apply (and update) methods of concrecte class.
     */
    def processType(tp: ScType): ScType = {
      val isUpdate = getContext.isInstanceOf[ScAssignStmt] && getContext.asInstanceOf[ScAssignStmt].getLExpression == this
      val methodName = if (isUpdate) "update" else "apply"
      val args: Seq[ScExpression] = this.args.exprs ++ (
              if (isUpdate) getContext.asInstanceOf[ScAssignStmt].getRExpression match {
                case Some(x) => Seq[ScExpression](x)
                case None =>
                  Seq[ScExpression](ScalaPsiElementFactory.createExpressionFromText("{val x: Nothing = null; x}",
                    getManager)) //we can't to not add something => add Nothing expression
              }
              else Seq.empty)
      val typeArgs: Seq[ScTypeElement] = getInvokedExpr match {
        case gen : ScGenericCall => gen.arguments
        case _ => Seq.empty
      }
      val processor = new MethodResolveProcessor(getInvokedExpr, methodName, args :: Nil,
        typeArgs, expectedType)
      processor.processType(tp, getInvokedExpr, ResolveState.initial)
      val candidates = processor.candidates
      if (candidates.length != 1) Nothing
      else {
        candidates(0) match {
          case ScalaResolveResult(fun: PsiMethod, s: ScSubstitutor) => {
            fun match {
              case fun: ScFun => s.subst(fun.retType)
              case fun: ScFunction => s.subst(fun.returnType)
              case meth: PsiMethod => s.subst(ScType.create(meth.getReturnType, getProject))
            }
          }
          case _ => Nothing
        }
      }
      //add implicit types check
    }
    val invokedType = getInvokedExpr.getType
    if (invokedType == types.Nothing) return Nothing
    invokedType match {
      case ScFunctionType(retType: ScType, params: Seq[ScType]) => {
        retType
      }
      case tp: ScType => {
        ScType.extractClassType(tp) match {
          case Some((clazz: PsiClass, subst: ScSubstitutor)) => {
            clazz match {
              case clazz: ScClass if clazz.isCase => tp //todo: this is wrong if reference isn't class name
              case _ => processType(tp)
            }
          }
          case _ => Nothing
        }
      }
    }
  }
}