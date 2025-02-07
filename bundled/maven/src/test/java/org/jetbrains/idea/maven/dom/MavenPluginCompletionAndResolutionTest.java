package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.idea.maven.indices.MavenIndicesTestFixture;

import java.io.IOException;
import java.util.List;

public class MavenPluginCompletionAndResolutionTest extends MavenDomWithIndicesTestCase {
  @Override
  protected MavenIndicesTestFixture createIndicesFixture() {
    return new MavenIndicesTestFixture(myDir, myProject, "plugins");
  }

  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");
  }

  public void testGroupIdCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId><caret></groupId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "test", "org.apache.maven.plugins", "org.codehaus.mojo");
  }

  public void testArtifactIdCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId><caret></artifactId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "maven-compiler-plugin", "maven-war-plugin", "maven-eclipse-plugin");
  }

  public void testArtifactWithoutGroupCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId><caret></artifactId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "maven-compiler-plugin", "maven-war-plugin", "build-helper-maven-plugin",
                             "maven-eclipse-plugin");
  }

  public void testResolvingPlugins() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId><caret>maven-compiler-plugin</artifactId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    String pluginPath = "plugins/org/apache/maven/plugins/maven-compiler-plugin/2.0.2/maven-compiler-plugin-2.0.2.pom";
    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath(pluginPath);
    VirtualFile f = LocalFileSystem.getInstance().findFileByPath(filePath);
    assertResolved(myProjectPom, findPsiFile(f));
  }

  public void testResolvingAbsentPlugins() throws Exception {
    removeFromLocalRepository("org/apache/maven/plugins/maven-compiler-plugin");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId><caret>maven-compiler-plugin</artifactId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);
    ref.resolve(); // shouldn't throw;
  }

  public void testDoNotHighlightAbsentGroupIdAndVersion() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");
    checkHighlighting();
  }

  public void testHighlightingAbsentArtifactId() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <<error descr=\"'artifactId' child tag should be defined\">plugin</error>>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testBasicConfigurationCompletion() throws Exception {
    putCaretInConfigurationSection();
    assertCompletionVariantsInclude(myProjectPom, "source", "target");
  }

  public void testIncludingConfigurationParametersFromAllTheMojos() throws Exception {
    putCaretInConfigurationSection();
    assertCompletionVariantsInclude(myProjectPom, "excludes", "testExcludes");
  }

  public void testDoesNotIncludeNonEditableConfigurationParameters() throws Exception {
    putCaretInConfigurationSection();
    assertCompletionVariantsDoNotInclude(myProjectPom, "basedir", "buildDirectory");
  }

  private void putCaretInConfigurationSection() throws IOException {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <<caret>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");
  }

  public void testNoParametersForUnknownPlugin() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>unknown-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <<caret>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);
  }

  public void testNoParametersIfNothingIsSpecified() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <configuration>" +
                     "        <<caret>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);
  }

  public void testResolvingParamaters() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <<caret>includes></includes>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);

    String pluginPath =
      "plugins/org/apache/maven/plugins/maven-compiler-plugin/2.0.2/maven-compiler-plugin-2.0.2.jar!/META-INF/maven/plugin.xml";
    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath(pluginPath);
    VirtualFile f = VirtualFileManager.getInstance().findFileByUrl("jar://" + filePath);

    PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
    assertEquals(findPsiFile(f), resolved.getContainingFile());
    assertTrue(resolved instanceof XmlTag);
    assertEquals("parameter", ((XmlTag)resolved).getName());
    assertEquals("includes", ((XmlTag)resolved).findFirstSubTag("name").getValue().getText());
  }

  public void testResolvingInnerParamatersIntoOuter() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <includes>" +
                     "          <<caret>include></include" +
                     "        </includes>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);

    String pluginPath =
      "plugins/org/apache/maven/plugins/maven-compiler-plugin/2.0.2/maven-compiler-plugin-2.0.2.jar!/META-INF/maven/plugin.xml";
    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath(pluginPath);
    VirtualFile f = VirtualFileManager.getInstance().findFileByUrl("jar://" + filePath);

    PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
    assertEquals(findPsiFile(f), resolved.getContainingFile());
    assertTrue(resolved instanceof XmlTag);
    assertEquals("parameter", ((XmlTag)resolved).getName());
    assertEquals("includes", ((XmlTag)resolved).findFirstSubTag("name").getValue().getText());
  }

  public void testGoalsCompletionAndHighlighting() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal><caret></goal>" +
                     "          </goals>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "compile", "testCompile");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal><error>xxx</error></goal>" +
                     "          </goals>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testGoalsResolution() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal><caret>compile</goal>" +
                     "          </goals>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);

    String pluginPath =
      "plugins/org/apache/maven/plugins/maven-compiler-plugin/2.0.2/maven-compiler-plugin-2.0.2.jar!/META-INF/maven/plugin.xml";
    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath(pluginPath);
    VirtualFile f = VirtualFileManager.getInstance().findFileByUrl("jar://" + filePath);

    PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
    assertEquals(findPsiFile(f), resolved.getContainingFile());
    assertTrue(resolved instanceof XmlTag);
    assertEquals("mojo", ((XmlTag)resolved).getName());
    assertEquals("compile", ((XmlTag)resolved).findFirstSubTag("goal").getValue().getText());
  }

  public void testGoalsCompletionAndResolutionForUnknownPlugin() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>xxx</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal><caret></goal>" +
                     "          </goals>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>xxx</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal><caret>compile</goal>" +
                     "          </goals>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertUnresolved(myProjectPom);
  }

  public void testPhaseCompletionAndHighlighting() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <phase><caret></phase>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariantsInclude(myProjectPom, "clean", "compile", "package");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <phase><error>xxx</error></phase>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testNoExecutionParametersIfGoalIsNotSpecified() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <configuration>" +
                     "            <<caret>" +
                     "          </configuration>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);
  }

  public void testExecutionParametersForSpecificGoal() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal>compile</goal>" +
                     "          </goals>" +
                     "          <configuration>" +
                     "            <<caret>" +
                     "          </configuration>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    List<String> variants = getCompletionVariants(myProjectPom);
    assertTrue(variants.toString(), variants.contains("excludes"));
    assertFalse(variants.toString(), variants.contains("testExcludes"));
  }

  public void testExecutionParametersForSeveralSpecificGoals() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <executions>" +
                     "        <execution>" +
                     "          <goals>" +
                     "            <goal>compile</goal>" +
                     "            <goal>testCompile</goal>" +
                     "          </goals>" +
                     "          <configuration>" +
                     "            <<caret>" +
                     "          </configuration>" +
                     "        </execution>" +
                     "      </executions>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariantsInclude(myProjectPom, "excludes", "testExcludes");
  }

  public void testAliasCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-war-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <<caret>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariantsInclude(myProjectPom, "warSourceExcludes", "excludes");
  }

  public void testListElementsCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <excludes>" +
                     "          <exclude></exclude>" +
                     "          <<caret>" +
                     "        </excludes>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "exclude");
  }

  public void testListElementWhatHasUnpluralizedNameCompletion() throws Exception {
    // NPE test - StringUtil.unpluralize returns null.

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-eclipse-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <additionalConfig>" +
                     "          <<caret>" +
                     "        </additionalConfig>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "additionalConfig", "config");
  }

  public void testDoNotHighlightUnknownElementsUnderLists() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <excludes>" +
                     "          <foo>foo</foo>" +
                     "        </excludes>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testArrayElementsCompletion() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-war-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <webResources>" +
                     "          <webResource></webResource>" +
                     "          <<caret>" +
                     "        </webResources>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "resource", "webResource");
  }

  public void testCompletionInCustomObjects() throws Exception {
    if (ignore()) return;

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-war-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <webResources>" +
                     "          <webResource>" +
                     "            <<caret>" +
                     "          </webResource>" +
                     "        </webResources>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);
  }

  public void testDocumentationForParameter() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <s<caret>ource></source>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertDocumentation("The -source argument for the Java compiler.");
  }

  public void testDoNotCompleteNorHighlightNonPluginConfiguration() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<ciManagement>" +
                     "  <system>foo</system>" +
                     "  <notifiers>" +
                     "    <notifier>" +
                     "      <type>mail</type>" +
                     "      <configuration>" +
                     "        <address>foo@bar.com</address>" +
                     "      </configuration>" +
                     "    </notifier>" +
                     "  </notifiers>" +
                     "</ciManagement>");

    checkHighlighting();
  }

  public void testDoNotHighlighInnerParameters() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <source>" +
                     "          <foo>*.java</foo>" +
                     "        </source>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testDoNotHighlighInnerParameterAttributes() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <includes value1='1'>" +
                     "          <include value2='2'/>" +
                     "        </includes>" +
                     "        <source value3='3'>" +
                     "          <child value4='4'/>" +
                     "        </source>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testDoNotCompleteHighlighParameterAttributes() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "        <source <caret>/>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    assertCompletionVariants(myProjectPom);
  }

  public void testWorksWithPropertiesInPluginId() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <plugin.groupId>org.apache.maven.plugins</plugin.groupId>" +
                     "  <plugin.artifactId>maven-compiler-plugin</plugin.artifactId>" +
                     "  <plugin.version>2.0.2</plugin.version>" +
                     "</properties>");
    importProject(); // let us recognize the properties first

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <plugin.groupId>org.apache.maven.plugins</plugin.groupId>" +
                     "  <plugin.artifactId>maven-compiler-plugin</plugin.artifactId>" +
                     "  <plugin.version>2.0.2</plugin.version>" +
                     "</properties>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>${plugin.groupId}</groupId>" +
                     "      <artifactId>${plugin.artifactId}</artifactId>" +
                     "      <version>${plugin.version}</version>" +
                     "      <configuration>" +
                     "        <source></source>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }

  public void testDoNotHighlightPropertiesForUnknownPlugins() throws Throwable {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <artifactId><error>foo.bar</error></artifactId>" +
                     "      <configuration>" +
                     "        <prop>" +
                     "          <value>foo</value>" +
                     "        </prop>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    checkHighlighting();
  }
}
