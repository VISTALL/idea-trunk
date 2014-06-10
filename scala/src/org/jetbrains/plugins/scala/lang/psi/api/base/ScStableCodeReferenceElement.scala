package org.jetbrains.plugins.scala
package lang
package psi
package api
package base


import com.intellij.psi.PsiElement

trait ScStableCodeReferenceElement extends ScReferenceElement with ScPathElement {
  def qualifier = findChild(classOf[ScStableCodeReferenceElement])
  def pathQualifier = findChild(classOf[ScPathElement])

  def qualName: String = (qualifier match {
    case Some(x) => x.qualName + "."
    case _ => ""
  }) + refName
}
