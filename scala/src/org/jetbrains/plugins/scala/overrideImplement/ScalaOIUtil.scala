package org.jetbrains.plugins.scala
package overrideImplement

import com.intellij.codeInsight.generation.{PsiMethodMember, OverrideImplementUtil, ClassMember, PsiFieldMember}
import com.intellij.openapi.editor.{Editor, VisualPosition}

import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

import com.intellij.ui.NonFocusableCheckBox
import com.intellij.util.{IncorrectOperationException, SmartList}
import java.awt.Component

import javax.swing.{JCheckBox, JComponent}
import lang.lexer.ScalaTokenTypes
import com.intellij.psi._
import lang.psi.api.base.{ScReferenceElement, ScStableCodeReferenceElement, ScFieldId}
import lang.psi.api.base.patterns.ScReferencePattern
import lang.psi.api.toplevel.templates.ScTemplateBody
import lang.psi.api.toplevel.typedef.{ScTrait, ScTypeDefinition, ScMember, ScTemplateDefinition}
import lang.psi.api.toplevel.{ScModifierListOwner, ScTyped}
import lang.psi.impl.toplevel.typedef.TypeDefinitionMembers
import lang.psi.api.base.types.ScSimpleTypeElement
import lang.psi.impl.toplevel.synthetic.ScSyntheticClass
import lang.psi.api.statements._
import com.intellij.ide.highlighter.JavaFileType
import lang.psi.impl.ScalaPsiElementFactory
import lang.psi.types.{FullSignature, ScType, PhysicalSignature, ScSubstitutor}
import lang.psi.{ScalaPsiUtil, ScalaPsiElement}
import org.jetbrains.plugins.scala.util.ScalaUtils
import com.intellij.ide.util.MemberChooser
import _root_.scala.collection.mutable.ArrayBuffer
import com.intellij.psi.infos.CandidateInfo
import com.intellij.openapi.project.Project
import settings.ScalaApplicationSettings

/**
 * User: Alexander Podkhalyuzin
 * Date: 08.07.2008
 */

object ScalaOIUtil {
  def invokeOverrideImplement(project: Project, editor: Editor, file: PsiFile, isImplement: Boolean) {
    val elem = file.findElementAt(editor.getCaretModel.getOffset - 1)
    def getParentClass(elem: PsiElement): PsiElement = {
      elem match {
        case _: ScTemplateDefinition | null => return elem
        case _ => getParentClass(elem.getParent)
      }
    }
    val parent = getParentClass(elem)
    if (parent == null) return
    val clazz = parent.asInstanceOf[ScTemplateDefinition]
    //val candidates = if (isImplement) ScalaOIUtil.getMembersToImplement(clazz) else ScalaOIUtil.getMembersToOverride(clazz)
    val candidates = if (isImplement) getMembersToImplement(clazz) else getMembersToOverride(clazz)
    if (candidates.isEmpty) return
    val classMembersBuf = new ArrayBuffer[ClassMember]
    for (candidate <- candidates) {
      candidate match {
        case sign: PhysicalSignature => classMembersBuf += new ScMethodMember(sign)
        case (name: PsiNamedElement, subst: ScSubstitutor) => {
          ScalaPsiUtil.nameContext(name) match {
            case x: ScValue => {
              name match {
                case y: ScTyped => classMembersBuf += new ScValueMember(x, y, subst)
                case _ => throw new IncorrectOperationException("Not supported type:" + x)
              }
            }
            case x: ScVariable => {
              name match {
                case y: ScTyped => classMembersBuf += new ScVariableMember(x, y, subst)
                case _ => throw new IncorrectOperationException("Not supported type:" + x)
              }
            }
            case x: ScTypeAlias => classMembersBuf += new ScAliasMember(x, subst)
            case x => throw new IncorrectOperationException("Not supported type:" + x)
          }
        }
        case x => throw new IncorrectOperationException("Not supported type:" + x)
      }
    }
    val classMembers = classMembersBuf.toArray
    val chooser = new {
      private val dontInferReturnTypeCheckBox: JCheckBox = new NonFocusableCheckBox(
        ScalaBundle.message("specify.return.type.explicitly"))
    }
       with MemberChooser[ClassMember](classMembers, false, true, project) {
      if (ScalaApplicationSettings.getInstance.SPECIFY_RETURN_TYPE_EXPLICITLY != null)
        dontInferReturnTypeCheckBox.setSelected(ScalaApplicationSettings.getInstance.SPECIFY_RETURN_TYPE_EXPLICITLY.booleanValue)
      override def customizeOptionsPanel: java.util.List[JComponent] = {
        val list = new SmartList[JComponent]
        list.add(dontInferReturnTypeCheckBox)
        return list
      }
      def needsInferType = dontInferReturnTypeCheckBox.isSelected
    }
    chooser.setTitle(if (isImplement) ScalaBundle.message("select.method.implement")
                     else ScalaBundle.message("select.method.override"))
    chooser.show

    val selectedMembers = chooser.getSelectedElements
    if (selectedMembers == null || selectedMembers.size == 0) return
    val needsInferType = chooser.needsInferType
    ScalaApplicationSettings.getInstance.SPECIFY_RETURN_TYPE_EXPLICITLY = needsInferType
    runAction(selectedMembers, isImplement, clazz, editor, needsInferType)
  }

  def runAction(selectedMembers: java.util.List[ClassMember],
               isImplement: Boolean, clazz: ScTemplateDefinition, editor: Editor, needsInferType: Boolean) {
    ScalaUtils.runWriteAction(new Runnable {
      def run {
        for (member <- selectedMembers.toArray(new Array[ClassMember](selectedMembers.size))) {
          val offset = editor.getCaretModel.getOffset
          val anchor = getAnchor(offset, clazz)
          member match {
            case member: ScMethodMember => {
              val method: PsiMethod = member.getElement
              val sign = member.sign

              val m = ScalaPsiElementFactory.createOverrideImplementMethod(sign, method.getManager, !isImplement, needsInferType)
              adjustTypesAndSetCaret(clazz.addMember(m, anchor), editor)
            }
            case member: ScAliasMember => {
              val alias = member.getElement
              val substitutor = member.substitutor
              val m = ScalaPsiElementFactory.createOverrideImplementType(alias, substitutor, alias.getManager, !isImplement)
              adjustTypesAndSetCaret(clazz.addMember(m, anchor), editor)
            }
            case _: ScValueMember | _: ScVariableMember => {
              val isVal = member match {case _: ScValueMember => true case _: ScVariableMember => false}
              val value = member match {case x: ScValueMember => x.element case x: ScVariableMember => x.element}
              val substitutor = member match {case x: ScValueMember => x.substitutor case x: ScVariableMember => x.substitutor}
              val m = ScalaPsiElementFactory.createOverrideImplementVariable(value, substitutor, value.getManager,
                !isImplement, isVal, needsInferType)
              adjustTypesAndSetCaret(clazz.addMember(m, anchor), editor)
            }
            case _ =>
          }
        }
      }
    }, clazz.getProject, if (isImplement) "Implement method" else "Override method")
  }

  def getMembersToImplement(clazz: ScTemplateDefinition): Seq[ScalaObject] = {
    val buf = new ArrayBuffer[ScalaObject]
    buf ++= clazz.allSignatures
    buf ++= clazz.allTypeAliases
    buf ++= clazz.allVals
    val buf2 = new ArrayBuffer[ScalaObject]
    for (element <- buf) {
      element match {
        case FullSignature(sign: PhysicalSignature, _, _, _) => {
          val m = sign.method
          val name = if (m == null) "" else m.getName
          m match {
            case _ if isProductAbstractMethod(m, clazz) =>
            case x if x.getName == "$tag" || x.getName == "$init$" =>
            case x if x.getContainingClass == clazz =>
            case x if x.getContainingClass.isInterface && !x.getContainingClass.isInstanceOf[ScTrait] => {
              buf2 += sign
            }
            case x if x.hasModifierProperty("abstract") => {
              buf2 += sign
            }
            case x: ScFunctionDeclaration => {
              buf2 += sign
            }
            case _ =>
          }
        }
        case (name: PsiNamedElement, subst: ScSubstitutor) => {
          ScalaPsiUtil.nameContext(name) match {
            case x: ScValueDeclaration if x.getContainingClass != clazz => buf2 += element
            case x: ScVariableDeclaration if x.getContainingClass != clazz => buf2 += element
            case x: ScTypeAliasDeclaration if x.getContainingClass != clazz => buf2 += element
            case _ =>
          }
        }
        case _ =>
      }
    }
    return buf2.toSeq
  }

  def isProductAbstractMethod(m: PsiMethod, clazz: PsiClass) : Boolean = clazz match {
    case td: ScTypeDefinition if td.isCase => {
      if (m.getName == "apply") return true
      val clazz = m.getContainingClass
      clazz != null && clazz.getQualifiedName == "scala.Product" &&
              (m.getName match {
                case "productArity" | "productElement" => true
                case _ => false
              })
    }
    case x : ScTemplateDefinition => (x.superTypes.map(t => ScType.extractClassType(t)).find{
      case Some((c, _)) if c != x /*variant for infinite loop*/ => isProductAbstractMethod(m, c)
      case _ => false
    }) match {
      case Some(_) => true
      case _ => false
    }
    case _ => false
  }

  def getMembersToOverride(clazz: ScTemplateDefinition): Seq[ScalaObject] = {
    val buf = new ArrayBuffer[ScalaObject]
    buf ++= clazz.allMethods
    buf ++= clazz.allTypeAliases
    buf ++= clazz.allVals
    val buf2 = new ArrayBuffer[ScalaObject]
    for (element <- buf) {
      element match {
        case FullSignature(_, _, _, _)  | _: PhysicalSignature => {
          val sign: PhysicalSignature = element match {
            case FullSignature(x: PhysicalSignature, _, _, _) => x
            case x: PhysicalSignature => x
          }
          sign.method match {
            case _ if isProductAbstractMethod(sign.method, clazz) => buf2 += sign
            case _: ScFunctionDeclaration =>
            case x if x.getName == "$tag" || x.getName == "$init$"=>
            case x if x.getContainingClass == clazz =>
            case x: PsiModifierListOwner if x.hasModifierProperty("abstract")
                || x.hasModifierProperty("final") /*|| x.hasModifierProperty("sealed")*/ =>
            case x if x.isConstructor =>
            case method => {
              var flag = false
              if (method match {case x: ScFunction => x.parameters.length == 0 case _ => method.getParameterList.getParametersCount == 0}) {
                for (pair <- clazz.allVals; v = pair._1) if (v.getName == method.getName) {
                  ScalaPsiUtil.nameContext(v) match {
                    case x: ScValue if x.getContainingClass == clazz => flag = true
                    case x: ScVariable if x.getContainingClass == clazz => flag = true
                    case _ =>
                  }
                }
              }
              if (!flag) buf2 += sign
            }
          }
        }
        case (name: PsiNamedElement, subst: ScSubstitutor) => {
          ScalaPsiUtil.nameContext(name) match {
            case x: ScPatternDefinition if x.getContainingClass != clazz => {
              var flag = false
              for (signe <- clazz.allMethods if signe.method.getContainingClass == clazz) {
                //getContainingClass == clazz so we sure that this is ScFunction (it is safe cast)
                if (signe.method.asInstanceOf[ScFunction].parameters.length == 0 && signe.method.getName == x.getName) flag = true
              }
              for (pair <- clazz.allVals; v = pair._1) if (v.getName == name.getName) {
                ScalaPsiUtil.nameContext(v) match {
                  case x: ScValue if x.getContainingClass == clazz => flag = true
                  case x: ScVariable if x.getContainingClass == clazz => flag = true
                  case _ =>
                }
              }
              if (!flag) buf2 += element
            }
            case x: ScVariableDefinition if x.getContainingClass != clazz => {
              var flag = false
              for (signe <- clazz.allMethods if signe.method.getContainingClass == clazz) {
                //getContainingClass == clazz so we sure that this is ScFunction (it is safe cast)
                if (signe.method.isInstanceOf[ScFunction] &&
                        signe.method.asInstanceOf[ScFunction].parameters.length == 0 &&
                        signe.method.getName == x.getName) flag = true
              }
              for (pair <- clazz.allVals; v = pair._1) if (v.getName == name.getName) {
                ScalaPsiUtil.nameContext(v) match {
                  case x: ScValue if x.getContainingClass == clazz => flag = true
                  case x: ScVariable if x.getContainingClass == clazz => flag = true
                  case _ =>
                }
              }
              if (!flag) buf2 += element
            }
            case x: ScTypeAliasDefinition if x.getContainingClass != clazz => buf2 += element
            case _ =>
          }
        }
        case _ =>
      }
    }

    return buf2.toSeq
  }



  def getMethod(clazz: ScTypeDefinition, methodName: String, isImplement: Boolean): ScMember = {
    val seq: Seq[ScalaObject] = if (isImplement) getMembersToImplement(clazz) else getMembersToOverride(clazz)
    def getObjectByName: ScalaObject = {
      for (obj <- seq) {
        obj match {
          case sign: PhysicalSignature if sign.method.getName == methodName => return sign
          case obj@(name: PsiNamedElement, subst: ScSubstitutor) if name.getName == methodName => return obj
          case _ =>
        }
      }
      return null
    }
    val obj = getObjectByName
    if (obj == null) return null
    obj match {
      case sign: PhysicalSignature => {
        val method: PsiMethod = sign.method
        return ScalaPsiElementFactory.createOverrideImplementMethod(sign, method.getManager, !isImplement, true)
      }
      case (name: PsiNamedElement, subst: ScSubstitutor) => {
        val element: PsiElement = ScalaPsiUtil.nameContext(name)
        element match {
          case alias: ScTypeAlias => {
            return ScalaPsiElementFactory.createOverrideImplementType(alias, subst, alias.getManager, !isImplement)
          }
          case _: ScValue | _: ScVariable => {
            val typed: ScTyped = name match {case x: ScTyped => x case _ => return null}
            return ScalaPsiElementFactory.createOverrideImplementVariable(typed, subst, typed.getManager, !isImplement, 
              element match {case _: ScValue => true case _ => false}, true)
          }
          case _ => return null
        }
      }
    }
  }

  def getAnchor(offset: Int, clazz: ScTemplateDefinition) : Option[ScMember] = {
    val body = clazz.extendsBlock.templateBody match {
      case Some(x) => x
      case None => return None
    }
    var element: PsiElement = body.getContainingFile.findElementAt(offset)
    while (element != null && element.getParent != body) element = element.getParent

    element match {
      case member: ScMember => Some(member)
      case null => None
      case _ => PsiTreeUtil.getNextSiblingOfType(element, classOf[ScMember]) match {
        case null => None
        case member => Some(member)
      }
    }
  }

  private def adjustTypesAndSetCaret(meth: PsiElement, editor: Editor): Unit = {
    ScalaPsiUtil.adjustTypes(meth)
    //hack for postformatting IDEA bug.
    val member = CodeStyleManager.getInstance(meth.getProject()).reformat(meth)
    //Setting selection
    val body: PsiElement = member match {
      case meth: ScTypeAliasDefinition => meth.aliasedTypeElement
      case meth: ScPatternDefinition => meth.expr
      case meth: ScVariableDefinition => meth.expr
      case method: ScFunctionDefinition => method.body match {
        case Some(x) => x
        case None => return
      }
      case _ => return
    }
    val offset = body.getTextRange.getStartOffset
    editor.getCaretModel.moveToOffset(offset)
    editor.getSelectionModel.setSelection(body.getTextRange.getStartOffset, body.getTextRange.getEndOffset)
  }
}