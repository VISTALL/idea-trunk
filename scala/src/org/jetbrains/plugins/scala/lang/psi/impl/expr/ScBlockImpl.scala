package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import _root_.scala.collection.mutable.HashMap
import api.toplevel.typedef.{ScClass, ScTypeDefinition, ScObject}
import api.toplevel.{ScTyped}
import com.intellij.psi.util.PsiTreeUtil
import types._
import psi.ScalaPsiElementImpl
import com.intellij.lang.ASTNode
import api.expr._
import api.toplevel.templates.ScTemplateBody
import api.statements.{ScDeclaredElementsHolder, ScTypeAlias}
import collection.Seq

/**
* @author ilyas
*/

class ScBlockImpl(node: ASTNode) extends ScalaPsiElementImpl(node) with ScBlock {

  override def toString: String = "BlockOfExpressions"

  protected override def innerType = lastExpr match {
    case None => Unit
    case Some(e) => {
      val m = new HashMap[String, ScExistentialArgument]
      def existize (t : ScType) : ScType = t match {
        case ScFunctionType(ret, params) => new ScFunctionType(existize(ret), collection.immutable.Seq(params.map({existize _}).toSeq: _*))
        case ScTupleType(comps) => new ScTupleType(collection.immutable.Seq(comps.map({existize _}).toSeq : _*))
        case ScDesignatorType(des) if PsiTreeUtil.isAncestor(this, des, true) => des match {
          case clazz : ScClass => {
            val t = existize(leastClassType(clazz))
            val vars = clazz.typeParameters.map{tp => ScalaPsiManager.typeVariable(tp)}.toList
            m.put(clazz.name, new ScExistentialArgument(clazz.name, vars, t, t))
            new ScTypeVariable(clazz.name)
          }
          case obj : ScObject => {
            val t = existize(leastClassType(obj))
            m.put(obj.name, new ScExistentialArgument(obj.name, Nil, t, t))
            new ScTypeVariable(obj.name)
          }
          case typed : ScTyped => {
            val t = existize(typed.calcType)
            m.put(typed.name, new ScExistentialArgument(typed.name, Nil, t, t))
            new ScTypeVariable(typed.name)
          }
        }
        case ScProjectionType(p, ref) => new ScProjectionType(existize(p), ref)
        case ScCompoundType(comps, decls, types) => new ScCompoundType(collection.immutable.Seq(comps.map({existize _}).toSeq: _*), decls, types)
        case ScParameterizedType (des, typeArgs) =>
          new ScParameterizedType(existize(des), collection.immutable.Seq(typeArgs.map({existize _}).toSeq : _*))
        case ScExistentialArgument(name, args, lower, upper) => new ScExistentialArgument(name, args, existize(lower), existize(upper))
        case ex@ScExistentialType(q, wildcards) => {
           new ScExistentialType(existize(q), wildcards.map {ex =>
                   new ScExistentialArgument(ex.name, ex.args, existize(ex.lowerBound), existize(ex.upperBound))})
        }
        case singl : ScSingletonType => existize(singl.pathType)
        case _ => t
      }
      val t = existize(e.getType)
      if (m.size == 0) t else new ScExistentialType(t, m.values.toList)
    }
  }

  private def leastClassType(t : ScTypeDefinition) = {
    val (holders, aliases): (Seq[ScDeclaredElementsHolder], Seq[ScTypeAlias]) = t.extendsBlock.templateBody match {
      case Some(b: ScTemplateBody) => {
        // jzaugg: Without these type annotations, a class cast exception occured above. I'm not entirely sure why.
        (b.holders: Seq[ScDeclaredElementsHolder], b.aliases: Seq[ScTypeAlias])
      }
      case None => (Seq.empty, Seq.empty)
    }

    val superTypes = t.extendsBlock.superTypes
    if (superTypes.length > 1 || !holders.isEmpty || !aliases.isEmpty) {
      new ScCompoundType(superTypes, holders.toList, aliases.toList)
    } else superTypes(0)
  }
}