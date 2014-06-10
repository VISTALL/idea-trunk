package jsipptests;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import junit.framework.TestCase;
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle;
import org.intellij.idea.lang.javascript.intention.JSIntentionPowerPack;
import org.intellij.idea.lang.javascript.intention.switchtoif.JSReplaceIfWithSwitchIntention;

import java.util.Collection;
import java.util.List;

public class JSIntentionActionTest extends TestCase {
  protected CodeInsightTestFixture myFixture;

  protected void setUp() throws Exception {
    super.setUp();
    final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
    final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = fixtureFactory.createFixtureBuilder();
    myFixture = fixtureFactory.createCodeInsightFixture(testFixtureBuilder.getFixture());
    myFixture.setTestDataPath(getTestDataPath());
    testFixtureBuilder.addModule(EmptyModuleFixtureBuilder.class).addSourceContentRoot(myFixture.getTempDirPath());
    myFixture.setUp();

    final JSIntentionPowerPack intentionPowerPack = myFixture.getProject().getComponent(JSIntentionPowerPack.class);
    intentionPowerPack.projectOpened();
  }

  protected void tearDown() throws Exception {
    final JSIntentionPowerPack intentionPowerPack = myFixture.getProject().getComponent(JSIntentionPowerPack.class);
    intentionPowerPack.projectClosed();
    myFixture.tearDown();
    myFixture = null;
    super.tearDown();
  }

  public void testSplitIf() throws Throwable {
    doIntentionTest("splitIf/", "js", "", JSIntentionBundle.message("trivialif.split-if-or.display-name"));
  }

  public void testAddBraces() throws Throwable {
    String addBracesDirPrefix = "addBraces/";
    String actionNameForForElement = JSIntentionBundle.message("braces.add-braces.display-name", "for");
    
    doIntentionTest(addBracesDirPrefix, "js2", "", actionNameForForElement);
    doIntentionTest(addBracesDirPrefix, "js", "", JSIntentionBundle.message("braces.add-braces.display-name", "if"));
    doIntentionTest(addBracesDirPrefix, "js", "2", actionNameForForElement);
  }

  public void testReplaceDQWithSQ() throws Throwable {
    doIntentionTest("replaceDQWithSQ/", "js", "", JSIntentionBundle.message("string.double-to-single-quoted-string.display-name", "for"));
  }

  public void testRemoveUnnecessaryParentheses() throws Throwable {
    doIntentionTest("RemoveUnnecessaryParentheses/", "js", "", JSIntentionBundle.message("parenthesis.remove-unnecessary-parentheses.display-name"));
  }
  
  public void testSplitDeclarationAndInitialization() throws Throwable {
    final String dirName = "splitDeclarationAndInitialization/";
    String ext = "js2";
    String suffix = "";

    final String intentionActionName = JSIntentionBundle.message("initialization.split-declaration-and-initialization.display-name");

    doIntentionTest(dirName, ext, suffix, intentionActionName);
    doIntentionTest(dirName, ext, "2", intentionActionName);
    doIntentionTest(dirName, ext, "3", intentionActionName);
    doIntentionTest(dirName, "js", suffix, intentionActionName);
    doIntentionTest(dirName, ext, "4", intentionActionName);
  }

  public void testReplaceSwitchWithIf() throws Throwable {
    doIntentionTest("ReplaceSwitchWithIf/", "js", "", JSIntentionBundle.message("switchtoif.replace-switch-with-if.display-name"));
  }

  private void doIntentionTest(final String dirName, final String ext, final String suffix, final String intentionActionName)
    throws Throwable {
    final Collection<IntentionAction> actions = myFixture.getAvailableIntentions(dirName + "before" + suffix + "." +ext);
    IntentionAction resultAction = null;

    for(IntentionAction action:actions) {
      if (action.getText().equals(intentionActionName)) {
        resultAction = action;
        break;
      }
    }

    boolean actionShouldPresent = true;
    final String doc = myFixture.getDocument(myFixture.getFile()).getCharsSequence().toString();
    String firstLine = doc.substring(0, doc.indexOf('\n'));
    if (firstLine.startsWith("//")) {
      int semiColon = firstLine.indexOf(':') + 1;
      if ("false".equals(firstLine.substring(semiColon).trim())) {
        actionShouldPresent = false;
      }
    }

    if (actionShouldPresent) {
      assertNotNull(resultAction);
      myFixture.launchAction(resultAction);
      myFixture.checkResultByFile(dirName + "after" + suffix + "." + ext);
    } else {
      assertNull(resultAction);
    }
  }

  public void testConstantExpressionsPerformance() throws Throwable {
    System.gc();
    myFixture.getAvailableIntentions("constantExpressions/before.js");

    long start = System.currentTimeMillis();
    final List<IntentionAction> list = myFixture.getAvailableIntentions("constantExpressions/before.js");
    long end = System.currentTimeMillis();
    for (IntentionAction intentionAction : list) {
      if (intentionAction instanceof JSReplaceIfWithSwitchIntention) {
        fail(JSReplaceIfWithSwitchIntention.class.getName() + " inapplicable");
      }
    }
    System.out.println(end - start);
    assertTrue(end - start < 1000);
  }

  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/svnPlugins/JSIntentionPowerPack/testData/";
  }
}