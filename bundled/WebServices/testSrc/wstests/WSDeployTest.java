package wstests;

import com.advancedtools.webservices.utils.DeployUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.StringBufferInputStream;

/**
 * @by maxim
 */
public class WSDeployTest extends BaseWSTestCase {
  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
  }

  protected void configureInspections() {
  }

  protected String getTestDataPath() {
    return "deployment";
  }

  public void testCreateFileInWebDirectory() throws Throwable {
    final String name = getTestName();
    doHighlightingTest(name + ".txt");
    final VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

    Module module = ProjectRootManager.getInstance(myFixture.getProject()).getFileIndex().getModuleForFile(virtualFile);
    @NonNls String[] pathComponents = {"zzz", "A.txt"};
    VirtualFile path = DeployUtils.findFileByPath(module, pathComponents, true);
    assertNull(path);

    final String text = myFixture.getFile().getText();
    DeployUtils.addFileToModuleFromTemplate(module, pathComponents, new StringBufferInputStream(text), true, true);
    path = DeployUtils.findFileByPath(module, pathComponents, true);
    assertNotNull(path);

    assertEquals(virtualFile.getParent().getPath(), path.getParent().getPath());
    char[] chars = FileUtil.loadFileText(new File(path.getPath()));

    assertEquals(text, new String(chars));

  }
}
