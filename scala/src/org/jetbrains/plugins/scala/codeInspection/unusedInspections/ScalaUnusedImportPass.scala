package org.jetbrains.plugins.scala
package codeInspection
package unusedInspections


import annotator.importsTracker.ImportTracker
import collection.Set
import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.codeInsight.daemon.impl.{HighlightInfo, UpdateHighlightersUtil, AnnotationHolderImpl}
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.Annotation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import java.util.ArrayList
import lang.psi.api.ScalaFile
import lang.psi.api.toplevel.imports.ScImportStmt
import lang.psi.api.toplevel.imports.usages.{ImportWildcardSelectorUsed, ImportSelectorUsed, ImportExprUsed, ImportUsed}
/**
 * User: Alexander Podkhalyuzin
 * Date: 15.06.2009
 */

class ScalaUnusedImportPass(file: PsiFile, editor: Editor) extends TextEditorHighlightingPass(file.getProject, editor.getDocument) {
  def doApplyInformationToEditor: Unit = {
    if (file.isInstanceOf[ScalaFile]) {
      val sFile = file.asInstanceOf[ScalaFile]
      val annotationHolder = new AnnotationHolderImpl()
      val tracker = ImportTracker.getInstance(file.getProject)
      if (!tracker.isFileAnnotated(sFile)) return
      val unusedImports: Set[ImportUsed] = tracker.getUnusedImport(sFile)
      val annotations = unusedImports.flatMap({
        imp: ImportUsed => {
          val psi: PsiElement = imp match {
            case ImportExprUsed(expr) if !PsiTreeUtil.hasErrorElements(expr) => {
              val impSt = expr.getParent.asInstanceOf[ScImportStmt]
              if (impSt == null) null //todo: investigate this case, this cannot be null
              else if (impSt.importExprs.length == 1) impSt
              else expr
            }
            case ImportSelectorUsed(sel) => sel
            case ImportWildcardSelectorUsed(e) if e.selectors.length > 0 => e.wildcardElement.get
            case ImportWildcardSelectorUsed(e) if !PsiTreeUtil.hasErrorElements(e) => e.getParent
            case _ => null
          }
          psi match {
            case null => Seq[Annotation]()
            case _ => {
              val annotation: Annotation = annotationHolder.createWarningAnnotation(psi, "Unused import statement")
              annotation.setHighlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
              annotation.registerFix(new ScalaOptimizeImportsFix)
              annotation
              Seq[Annotation](annotation)
            }
          }
        }
      }).toSeq

      val holder = new HighlightInfoHolder(file)
      val list = new ArrayList[HighlightInfo](annotations.length)
      for (annotation <- annotations) {
        list.add(HighlightInfo.fromAnnotation(annotation))
      }
      UpdateHighlightersUtil.setHighlightersToEditor(file.getProject, editor.getDocument, 0, file.getTextLength, list, getId)
    }
  }

  def doCollectInformation(progress: ProgressIndicator): Unit = {

  }
}
