package org.jetbrains.plugins.scala.lang.surroundWith;

import org.jetbrains.plugins.scala.testcases.BaseScalaFileSetTestCase;
import org.jetbrains.plugins.scala.util.TestUtils;
import org.jetbrains.plugins.scala.util.ScalaToolsFactory;
import junit.framework.Test;
import junit.framework.Assert;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.util.IncorrectOperationException;
import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler;
import scala.Tuple4;

/**
 * User: Alexander Podkhalyuzin
 * Date: 08.11.2008
 */
@SuppressWarnings({"ConstantConditions"})
public class SurroundWithTest extends BaseScalaFileSetTestCase{
  private static final String DATA_PATH = "test/org/jetbrains/plugins/scala/lang/surroundWith/data/";


  public SurroundWithTest(String path) {
    super(path);
  }

  public static Test suite() {
    return new SurroundWithTest(DATA_PATH);
  }

  private void doSurround(final Project project, final PsiFile file,
                          Surrounder surrounder, int startSelection, int endSelection) {
    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    try {
      Editor editor = fileEditorManager.openTextEditor(new OpenFileDescriptor(myProject, file.getVirtualFile(), 0), false);
      editor.getSelectionModel().setSelection(startSelection, endSelection);
      SurroundWithHandler.invoke(project, editor, file, surrounder);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      fileEditorManager.closeFile(file.getVirtualFile());
    }
  }

  public String transform(String testName, String[] data) throws Exception {
    Tuple4<String, Integer, Integer, Integer> res = SurroundWithTestUtil.prepareFile(data[0]);
    String fileText = res._1();
    final int startSelection = res._2();
    final int endSelection = res._3();
    final int surroundType = res._4();
    final PsiFile psiFile = TestUtils.createPseudoPhysicalScalaFile(myProject, fileText);

    final Surrounder[] surrounder = ScalaToolsFactory.getInstance().createSurroundDescriptors().getSurroundDescriptors()[0].getSurrounders();
    CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            doSurround(myProject, psiFile, surrounder[surroundType], startSelection, endSelection);
          }
        });
      }
    }, null, null);

    System.out.println("------------------------ " + testName + " ------------------------");
    System.out.println(psiFile.getText());
    System.out.println("");
    return psiFile.getText();
  }
}
