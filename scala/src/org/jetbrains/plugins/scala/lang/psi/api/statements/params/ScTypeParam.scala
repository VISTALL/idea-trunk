package org.jetbrains.plugins.scala
package lang
package psi
package api
package statements
package params

import toplevel.{ScTypeBoundsOwner, ScTypeParametersOwner, ScPolymorphicElement}
import types.ScType
import psi.ScalaPsiElement
import com.intellij.psi._
import toplevel.typedef.ScTypeDefinition

/** 
* @author Alexander Podkhalyuzin
* Date: 22.02.2008
*/

trait ScTypeParam extends ScalaPsiElement with ScPolymorphicElement with PsiTypeParameter {
  def isCovariant() : Boolean
  def isContravariant() : Boolean

  def owner : ScTypeParametersOwner
}