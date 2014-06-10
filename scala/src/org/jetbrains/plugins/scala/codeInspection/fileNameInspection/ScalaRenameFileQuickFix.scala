package org.jetbrains.plugins.scala
package codeInspection
package fileNameInspection


import com.intellij.codeInspection.{ProblemDescriptor, LocalQuickFix}
import com.intellij.openapi.project.Project
import com.intellij.refactoring.RefactoringFactory
import java.lang.String
import lang.psi.api.ScalaFile
import com.intellij.refactoring.openapi.impl.RenameRefactoringImpl

/**
 * User: Alexander Podkhalyuzin
 * Date: 03.07.2009
 */

class ScalaRenameFileQuickFix(file: ScalaFile, name: String) extends LocalQuickFix {
  def applyFix(project: Project, descriptor: ProblemDescriptor): Unit = {
    (new RenameRefactoringImpl(project, file, name, false, true)).run
  }

  def getName: String = "Rename File " + file.getName + " to " + name

  def getFamilyName: String = "Rename File"
}