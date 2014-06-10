package org.jetbrains.plugins.scala
package lang
package psi
package types

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScDeclaration
import _root_.scala.collection.mutable.{HashSet, HashMap}
import api.statements._
import com.intellij.psi.PsiTypeParameter

case class ScCompoundType(val components: Seq[ScType], val decls: Seq[ScDeclaredElementsHolder], val typeDecls: Seq[ScTypeAlias]) extends ScType{
  //compound types are checked by checking the set of signatures in their refinements
  val signatureMap = new HashMap[Signature, ScType] {
    override def elemHashCode(s : Signature) = s.name.hashCode* 31 + s.types.length
  }

  type Bounds = Pair[ScType, ScType]
  val types = new HashMap[String, Bounds]

  for (typeDecl <- typeDecls) {
    types += ((typeDecl.name, (typeDecl.lowerBound, typeDecl.upperBound)))
  }

  for (decl <- decls) {
    decl match {
      case fun: ScFunction =>
        signatureMap += ((new PhysicalSignature(fun, ScSubstitutor.empty), fun.calcType))
      case varDecl: ScVariable => {
        varDecl.typeElement match {
          case Some(te) => for (e <- varDecl.declaredElements) {
            val varType = te.getType(Set(varDecl.declaredElements.toSeq: _*)).resType
            signatureMap += ((new Signature(e.name, Seq.empty, 0, ScSubstitutor.empty),varType))
            signatureMap += ((new Signature(e.name + "_", Seq.singleton(varType), 1, ScSubstitutor.empty), Unit)) //setter
          }
          case None =>
        }
      }
      case valDecl: ScValue => valDecl.typeElement match {
        case Some(te) => for (e <- valDecl.declaredElements) {
          val valType = te.getType(Set(valDecl.declaredElements.toSeq: _*)).resType
          signatureMap += ((new Signature(e.name, Seq.empty, 0, ScSubstitutor.empty), valType))
        }
        case None =>
      }
    }
  }

  override def equiv(t: ScType) = t match {
    case other : ScCompoundType => {
      (components.zip(other.components) forall {case (x, y) => x equiv y}) &&
      signatureMap.size == other.signatureMap.size &&
      signatureMap.elements.forall {case (sig, t) => other.signatureMap.get(sig) match {
        case None => false
        case Some(t1) => t equiv t1
      }
      } &&
      typesMatch(types, other.types)
    }
    case _ => false
  }

  private def typesMatch(types1 : HashMap[String, Bounds],
                         types2 : HashMap[String, Bounds]) : Boolean = {
    if (types1.size != types.size) return false
    else {
      for ((name, bounds1) <- types1) {
        types2.get(name) match {
          case None => return false
          case Some (bounds2) => if (!(bounds1._1 equiv bounds2._1) ||
                                     !(bounds1._2 equiv bounds2._2)) return false
        }
      }
      true
    }
  }
}