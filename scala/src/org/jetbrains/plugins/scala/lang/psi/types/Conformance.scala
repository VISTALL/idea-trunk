package org.jetbrains.plugins.scala
package lang
package psi
package types

import _root_.scala.collection.mutable.HashMap
import caches.CachesUtil
import com.intellij.openapi.progress.ProgressManager
import psi.impl.toplevel.synthetic.ScSyntheticClass
import org.jetbrains.plugins.scala.Misc._
import api.statements._
import params._
import api.toplevel.typedef.ScTypeDefinition
import impl.toplevel.typedef.TypeDefinitionMembers
import _root_.scala.collection.immutable.HashSet

import com.intellij.psi._
import util.PsiModificationTracker

object Conformance {

  /**
   * Checks, whether the following assignment is correct:
   * val x: l = (y: r) 
   */
  def conforms(l: ScType, r: ScType): Boolean = conforms(l, r, HashSet.empty)

  def conformsSeq(ls: Seq[ScType], rs: Seq[ScType]): Boolean = ls.length == rs.length && ls.zip(rs).foldLeft(true)((z, p) => z && conforms(p._1, p._2, HashSet.empty))

  private def conforms(l: ScType, r: ScType, visited: Set[PsiClass]): Boolean = {
    ProgressManager.getInstance.checkCanceled

    def scalaCompilerIsTheBestCompilerInTheWorld = l match {
      case ScTypeParameterType(_, _, lower, upper, ptp) => conforms(upper.v, r) && conforms(r, lower.v)
    }

    if (l.isInstanceOf[ScTypeParameterType]) return scalaCompilerIsTheBestCompilerInTheWorld
    if (r == Nothing) true
    else if (l equiv r) true
    else l match {
      case Any => true
      case Nothing => false
      case Null => r == Nothing
      case AnyRef => r match {
        case Null => true
        case _: ScParameterizedType => true
        case _: ScDesignatorType => true
        case _: ScSingletonType => true
        case _ => false
      }
      case Singleton => r match {
        case _: ScSingletonType => true
        case _ => false
      }
      case AnyVal => r match {
        case _: ValType => true
        case _ => false
      }

      case ScSkolemizedType(_, _, lower, _) => conforms(lower, r)
      case ScPolymorphicType(_, _, lower, _) => conforms(lower.v, r) //todo implement me

      case ScParameterizedType(owner: ScType, args1) => r match { //Parametrized type can have not only designators (projection)
        case ScParameterizedType(owner1, args2) => {
          if (!(owner equiv owner1)) return false
          if (args1.length != args2.length) return false
          ScType.extractDesignated(owner) match {
            case Some((owner: PsiClass, _)) => {
              owner.getTypeParameters.zip(args1 zip args2) forall {
                case (tp, argsPair) => tp match {
                  case scp: ScTypeParam if (scp.isCovariant) => if (!argsPair._1.conforms(argsPair._2)) return false
                  case scp: ScTypeParam if (scp.isContravariant) => if (!argsPair._2.conforms(argsPair._1)) return false
                  case _ => argsPair._1 match {
                    case _: ScExistentialArgument => if (!argsPair._2.conforms(argsPair._1)) return false
                    case _ => if (!argsPair._1.equiv(argsPair._2)) return false
                  }
                }
                true
              }
            }
            case _ => rightRec(l, r, visited)
          }
        }
        case _ => rightRec(l, r, visited)
      }

      case c@ScCompoundType(comps, decls, types) => comps.forall(_ conforms r) && (ScType.extractClassType(r) match {
        case Some((clazz, subst)) => {
          if (!decls.isEmpty || (comps.isEmpty && decls.isEmpty && types.isEmpty)) { //if decls not empty or it's synthetic created
            val sigs = getSignatureMap(clazz)
            for ((sig, t) <- c.signatureMap) {
              sigs.get(sig) match {
                case None => return false
                case Some(t1) => if (!subst.subst(t1).conforms(t)) return false
              }
            }
          }
          if (!types.isEmpty) {
            val hisTypes = TypeDefinitionMembers.getTypes(clazz)
            for (t <- types) {
              hisTypes.get(t) match {
                case None => return false
                case Some(n) => {
                  val subst1 = n.substitutor
                  n.info match {
                    case ta: ScTypeAlias => {
                      val s = subst1 followed subst
                      if (!s.subst(ta.upperBound).conforms(t.upperBound) ||
                              !t.lowerBound.conforms(s.subst(ta.lowerBound))) return false
                    }
                    case inner: PsiClass => {
                      val des = ScParameterizedType.create(inner, subst1 followed subst)
                      if (!subst.subst(des).conforms(t.upperBound) || !t.lowerBound.conforms(des)) return false
                    }
                  }
                }
              }
            }
          }
          true
        }
        case None => r match {
          case c1@ScCompoundType(comps1, _, _) => comps1.forall(c conforms _) && (
                  c1.signatureMap.forall {
                    p => {
                      val s1 = p._1
                      val rt1 = p._2
                      c.signatureMap.get(s1) match {
                        case None => comps.find {
                          t => ScType.extractClassType(t) match {
                            case None => false
                            case Some((clazz, subst)) => {
                              val classSigs = getSignatureMap(clazz)
                              classSigs.get(s1) match {
                                case None => false
                                case Some(rt) => rt1.conforms(subst.subst(rt))
                              }
                            }
                          }
                        }
                        case Some(rt) => rt1.conforms(rt)
                      }
                      //todo check for refinement's type decls
                    }
                  })
          case _ => false
        }
      })

      case ScExistentialArgument(_, params, lower, upper) if params.isEmpty => conforms(upper, r)
      case ex@ScExistentialType(q, wilds) => conforms(ex.substitutor.subst(q), r)
      case proj: ScProjectionType => {
        proj.element match {
          case Some(clazz: ScSyntheticClass) => conforms(clazz.t, r, visited + clazz)
          case Some(clazz: PsiClass) if !visited.contains(clazz) => BaseTypes.get(proj).find {t => conforms(l, t, visited + clazz)}
          //todo should this immediate return false?
          case _ => rightRec(l, r, visited)
        }
      }
      case _ => rightRec(l, r, visited)
    }
  }

  private def rightRec(l: ScType, r: ScType, visited: Set[PsiClass]): Boolean = r match {
    case sin: ScSingletonType => conforms(l, sin.pathType)

    case ScDesignatorType(td: ScTypeDefinition) => if (visited.contains(td)) false else td.superTypes.find {t => conforms(l, t, visited + td)}
    case ScDesignatorType(clazz: PsiClass) =>
      clazz.getSuperTypes.find {t => conforms(l, ScType.create(t, clazz.getProject), visited + clazz)}

    case projectionType: ScProjectionType => {
      projectionType.element match {
        case Some(syntheticClass: ScSyntheticClass) => conforms(l, syntheticClass.t)
        case Some(clazz: PsiClass) if !visited.contains(clazz) => BaseTypes.get(projectionType).find {t => conforms(l, t, visited + clazz)}
        case _ => false
      }
    }

    case ScPolymorphicType(_, _, _, upper) => {
      val uBound = upper.v
      ScType.extractClassType(uBound) match {
        case Some((pc, _)) if visited.contains(pc) => conforms(l, ScDesignatorType(pc), visited + pc)
        case Some((pc, _)) => conforms(l, uBound, visited + pc)
        case None => conforms(l, uBound, visited)
      }
    }
    case ScSkolemizedType(_, _, _, upper) => conforms(l, upper)

    case p: ScParameterizedType => {
      ScType.extractClassType(p) match {
        case Some((td: ScTypeDefinition, s)) => {
          if (!visited.contains(td)) td.superTypes.find {t => conforms(l, s.subst(t), visited + td)} else false
        }
        case Some((clazz, s)) => {
          clazz.getSuperTypes.find {t => conforms(l, s.subst(ScType.create(t, clazz.getProject)), visited + clazz)}
        }
        case _ => false
      }
    }

    case ScCompoundType(comps, _, _) => comps.find(conforms(l, _))

    case ScExistentialArgument(_, params, _, upper) if params.isEmpty => conforms(l, upper)

    case ex: ScExistentialType => conforms(l, ex.skolem)

    case _ => false //todo
  }

  def getSignatureMapInner(clazz: PsiClass): HashMap[Signature, ScType] = {
    val m = new HashMap[Signature, ScType]
    for ((full, _) <- TypeDefinitionMembers.getSignatures(clazz)) {
      m += ((full.sig, full.retType))
    }
    m
  }

  def getSignatureMap(clazz: PsiClass): HashMap[Signature, ScType] = {
    CachesUtil.get(
      clazz, CachesUtil.SIGNATURES_MAP_KEY,
      new CachesUtil.MyProvider(clazz, {clazz: PsiClass => getSignatureMapInner(clazz)})
        (PsiModificationTracker.MODIFICATION_COUNT)
    )
  }
}