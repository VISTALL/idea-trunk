package org.jetbrains.idea.maven.dom;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

public class MavenModuleCompletionAndResolutionTest extends MavenDomWithIndicesTestCase {
  private static final String CREATE_MODULE_INTENTION = MavenDomBundle.message("fix.create.module");
  private static final String CREATE_MODULE_WITH_PARENT_INTENTION = MavenDomBundle.message("fix.create.module.with.parent");

  public void testCompleteFromAllAvailableModules() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>");

    VirtualFile module2Pom = createModulePom("m2",
                                             "<groupId>test</groupId>" +
                                             "<artifactId>m2</artifactId>" +
                                             "<version>1</version>" +
                                             "<packaging>pom</packaging>" +

                                             "<modules>" +
                                             "  <module>m3</module>" +
                                             "</modules>");

    createModulePom("m2/m3",
                    "<groupId>test</groupId>" +
                    "<artifactId>m3</artifactId>" +
                    "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2", "m3");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "  <module><caret></module>" +
                     "</modules>");

    assertCompletionVariants(myProjectPom, "m1", "m2", "m2/m3");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>project</artifactId>" +
                          "<version>1</version>" +
                          "<packaging>pom</packaging>" +

                          "<modules>" +
                          "  <module>m3</module>" +
                          "  <module><caret></module>" +
                          "</modules>");

    assertCompletionVariants(module2Pom, "..", "../m1", "m3");
  }

  public void testDoesNotCompeteIfThereIsNoModules() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module><caret></module>" +
                     "</modules>");

    assertCompletionVariants(myProjectPom);
  }

  public void testIncludesAllThePomsAvailable() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createModulePom("subDir1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>");

    createModulePom("subDir1/subDir2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module><caret></module>" +
                     "</modules>");

    assertCompletionVariants(myProjectPom, "subDir1", "subDir1/subDir2");
  }

  public void testResolution() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m<caret>1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    assertResolved(myProjectPom, findPsiFile(m1), "m1");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m<caret>2</module>" +
                     "</modules>");

    assertResolved(myProjectPom, findPsiFile(m2), "m2");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>unknown<caret>Module</module>" +
                     "</modules>");

    assertUnresolved(myProjectPom, "unknownModule");
  }

  public void testResolutionWithSlashes() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>./m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>./m<caret></module>" +
                     "</modules>");

    assertResolved(myProjectPom, findPsiFile(m), "./m");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>.\\m<caret></module>" +
                     "</modules>");

    assertResolved(myProjectPom, findPsiFile(m), ".\\m");
  }

  public void testResolutionWithProperties() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<properties>" +
                     "  <dirName>subDir</dirName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>${dirName}/m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("subDir/m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<properties>" +
                     "  <dirName>subDir</dirName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module><caret>${dirName}/m</module>" +
                     "</modules>");

    assertResolved(myProjectPom, findPsiFile(m), "subDir/m");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<properties>" +
                     "  <dirName>subDir</dirName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>${<caret>dirName}/m</module>" +
                     "</modules>");

    assertResolved(myProjectPom, findTag(myProjectPom, "project.properties.dirName"));
  }

  public void testCreatePomQuickFix() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>subDir/new<caret>Module</module>" +
                     "</modules>");

    IntentionAction i = getIntentionAtCaret(CREATE_MODULE_INTENTION);
    assertNotNull(i);

    myCodeInsightFixture.launchAction(i);

    assertCreateModuleFixResult(
      "subDir/newModule/pom.xml",
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
      "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
      "    <modelVersion>4.0.0</modelVersion>\n" +
      "\n" +
      "    <groupId>test</groupId>\n" +
      "    <artifactId>newModule</artifactId>\n" +
      "    <version>1</version>\n" +
      "\n" +
      "    \n" +
      "</project>");
  }

  public void testCreatePomQuickFixTakesGroupAndVersionFromSuperParent() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +

                     "<parent>" +
                     "  <groupId>parentGroup</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>parentVersion</version>" +
                     "</parent>" +

                     "<modules>" +
                     "  <module>new<caret>Module</module>" +
                     "</modules>");

    IntentionAction i = getIntentionAtCaret(CREATE_MODULE_INTENTION);
    assertNotNull(i);

    myCodeInsightFixture.launchAction(i);

    assertCreateModuleFixResult(
      "newModule/pom.xml",
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
      "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
      "    <modelVersion>4.0.0</modelVersion>\n" +
      "\n" +
      "    <groupId>parentGroup</groupId>\n" +
      "    <artifactId>newModule</artifactId>\n" +
      "    <version>parentVersion</version>\n" +
      "\n" +
      "    \n" +
      "</project>");
  }

  public void testCreatePomQuickFixWithProperties() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <dirName>subDir</dirName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>${dirName}/new<caret>Module</module>" +
                     "</modules>");

    IntentionAction i = getIntentionAtCaret(CREATE_MODULE_INTENTION);
    assertNotNull(i);

    myCodeInsightFixture.launchAction(i);

    VirtualFile pom = myProjectRoot.findFileByRelativePath("subDir/newModule/pom.xml");
    assertNotNull(pom);
  }

  public void testCreatePomQuickFixTakesDefaultGroupAndVersionIfNothingToOffer() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>new<caret>Module</module>" +
                     "</modules>");

    IntentionAction i = getIntentionAtCaret(CREATE_MODULE_INTENTION);
    assertNotNull(i);
    myCodeInsightFixture.launchAction(i);

    assertCreateModuleFixResult(
      "newModule/pom.xml",
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
      "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
      "    <modelVersion>4.0.0</modelVersion>\n" +
      "\n" +
      "    <groupId>groupId</groupId>\n" +
      "    <artifactId>newModule</artifactId>\n" +
      "    <version>version</version>\n" +
      "\n" +
      "    \n" +
      "</project>");
  }

  public void testCreateModuleWithParentQuickFix() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>new<caret>Module</module>" +
                     "</modules>");

    IntentionAction i = getIntentionAtCaret(CREATE_MODULE_WITH_PARENT_INTENTION);
    assertNotNull(i);
    myCodeInsightFixture.launchAction(i);

    assertCreateModuleFixResult(
      "newModule/pom.xml",
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
      "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
      "    <modelVersion>4.0.0</modelVersion>\n" +
      "\n" +
      "    <parent>\n" +
      "        <groupId>test</groupId>\n" +
      "        <artifactId>project</artifactId>\n" +
      "        <version>1</version>\n" +
      "    </parent>\n" +
      "\n" +
      "    <groupId>test</groupId>\n" +
      "    <artifactId>newModule</artifactId>\n" +
      "    <version>1</version>\n" +
      "\n" +
      "    \n" +
      "</project>");
  }

  public void testDoesNotShowCreatePomQuickFixForEmptyModuleTag() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module><caret></module>" +
                     "</modules>");

    assertNull(getIntentionAtCaret(CREATE_MODULE_INTENTION));
  }

  public void testDoesNotShowCreatePomQuickFixExistingModule() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module</module>" +
                     "</modules>");

    createModulePom("module",
                    "<groupId>test</groupId>" +
                    "<artifactId>module</artifactId>" +
                    "<version>1</version>");
    importProject();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m<caret>odule</module>" +
                     "</modules>");

    assertNull(getIntentionAtCaret(CREATE_MODULE_INTENTION));
  }

  private void assertCreateModuleFixResult(String relativePath, String expectedText) {
    VirtualFile pom = myProjectRoot.findFileByRelativePath(relativePath);
    assertNotNull(pom);

    Document doc = FileDocumentManager.getInstance().getDocument(pom);

    Editor selectedEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
    assertEquals(doc, selectedEditor.getDocument());

    assertEquals(expectedText, doc.getText());
  }
}
