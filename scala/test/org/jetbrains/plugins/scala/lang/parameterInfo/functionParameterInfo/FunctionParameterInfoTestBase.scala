package org.jetbrains.plugins.scala
package lang
package parameterInfo
package functionParameterInfo

import _root_.scala.collection.mutable.ArrayBuffer
import _root_.scala.runtime.RichString
import _root_.scala.util.Sorting
import base.ScalaPsiTestCase
import com.intellij.codeInsight.hint.{HintUtil, ShowParameterInfoContext}
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.openapi.fileEditor.{OpenFileDescriptor, FileEditorManager}
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiComment, PsiManager, PsiElement}
import java.awt.Color
import java.io.File
import java.lang.String
import com.intellij.vcsUtil.VcsUtil
import lexer.ScalaTokenTypes
import psi.api.ScalaFile

/**
 * User: Alexander Podkhalyuzin
 * Date: 02.03.2009
 */

abstract class FunctionParameterInfoTestBase extends ScalaPsiTestCase {
  val caretMarker = "/*caret*/"


  override protected def rootPath = super.rootPath + "parameterInfo/functionParameterInfo/"

  override protected def doTest {
    import _root_.junit.framework.Assert._
    val filePath = rootPath + getTestName(false) + ".scala"
    val file = LocalFileSystem.getInstance.findFileByPath(filePath.replace(File.separatorChar, '/'))
    assert(file != null, "file " + filePath + " not found")
    val scalaFile: ScalaFile = PsiManager.getInstance(myProject).findFile(file).asInstanceOf[ScalaFile]
    val fileText = scalaFile.getText
    val offset = fileText.indexOf(caretMarker)
    assert(offset != -1, "Not specified caret marker in test case. Use /*caret*/ in scala file for this.")
    val fileEditorManager = FileEditorManager.getInstance(myProject)
    val editor = fileEditorManager.openTextEditor(new OpenFileDescriptor(myProject, file, offset), false)
    val context = new ShowParameterInfoContext(editor, myProject, scalaFile, offset, -1)
    val handler = new ScalaFunctionParameterInfoHandler
    val leafElement = scalaFile.findElementAt(offset)
    val element = PsiTreeUtil.getParentOfType(leafElement, handler.getArgumentListClass)
    handler.findElementForParameterInfo(context)
    val items = new ArrayBuffer[String]

    for (item <- context.getItemsToShow) {
      val uiContext = new ParameterInfoUIContext {
        def getDefaultParameterColor: Color = HintUtil.INFORMATION_COLOR
        def setupUIComponentPresentation(text: String, highlightStartOffset: Int, highlightEndOffset: Int,
                                        isDisabled: Boolean, strikeout: Boolean, isDisabledBeforeHighlight: Boolean,
                                        background: Color): Unit = {
          items.append(text)
        }
        def isUIComponentEnabled: Boolean = false
        def getCurrentParameterIndex: Int = 0
        def getParameterOwner: PsiElement = element
        def setUIComponentEnabled(enabled: Boolean): Unit = {}
      }
      handler.updateUI(item, uiContext)
    }

    val itemsArray = items.toArray
    Sorting.quickSort[String](itemsArray)

    val res = new StringBuilder("")

    for (item <- itemsArray) res.append(item).append("\n")
    if (res.length > 0) res.replace(res.length - 1, res.length, "")
    println("------------------------ " + scalaFile.getName + " ------------------------")
    println(res)
    val lastPsi = scalaFile.findElementAt(scalaFile.getText.length - 1)
    val text = lastPsi.getText
    val output = lastPsi.getNode.getElementType match {
      case ScalaTokenTypes.tLINE_COMMENT => text.substring(2).trim
      case ScalaTokenTypes.tBLOCK_COMMENT | ScalaTokenTypes.tDOC_COMMENT =>
        text.substring(2, text.length - 2).trim
      case _ => assertTrue("Test result must be in last comment statement.", false)
    }
    assertEquals(output, res.toString)
  }
}
