package org.jetbrains.android.dom;

import org.jetbrains.android.AndroidTestCase;
import org.jetbrains.android.resourceManagers.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * @author coyote
 */
abstract class AndroidDomTest extends AndroidTestCase {
  protected final String testFolder;

  protected AndroidDomTest(boolean createManifest, String testFolder) {
    super(createManifest);
    this.testFolder = testFolder;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.copyDirectoryToProject("dom/res", "res");
    myFixture.copyFileToProject("dom/R.java", "gen/p1/p2/R.java");
  }

  protected static String[] withNamespace(String... arr) {
    List<String> list = new ArrayList<String>();
    for (String s : arr) {
      list.add("android:" + s);
    }
    return list.toArray(new String[list.size()]);
  }

  protected void testCompletionVariants(String fileName, String... variants) throws Throwable {
    String path = copyFileToProject(fileName);
    myFixture.testCompletionVariants(path, variants);
  }

  protected static List<String> getAllResources() {
    List<String> list = new ArrayList<String>();
    for (String type : ResourceManager.REFERABLE_RESOURCE_TYPES) {
      list.add('@' + type + '/');
    }
    return list;
  }

  protected void testHighlighting(String file) throws Throwable {
    String path = copyFileToProject(file);
    myFixture.testHighlighting(false, false, false, path);
  }

  protected void testCompletion(String fileBefore, String fileAfter) throws Throwable {
    String path = copyFileToProject(fileBefore);
    myFixture.testCompletion(path, testFolder + '/' + fileAfter);
  }

  protected abstract String getPathToCopy(String testFileName);

  protected String copyFileToProject(String path) throws IOException {
    String pathToCopy = getPathToCopy(path);
    myFixture.copyFileToProject(testFolder + '/' + path, pathToCopy);
    return pathToCopy;
  }
}

