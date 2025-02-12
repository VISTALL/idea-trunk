package org.jetbrains.idea.maven.dom;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;

public class MavenParentCompletionAndResolutionTest extends MavenDomWithIndicesTestCase {
  public void testVariants() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId><caret></groupId>" +
                     "  <artifactId>junit</artifactId>" +
                     "  <version><caret></version>" +
                     "</parent>");
    assertCompletionVariants(myProjectPom, "junit", "jmock", "test");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>junit</groupId>" +
                     "  <artifactId><caret></artifactId>" +
                     "</parent>");
    assertCompletionVariants(myProjectPom, "junit");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>junit</groupId>" +
                     "  <artifactId>junit</artifactId>" +
                     "  <version><caret></version>" +
                     "</parent>");
    assertCompletionVariants(myProjectPom, "3.8.1", "3.8.2", "4.0");
  }

  public void testResolutionInsideTheProject() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    importProjects(myProjectPom, m);

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m</artifactId>" +
                         "<version>1</version>" +

                         "<parent>" +
                         "  <groupId><caret>test</groupId>" +
                         "  <artifactId>project</artifactId>" +
                         "  <version>1</version>" +
                         "</parent>");

    assertResolved(m, findPsiFile(myProjectPom));
  }

  public void testResolutionOutsideOfTheProject() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId><caret>junit</groupId>" +
                     "  <artifactId>junit</artifactId>" +
                     "  <version>4.0</version>" +
                     "</parent>");

    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath("local1/junit/junit/4.0/junit-4.0.pom");
    VirtualFile f = LocalFileSystem.getInstance().findFileByPath(filePath);
    assertResolved(myProjectPom, findPsiFile(f));
  }

  public void testResolvingByRelativePath() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId><caret>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>parent/pom.xml</relativePath>" +
                     "</parent>");

    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>");

    assertResolved(myProjectPom, findPsiFile(parent));
  }

  public void testResolvingByRelativePathWithProperties() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <parentPath>parent/pom.xml</parentPath>" +
                     "</properties>" +

                     "<parent>" +
                     "  <groupId><caret>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>${parentPath}</relativePath>" +
                     "</parent>");

    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>");

    assertResolved(myProjectPom, findPsiFile(parent));
  }

  public void testResolvingByRelativePathWhenOutsideOfTheProject() throws Throwable {
    VirtualFile parent = createPomFile(myProjectRoot.getParent(),
                                       "<groupId>test</groupId>" +
                                       "<artifactId>project</artifactId>" +
                                       "<version>1</version>");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId><caret>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>../pom.xml</relativePath>" +
                     "</parent>");

    assertResolved(myProjectPom, findPsiFile(parent));
  }

  public void testResolvingByRelativePathWithoutPomXmlAtTheEnd() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath><caret>parent</relativePath>" +
                     "</parent>");

    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>");

    assertResolved(myProjectPom, findPsiFile(parent));
  }

  public void testDoNotHighlightResolvedParentByRelativePathWhenOutsideOfTheProject() throws Throwable {
    createPomFile(myProjectRoot.getParent(),
                  "<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>../pom.xml</relativePath>" +
                     "</parent>");
    checkHighlighting(myProjectPom);
  }

  public void testRelativePathCompletion() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath><caret></relativePath>" +
                     "</parent>");

    createModulePom("dir/one",
                    "<groupId>test</groupId>" +
                    "<artifactId>one</artifactId>" +
                    "<version>1</version>");

    createModulePom("two",
                    "<groupId>test</groupId>" +
                    "<artifactId>two</artifactId>" +
                    "<version>1</version>");

    assertCompletionVariants(myProjectPom, "dir/one/pom.xml", "two/pom.xml");
  }

  public void testHighlightingUnknownValues() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<parent>" +
                  "  <groupId><error>xxx</error></groupId>" +
                  "  <artifactId><error>xxx</error></artifactId>" +
                  "  <version><error>xxx</error></version>" +
                  "</parent>");

    checkHighlighting();
  }

  public void testHighlightingAbsentGroupId() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<<error descr=\"'groupId' child tag should be defined\">parent</error>>" +
                  "  <artifactId><error>junit</error></artifactId>" +
                  "  <version><error>4.0</error></version>" +
                  "</parent>");
    checkHighlighting();
  }

  public void testHighlightingAbsentArtifactId() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<<error descr=\"'artifactId' child tag should be defined\">parent</error>>" +
                  "  <groupId>junit</groupId>" +
                  "  <version><error>4.0</error></version>" +
                  "</parent>");
    checkHighlighting();
  }

  public void testHighlightingAbsentVersion() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<<error descr=\"'version' child tag should be defined\">parent</error>>" +
                  "  <groupId>junit</groupId>" +
                  "  <artifactId>junit</artifactId>" +
                  "</parent>");
    checkHighlighting();
  }

  public void testHighlightingInvalidRelativePath() throws Throwable {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<parent>" +
                  "  <groupId>junit</groupId>" +
                  "  <artifactId>junit</artifactId>" +
                  "  <version>4.0</version>" +
                  "  <relativePath><error>parent/pom.xml</error></relativePath>" +
                  "</parent>");

    checkHighlighting();
  }

  public void testPathQuickFixForInvalidValue() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>");

    VirtualFile m = createModulePom("bar",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>one</artifactId>" +
                                    "<version>1</version>");

    importProjects(myProjectPom, m);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>one</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath><caret>xxx</relativePath>" +
                     "</parent>");

    IntentionAction i = getIntentionAtCaret("Fix relative path");
    assertNotNull(i);

    myCodeInsightFixture.launchAction(i);
    PsiElement el = getElementAtCaret(myProjectPom);

    assertEquals("bar/pom.xml", ElementManipulators.getValueText(el));
  }

  public void testDoNotShowPathQuickFixForValidPath() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>");

    VirtualFile m = createModulePom("bar",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>one</artifactId>" +
                                    "<version>1</version>");

    importProjects(myProjectPom, m);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>one</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath><caret>bar/pom.xml</relativePath>" +
                     "</parent>");

    assertNull(getIntentionAtCaret("Fix relative path"));
  }
}
