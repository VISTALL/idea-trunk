package org.jetbrains.idea.maven;

import com.intellij.compiler.impl.javaCompiler.javac.JavacSettings;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.idea.maven.project.MavenProject;

import java.io.File;
import java.util.List;

public class StructureImportingTest extends MavenImportingTestCase {
  public void testUsingRelativePathForTheProject() throws Exception {
    assertFalse(((ProjectEx)myProject).isSavePathsRelative());

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertTrue(((ProjectEx)myProject).isSavePathsRelative());
  }

  public void testUsingRelativePathForModules() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertTrue(getModule("project").isSavePathsRelative());
  }

  public void testInheritProjectJdkForModules() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertTrue(ModuleRootManager.getInstance(getModule("project")).isSdkInherited());
  }

  public void testDoNotResetSomeSettingsAfterReimport() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    getModule("project").setSavePathsRelative(false);
    Sdk sdk = setupJdkForModule("project");

    importProject();
    assertFalse(getModule("project").isSavePathsRelative());
    assertFalse(ModuleRootManager.getInstance(getModule("project")).isSdkInherited());
    assertEquals(sdk, ModuleRootManager.getInstance(getModule("project")).getSdk());
  }

  public void testModulesWithSlashesRegularAndBack() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir\\m1</module>" +
                     "  <module>dir/m2</module>" +
                     "</modules>");

    createModulePom("dir/m1", "<groupId>test</groupId>" +
                              "<artifactId>m1</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir/m2", "<groupId>test</groupId>" +
                              "<artifactId>m2</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2");

    List<MavenProject> roots = myProjectsTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = myProjectsTree.getModules(roots.get(0));
    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
    assertEquals("m2", modules.get(1).getMavenId().getArtifactId());
  }

  public void testModulesAreNamedAfterArtifactIds() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +
                     "<name>name</name>" +

                     "<modules>" +
                     "  <module>dir1</module>" +
                     "  <module>dir2</module>" +
                     "</modules>");

    createModulePom("dir1", "<groupId>test</groupId>" +
                            "<artifactId>m1</artifactId>" +
                            "<version>1</version>" +
                            "<name>name1</name>");

    createModulePom("dir2", "<groupId>test</groupId>" +
                            "<artifactId>m2</artifactId>" +
                            "<version>1</version>" +
                            "<name>name2</name>");
    importProject();
    assertModules("project", "m1", "m2");
  }

  public void testModulesWithSlashesAtTheEnds() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1/</module>" +
                     "  <module>m2\\</module>" +
                     "  <module>m3//</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");

    createModulePom("m3", "<groupId>test</groupId>" +
                          "<artifactId>m3</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2", "m3");
  }

  public void testModulesWithSameArtifactId() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir1/m</module>" +
                     "  <module>dir2/m</module>" +
                     "</modules>");

    createModulePom("dir1/m", "<groupId>test.group1</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir2/m", "<groupId>test.group2</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m (1) (test.group1)", "m (2) (test.group2)");
  }
  
  public void testModulesWithSameArtifactIdAndGroup() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir1/m</module>" +
                     "  <module>dir2/m</module>" +
                     "</modules>");

    createModulePom("dir1/m", "<groupId>test</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir2/m", "<groupId>test</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m (1)", "m (2)");
  }

  public void testModuleWithRelativePath() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>../m</module>" +
                     "</modules>");

    createModulePom("../m", "<groupId>test</groupId>" +
                            "<artifactId>m</artifactId>" +
                            "<version>1</version>");

    importProject();
    assertModules("project", "m");
  }

  public void testModuleWithRelativeParent() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>../parent</relativePath>" +
                     "</parent>");

    createModulePom("../parent", "<groupId>test</groupId>" +
                                 "<artifactId>parent</artifactId>" +
                                 "<version>1</version>");

    importProject();
    assertModules("project");
  }

  public void testModulePathsAsProperties() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <module1>m1</module1>" +
                     "  <module2>m2</module2>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>${module1}</module>" +
                     "  <module>${module2}</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2");

    List<MavenProject> roots = myProjectsTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = myProjectsTree.getModules(roots.get(0));
    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
    assertEquals("m2", modules.get(1).getMavenId().getArtifactId());
  }

  public void testRecursiveParent() throws Exception {
    importProject("<parent>" +
                  "  <groupId>org.apache.maven.archetype.test</groupId>" +
                  "  <artifactId>test-create-2</artifactId>" +
                  "  <version>1.0-SNAPSHOT</version>" +
                  "</parent>" +

                  "<artifactId>test-create-2</artifactId>" +
                  "<name>Maven archetype Test create-2-subModule</name>" +
                  "<packaging>pom</packaging>");
  }

  public void testParentWithoutARelativePath() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <moduleName>m1</moduleName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>modules/m</module>" +
                     "</modules>");

    createModulePom("modules/m", "<groupId>test</groupId>" +
                                 "<artifactId>${moduleName}</artifactId>" +
                                 "<version>1</version>" +

                                 "<parent>" +
                                 "  <groupId>test</groupId>" +
                                 "  <artifactId>project</artifactId>" +
                                 "  <version>1</version>" +
                                 "</parent>");

    importProject();
    assertModules("project", "m1");

    List<MavenProject> roots = myProjectsTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = myProjectsTree.getModules(roots.get(0));
    assertEquals(1, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
  }

  public void testModuleWithPropertiesWithParentWithoutARelativePath() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <moduleName>m1</moduleName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>modules/m</module>" +
                     "</modules>");

    createModulePom("modules/m", "<groupId>test</groupId>" +
                                 "<artifactId>${moduleName}</artifactId>" +
                                 "<version>1</version>" +

                                 "<parent>" +
                                 "  <groupId>test</groupId>" +
                                 "  <artifactId>project</artifactId>" +
                                 "  <version>1</version>" +
                                 "</parent>");

    importProject();
    assertModules("project", "m1");

    List<MavenProject> roots = myProjectsTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = myProjectsTree.getModules(roots.get(0));
    assertEquals(1, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
  }

  public void testParentInLocalRepository() throws Exception {
    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>" +
                                         "<packaging>pom</packaging>" +

                                         "<dependencies>" +
                                         "  <dependency>" +
                                         "    <groupId>junit</groupId>" +
                                         "    <artifactId>junit</artifactId>" +
                                         "    <version>4.0</version>" +
                                         "  </dependency>" +
                                         "</dependencies>");
    executeGoal("parent", "install");
    parent.delete(null);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>m</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "</parent>");

    importProject();
    assertModules("m");
    assertModuleLibDeps("m", "Maven: junit:junit:4.0");
  }

  public void testParentInRemoteRepository() throws Exception {
    String pathToJUnit = "asm/asm-parent/3.0";
    File parentDir = new File(getRepositoryPath(), pathToJUnit);

    removeFromLocalRepository(pathToJUnit);
    assertFalse(parentDir.exists());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>asm</groupId>" +
                     "  <artifactId>asm-parent</artifactId>" +
                     "  <version>3.0</version>" +
                     "</parent>");

    importProject();
    assertModules("project");

    assertTrue(parentDir.exists());

    assertEquals("asm-parent", myProjectsTree.getRootProjects().get(0).getParentId().getArtifactId());
    assertTrue(new File(parentDir, "asm-parent-3.0.pom").exists());
  }

  public void testCreatingModuleGroups() throws Exception {
    VirtualFile p1 = createModulePom("project1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m1</module>" +
                                     "</modules>");

    createModulePom("project1/m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>");

    VirtualFile p2 = createModulePom("project2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project2</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m2</module>" +
                                     "</modules>");

    createModulePom("project2/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>m3</module>" +
                    "</modules>");

    createModulePom("project2/m2/m3",
                    "<groupId>test</groupId>" +
                    "<artifactId>m3</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProjects(p1, p2);
    assertModules("project1", "project2", "m1", "m2", "m3");

    assertModuleGroupPath("project1", "project1 and modules");
    assertModuleGroupPath("m1", "project1 and modules");
    assertModuleGroupPath("project2", "project2 and modules");
    assertModuleGroupPath("m2", "project2 and modules", "m2 and modules");
    assertModuleGroupPath("m3", "project2 and modules", "m2 and modules");
  }

  public void testDoesNotCreateUnnecessaryTopLevelModuleGroup() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    createModulePom("m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>m2</module>" +
                    "</modules>");

    createModulePom("m1/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProject();
    assertModules("project", "m1", "m2");

    assertModuleGroupPath("project");
    assertModuleGroupPath("m1", "m1 and modules");
    assertModuleGroupPath("m2", "m1 and modules");
  }

  public void testModuleGroupsWhenNotCreatingModulesForAggregatorProjects() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module1</module>" +
                     "</modules>");

    createModulePom("module1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>module2</module>" +
                    "</modules>");

    createModulePom("module1/module2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module2</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    getMavenImporterSettings().setCreateModulesForAggregators(false);
    importProject();
    assertModules("module2");

    assertModuleGroupPath("module2", "module1 and modules");
  }

  public void testReimportingProjectWhenCreatingModuleGroupsSettingChanged() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module1</module>" +
                     "</modules>");

    createModulePom("module1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>module2</module>" +
                    "</modules>");

    createModulePom("module1/module2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module2</artifactId>" +
                    "<version>1</version>");
    importProject();

    assertModuleGroupPath("module2");

    getMavenImporterSettings().setCreateModuleGroups(true);
    myProjectsManager.performScheduledImport();
    assertModuleGroupPath("module2", "module1 and modules");
  }

  public void testModuleGroupsWhenProjectWithDuplicateNameEmerges() throws Exception {
    VirtualFile p1 = createModulePom("project1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m1</module>" +
                                     "</modules>");

    createModulePom("project1/m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module</artifactId>" +
                    "<version>1</version>");

    VirtualFile p2 = createModulePom("project2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project2</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>");

    //createModulePom("m2",
    //                "<groupId>test</groupId>" +
    //                "<artifactId>m2</artifactId>" +
    //                "<version>1</version>" +
    //                "<packaging>pom</packaging>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProjects(p1, p2);
    assertModules("project1", "project2", "module");

    assertModuleGroupPath("project1", "project1 and modules");
    assertModuleGroupPath("module", "project1 and modules");

    p2 = createModulePom("project2",
                         "<groupId>test</groupId>" +
                         "<artifactId>project2</artifactId>" +
                         "<version>1</version>" +
                         "<packaging>pom</packaging>" +

                         "<modules>" +
                         "  <module>m2</module>" +
                         "</modules>");

    createModulePom("project2/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module</artifactId>" +
                    "<version>1</version>");

    updateProjectsAndImport(p2); // should not fail to map module names. 

    assertModules("project1", "project2", "module", "module (2)");

    assertModuleGroupPath("project1", "project1 and modules");
    assertModuleGroupPath("module", "project1 and modules");
    assertModuleGroupPath("project2", "project2 and modules");
    assertModuleGroupPath("module (2)", "project2 and modules");
  }

  public void testLanguageLevel() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>1.4</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_4, getLanguageLevelForProject());
  }

  public void testLanguageLevel6() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>1.6</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_6, getLanguageLevelForProject());
  }

  public void testLanguageLevelWhenCompilerPluginIsNotSpecified() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertModules("project");
    assertNull(getLanguageLevelForProject());
  }

  public void testLanguageLevelWhenConfigurationIsNotSpecified() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertNull(getLanguageLevelForProject());
  }

  public void testLanguageLevelWhenSourseLanguageLevelIsNotSpecified() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertNull(getLanguageLevelForProject());
  }

  private LanguageLevel getLanguageLevelForProject() {
    return LanguageLevelModuleExtension.getInstance(getModule("project")).getLanguageLevel();
  }

  public void testSettingTargetLevel() throws Exception {
    JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING = "-Xmm500m -Xms128m";

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <target>1.3</target>" +
                  "        </configuration>" +
                  "     </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals("-Xmm500m -Xms128m -target 1.3", JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING);
  }

  public void testRewritingTargetLevel() throws Exception {
    JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING = "-Xmm500m -target 1.5 -Xms128m";

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <target>1.3</target>" +
                  "        </configuration>" +
                  "     </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals("-Xmm500m -target 1.3 -Xms128m", JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING);
  }

  public void testSettingTargetLevelAtMinimalSpecified() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>" +

                          "<build>" +
                          "  <plugins>" +
                          "    <plugin>" +
                          "      <artifactId>maven-compiler-plugin</artifactId>" +
                          "        <configuration>" +
                          "          <target>1.3</target>" +
                          "        </configuration>" +
                          "     </plugin>" +
                          "  </plugins>" +
                          "</build>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>" +

                          "<build>" +
                          "  <plugins>" +
                          "    <plugin>" +
                          "      <artifactId>maven-compiler-plugin</artifactId>" +
                          "        <configuration>" +
                          "          <target>1.5</target>" +
                          "        </configuration>" +
                          "     </plugin>" +
                          "  </plugins>" +
                          "</build>");

    importProject();

    assertEquals("-target 1.3", JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING);
  }

  public void testSettingTargetLevelAtMinimalSpecifiedButNoLessThanMaximumSourceLevel() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>" +

                          "<build>" +
                          "  <plugins>" +
                          "    <plugin>" +
                          "      <artifactId>maven-compiler-plugin</artifactId>" +
                          "        <configuration>" +
                          "          <target>1.3</target>" +
                          "          <source>1.3</source>" +
                          "        </configuration>" +
                          "     </plugin>" +
                          "  </plugins>" +
                          "</build>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>" +

                          "<build>" +
                          "  <plugins>" +
                          "    <plugin>" +
                          "      <artifactId>maven-compiler-plugin</artifactId>" +
                          "        <configuration>" +
                          "          <target>1.5</target>" +
                          "          <source>1.5</source>" +
                          "        </configuration>" +
                          "     </plugin>" +
                          "  </plugins>" +
                          "</build>");

    importProject();

    assertEquals("-target 1.5", JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING);
  }

  public void testRewritingIncorrectTargetLevel() throws Exception {
    JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING = "-Xmm500m -target ${undefined.variable} -Xms128m";

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <target>1.3</target>" +
                  "        </configuration>" +
                  "     </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals("-Xmm500m -target 1.3 -Xms128m", JavacSettings.getInstance(myProject).ADDITIONAL_OPTIONS_STRING);
  }

  public void testProjectWithBuiltExtension() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  " <extensions>" +
                  "   <extension>" +
                  "     <groupId>org.apache.maven.wagon</groupId>" +
                  "     <artifactId>wagon-webdav</artifactId>" +
                  "     <version>1.0-beta-2</version>" +
                  "    </extension>" +
                  "  </extensions>" +
                  "</build>");
    assertModules("project");
  }

  public void testProjectWithInvalidBuildExtension() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  " <extensions>" +
                  "   <extension>" +
                  "     <groupId>xxx</groupId>" +
                  "     <artifactId>yyy</artifactId>" +
                  "     <version>1</version>" +
                  "    </extension>" +
                  "  </extensions>" +
                  "</build>");
    assertModules("project"); // shouldn't throw any exception
  }

  public void testUsingPropertyInBuildExtensionsOfChildModule() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<properties>" +
                     "  <xxx>1.0-beta-2</xxx>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m</artifactId>" +

                         "<parent>" +
                         "  <groupId>test</groupId>" +
                         "  <artifactId>project</artifactId>" +
                         "  <version>1</version>" +
                         "</parent>" +

                         "<build>" +
                         "  <extensions>" +
                         "    <extension>" +
                         "      <groupId>org.apache.maven.wagon</groupId>" +
                         "      <artifactId>wagon-webdav</artifactId>" +
                         "      <version>${xxx}</version>" +
                         "    </extension>" +
                         "  </extensions>" +
                         "</build>");

    importProject();
    assertModules("project", "m");
  }

  public void testProjectWithProfilesXmlFile() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<dependencies>" +
                     "  <dependency>" +
                     "    <groupId>junit</groupId>" +
                     "    <artifactId>junit</artifactId>" +
                     "    <version>${junit.version}</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>false</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <junit.version>4.0</junit.version>" +
                      "  </properties>" +
                      "</profile>" +

                      "<profile>" +
                      "  <id>two</id>" +
                      "  <activation>" +
                      "    <activeByDefault>false</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <junit.version>3.8.1</junit.version>" +
                      "  </properties>" +
                      "</profile>");

    importProjectWithProfiles("one");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:4.0");

    importProjectWithProfiles("two");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:3.8.1");
  }

  public void testProjectWithOldProfilesXmlFile() throws Exception {
    ignore(); // not supported by 2.2
  }

  public void testProjectWithProfilesXmlWithNewRootTagFile() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<dependencies>" +
                     "  <dependency>" +
                     "    <groupId>junit</groupId>" +
                     "    <artifactId>junit</artifactId>" +
                     "    <version>${junit.version}</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>false</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <junit.version>4.0</junit.version>" +
                      "  </properties>" +
                      "</profile>" +

                      "<profile>" +
                      "  <id>two</id>" +
                      "  <activation>" +
                      "    <activeByDefault>false</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <junit.version>3.8.1</junit.version>" +
                      "  </properties>" +
                      "</profile>");

    importProjectWithProfiles("one");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:4.0");

    importProjectWithProfiles("two");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:3.8.1");
  }

  public void testProjectWithDefaultProfileInProfilesXmlFile() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<dependencies>" +
                     "  <dependency>" +
                     "    <groupId>junit</groupId>" +
                     "    <artifactId>junit</artifactId>" +
                     "    <version>${junit.version}</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <junit.version>4.0</junit.version>" +
                      "  </properties>" +
                      "</profile>");

    importProject();
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:4.0");
  }

  public void testRefreshFSAfterImport() throws Exception {
    myProjectRoot.getChildren(); // make sure fs is cached
    new File(myProjectRoot.getPath(), "foo").mkdirs();

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertNotNull(myProjectRoot.findChild("foo"));
  }
}
