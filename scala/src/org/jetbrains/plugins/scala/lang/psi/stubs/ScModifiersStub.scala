package org.jetbrains.plugins.scala
package lang
package psi
package stubs

import api.base.ScModifierList
import com.intellij.psi.stubs.StubElement

/**
 * User: Alexander Podkhalyuzin
 * Date: 21.01.2009
 */

trait ScModifiersStub extends StubElement[ScModifierList] {
  def getModifiers(): Array[String]
}