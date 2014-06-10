package org.jetbrains.plugins.scala
package lang
package psi
package stubs
package elements
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.{StubElement, IndexSink, IStubElementType, PsiFileStub}
import wrappers.IStubElementTypeWrapper
import com.intellij.psi.{PsiFile, PsiElement}

/**
 * @author ilyas
 */

abstract class ScStubElementType[S <: StubElement[T], T <: PsiElement](debugName: String)
extends IStubElementTypeWrapper[S, T](debugName) {

  def getExternalId = "sc." + super.toString()

  def isCompiled(stub: S) = {
    var parent = stub
    while (!(parent.isInstanceOf[PsiFileStub[_ <: PsiFile]])) {
      parent = parent.getParentStub.asInstanceOf[S]
    }
    parent.asInstanceOf[ScFileStub].isCompiled
  }


  override def shouldCreateStub(node: ASTNode): Boolean = ScalaPsiUtil.shouldCreateStub(node.getPsi)
}