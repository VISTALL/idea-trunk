package com.intellij.eclipse.export.exporter;

import com.intellij.eclipse.export.model.IdeaProject;
import com.intellij.eclipse.export.model.PluginTestCase;
import com.intellij.eclipse.export.model.ProjectLibrary;
import com.intellij.eclipse.export.model.Resource;
import com.intellij.eclipse.export.model.stubs.TestIdeaModule;
import org.eclipse.core.runtime.Path;
import static org.eclipse.jdt.core.IClasspathContainer.*;
import static org.eclipse.jdt.core.IClasspathEntry.*;
import org.eclipse.jdt.core.JavaCore;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExporterTest extends PluginTestCase {
  private File tempDir;
  private Document xmlDoc;

  @Before
  public void setUp() {
    super.setUp();

    String root = ClassLoader.getSystemResource(".").getPath();
    tempDir = new File(root, "temp");
    tempDir.mkdirs();

    xmlDoc = new Document();
    Element xmlRoot = new Element("root");
    xmlDoc.setRootElement(xmlRoot);
  }

  @After
  public void tearDown() {
    deleteDirectory(tempDir);
  }

  @Test
  public void testProjectPaths() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project");
    ideaProject.setName("exportedProjectName");

    File outputDir = new File(tempDir, "output");
    File projectDir = new File(tempDir, "output/project");
    Exporter exporter = new Exporter(ideaProject, outputDir.toString(), false, false, false);

    exporter.prepareProjectDirectory();
    assertTrue(projectDir.exists());

    assertEquals(new File(tempDir, "output/project/exportedProjectName.ipr"),
                 exporter.getProjectFile());
  }

  @Test
  public void testModulePaths() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectName("name");
    setEclipseProjectLocation("c:/workspace/folder");

    File outputDir = new File(tempDir, "output");
    File moduleDir = new File(tempDir, "output/folder/");
    Exporter exporter = new Exporter(ideaProject, outputDir.toString(), false, false, false);

    exporter.prepareModuleDirectory(ideaModule);

    assertTrue(moduleDir.exists());

    assertEquals(new File(tempDir, "output/folder/name.iml"),
                 exporter.getModuleFile(ideaModule));
  }

  @Test
  public void testWritingProjectManagerComponent() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectName("name");
    setEclipseProjectLocation("c:/workspace/folder");

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createProjectModuleManagerComponent(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"ProjectModuleManager\">\r\n" +
                    "    <modules>\r\n" +
                    "      <module fileurl=\"file://$PROJECT_DIR$/../folder/name.iml\" " +
                    "filepath=\"file://$PROJECT_DIR$/../folder/name.iml\" />\r\n" +
                    "    </modules>\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingProjectLibrariesTable() {
    IdeaProject project = new IdeaProject(null) {
      public List<ProjectLibrary> getProjectLibraries() {
        ProjectLibrary lib1 = new ProjectLibrary("lib1");
        ProjectLibrary lib2 = new ProjectLibrary("lib2");

        lib1.addResources(asResources("c:/folder/lib1"));
        lib2.addResources(asResources("c:/folder/lib2.jar"));

        List<ProjectLibrary> result = new ArrayList<ProjectLibrary>();
        result.add(lib1);
        result.add(lib2);
        return result;
      }
    };

    Exporter exporter = new Exporter(project, null, false, false, true);
    exporter.createProjectLibraryTable(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"libraryTable\">\r\n" +
                    "    <library name=\"lib1\">\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"file://c:/folder/lib1\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "    <library name=\"lib2\">\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/folder/lib2.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingProjectLibrariesTableWithPathVariables() {
    setEclipseInstallationDirectory("c:/eclipse/");
    setEclipseWorkspaceResolverdVariablePath("VAR_NAME/lib1", "c:/folder/lib1");

    IdeaProject project = new IdeaProject(null) {
      public List<ProjectLibrary> getProjectLibraries() {
        ProjectLibrary lib = new ProjectLibrary("lib1");

        lib.addResources(new Resource[]{new Resource(new Path("VAR_NAME/lib1"), true)});
        lib.addResources(new Resource[]{new Resource(new Path("c:/eclipse/lib2"), false)});

        return Collections.singletonList(lib);
      }
    };

    Exporter exporter = new Exporter(project, null, false, true, true);
    exporter.createProjectLibraryTable(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"libraryTable\">\r\n" +
                    "    <library name=\"lib1\">\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"file://$VAR_NAME$/lib1\" />\r\n" +
                    "        <root url=\"file://$ECLIPSE_HOME$/lib2\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testDoesNotWriteProjectLibrariesWhenShouldNotDeclare() {
    IdeaProject project = new IdeaProject(null) {
      public List<ProjectLibrary> getProjectLibraries() {
        ProjectLibrary lib = new ProjectLibrary("lib");
        lib.addResources(asResources("c:/folder/lib"));
        return Collections.singletonList(lib);
      }
    };

    Exporter exporter = new Exporter(project, null, false, false, false);
    exporter.createProjectLibraryTable(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"libraryTable\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingPathVariables() {
    IdeaProject project = new IdeaProject(null) {
      @Override
      public List<String> getPathVariables() {
        return Arrays.asList(new String[]{"VAR_1", "VAR_2"});
      }
    };

    Exporter exporter = new Exporter(project, null, false, true, false);
    exporter.createPathVariablesTable(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <UsedPathMacros>\r\n" +
                    "    <macro name=\"VAR_1\" />\r\n" +
                    "    <macro name=\"VAR_2\" />\r\n" +
                    "  </UsedPathMacros>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testDoesNotWritePathVariables() {
    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createPathVariablesTable(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root />\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingProjectJdkAndLanguageLevel() {
    setEclipseWorkspaceVMAndCompilerLevel(createVMInstall("jdk.name"), JavaCore.VERSION_1_4);

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createProjectRootManager(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"ProjectRootManager\" version=\"2\" " +
                    "assert-keyword=\"true\" jdk-15=\"false\" " +
                    "project-jdk-name=\"jdk.name\" project-jdk-type=\"JavaSDK\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingAbsendJdkAndLanguageLevel() {
    setEclipseWorkspaceVMAndCompilerLevel(null, null);

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createProjectRootManager(xmlDoc.getRootElement());

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"ProjectRootManager\" version=\"2\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingModuleWithoutContentCopying() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectName("name");
    setEclipseProjectLocation("c:/workspace/folder");
    setEclipseProjectReferencedProjects();

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createNewModuleRootManager(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"NewModuleRootManager\">\r\n" +
                    "    <exclude-output />\r\n" +
                    "    <output url=\"file://$MODULE_DIR$/classes\" />\r\n" +
                    "    <content url=\"file://c:/workspace/folder\" />\r\n" +
                    "    <orderEntry type=\"inheritedJdk\" />\r\n" +
                    "    <orderEntry type=\"sourceFolder\" isTestSource=\"false\" />\r\n" +
                    "    <orderEntryProperties />\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingModuleWithContentCopying() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectName("name");
    setEclipseProjectLocation("c:/workspace/folder");
    setEclipseProjectReferencedProjects();

    Exporter exporter = new Exporter(ideaProject, null, true, false, false);
    exporter.createNewModuleRootManager(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"NewModuleRootManager\">\r\n" +
                    "    <exclude-output />\r\n" +
                    "    <output url=\"file://$MODULE_DIR$/classes\" />\r\n" +
                    "    <content url=\"file://$MODULE_DIR$/\" />\r\n" +
                    "    <orderEntry type=\"inheritedJdk\" />\r\n" +
                    "    <orderEntry type=\"sourceFolder\" isTestSource=\"false\" />\r\n" +
                    "    <orderEntryProperties />\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingModuleJdkAndLanguageLevel() {
    setEclipseProjectLocation("c:/workspace/folder");
    setEclipseProjectVMAndCompilerLevel(createVMInstall("jdk.name"), JavaCore.VERSION_1_4);
    setEclipseProjectReferencedProjects();

    Exporter exporter = new Exporter(ideaProject, null, true, false, false);
    exporter.createNewModuleRootManager(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <component name=\"NewModuleRootManager\" LANGUAGE_LEVEL=\"JDK_1_4\">\r\n" +
                    "    <exclude-output />\r\n" +
                    "    <output url=\"file://$MODULE_DIR$/classes\" />\r\n" +
                    "    <content url=\"file://$MODULE_DIR$/\" />\r\n" +
                    "    <orderEntry type=\"jdk\" jdkName=\"jdk.name\" jdkType=\"JavaSDK\" />\r\n" +
                    "    <orderEntry type=\"sourceFolder\" isTestSource=\"false\" />\r\n" +
                    "    <orderEntryProperties />\r\n" +
                    "  </component>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingSourceFolder() {
    setEclipseProjectLocation("c:/workspace/project");
    setEclipseProjectClasspaths(createClasspath(CPE_SOURCE, "/project/scr1"),
                                createClasspath(CPE_SOURCE, "/project/folder/scr2"));

    Exporter exporter = new Exporter(ideaProject, null, true, false, false);
    exporter.createModuleContent(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <content url=\"file://$MODULE_DIR$/\">\r\n" +
                    "    <sourceFolder url=\"file://$MODULE_DIR$/scr1\" isTestSource=\"false\" />\r\n" +
                    "    <sourceFolder url=\"file://$MODULE_DIR$/folder/scr2\" isTestSource=\"false\" />\r\n" +
                    "  </content>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingSourceFolderWithoutCopyingContent() {
    setEclipseProjectLocation("c:/workspace/project");
    setEclipseProjectClasspaths(createClasspath(CPE_SOURCE, "/project/scr1"),
                                createClasspath(CPE_SOURCE, "/project/folder/scr2"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleContent(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <content url=\"file://c:/workspace/project\">\r\n" +
                    "    <sourceFolder url=\"file://c:/workspace/project/scr1\" isTestSource=\"false\" />\r\n" +
                    "    <sourceFolder url=\"file://c:/workspace/project/folder/scr2\" isTestSource=\"false\" />\r\n" +
                    "  </content>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithoutContentCopying() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "c:/workspace/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/workspace/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithContentCopying() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project");
    addEclipseWorkspaceResource("/project/lib1.jar", "c:/workspace/project/lib1.jar");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "/project/lib1.jar"),
                                createClasspath(CPE_LIBRARY, "c:/workspace/lib2.jar"),
                                createClasspath(CPE_LIBRARY, "d:/temp/lib3.jar"));

    Exporter exporter = new Exporter(ideaProject, null, true, false, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$MODULE_DIR$/lib1.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/workspace/lib2.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://d:/temp/lib3.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithContentCopyingFromAnotherModule() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project");
    addEclipseWorkspaceResource("/anotherProject/lib.jar",
                                "c:/workspace/anotherProject/lib.jar");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "/anotherProject/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, true, false, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$MODULE_DIR$/../anotherProject/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithPathVariables() {
    setEclipseProjectClasspaths(createClasspath(CPE_VARIABLE, "VAR_NAME/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, false, true, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$VAR_NAME$/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithoutPathVariables() {
    setEclipseWorkspaceResolverdVariablePath("VAR_NAME/lib.jar", "c:/folder/lib.jar");
    setEclipseProjectClasspaths(createClasspath(CPE_VARIABLE, "VAR_NAME/lib.jar"),
                                createClasspath(CPE_VARIABLE, "UNRESOLVED_VAR_NAME/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/folder/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://UNRESOLVED_VAR_NAME/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesInEclipseInstallationDirectory() {
    setEclipseInstallationDirectory("c:/eclipse");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY,
                                                "c:/eclipse/plugins/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, false, true, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$ECLIPSE_HOME$/plugins/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWhenProjectIsInEclipseInstallationDirectory() {
    setEclipseInstallationDirectory("c:/eclipse");
    setEclipseWorkspaceLocation("c:/eclipse/workspace");
    setEclipseProjectLocation("c:/eclipse/workspace/project1");

    addEclipseWorkspaceResource("/project1/lib.jar",
                                "c:/eclipse/workspace/project1/lib.jar");
    addEclipseWorkspaceResource("/project2/lib.jar",
                                "c:/eclipse/workspace/project2/lib.jar");

    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "/project1/lib.jar"),
                                createClasspath(CPE_LIBRARY, "/project2/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, false, true, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/eclipse/workspace/project1/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/eclipse/workspace/project2/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingLibrariesWithContentCopyingWhenProjectIsInEclipseInstallationDirectory() {
    setEclipseInstallationDirectory("c:/eclipse");
    setEclipseWorkspaceLocation("c:/eclipse/workspace");
    setEclipseProjectLocation("c:/eclipse/workspace/project1");

    addEclipseWorkspaceResource("/project1/lib.jar",
                                "c:/eclipse/workspace/project1/lib.jar");
    addEclipseWorkspaceResource("/project2/lib.jar",
                                "c:/eclipse/workspace/project2/lib.jar");

    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "/project1/lib.jar"),
                                createClasspath(CPE_LIBRARY, "/project2/lib.jar"));

    Exporter exporter = new Exporter(ideaProject, null, true, true, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$MODULE_DIR$/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "  <orderEntry type=\"module-library\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://$MODULE_DIR$/../project2/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingExportedLibrary() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "c:/workspace/lib.jar", true));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module-library\" exported=\"\">\r\n" +
                    "    <library>\r\n" +
                    "      <CLASSES>\r\n" +
                    "        <root url=\"jar://c:/workspace/lib.jar!/\" />\r\n" +
                    "      </CLASSES>\r\n" +
                    "    </library>\r\n" +
                    "  </orderEntry>\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingModuleLibrariesWithDeclaring() {
    setEclipseProjectClasspaths(createClasspath(CPE_CONTAINER, "path"));
    addEclipseProjectClasspathContainer(
      "path", createClasspathContainer(K_APPLICATION, "library.name"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, true);
    exporter.createModuleModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"library\" name=\"library.name\" level=\"project\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingModuleLibrariesWithoutDeclaring() {
    setEclipseProjectClasspaths(createClasspath(CPE_CONTAINER, "path"));
    addEclipseProjectClasspathContainer(
      "path", createClasspathContainer(K_APPLICATION, "library.name"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"library\" name=\"library.name\" level=\"application\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingExportedModuleLibraries() {
    setEclipseProjectClasspaths(createClasspath(CPE_CONTAINER, "path", true));
    addEclipseProjectClasspathContainer(
      "path", createClasspathContainer(K_APPLICATION, "library.name"));

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleModuleLibraries(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"library\" exported=\"\" name=\"library.name\" level=\"application\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  @Test
  public void testWritingReferencedModules() {
    TestIdeaModule refModule = createTestIdeaModule();
    refModule.getEclipseProject().setName("ref.module.name");

    ideaProject.setModules(ideaModule, refModule);
    ideaModule.getEclipseProject().setReferencedProjects(refModule.getEclipseProject());

    Exporter exporter = new Exporter(ideaProject, null, false, false, false);
    exporter.createModuleDependencies(xmlDoc.getRootElement(), ideaModule);

    assertXmlOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<root>\r\n" +
                    "  <orderEntry type=\"module\" module-name=\"ref.module.name\" />\r\n" +
                    "</root>\r\n" +
                    "\r\n");
  }

  private void assertXmlOutput(String expected) {
    XMLOutputter outputter = new XMLOutputter();

    Format format = Format.getPrettyFormat();
    outputter.setFormat(format);

    assertEquals(expected, outputter.outputString(xmlDoc));
  }

  private void deleteDirectory(File dir) {
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) deleteDirectory(f);
      else f.delete();
    }
    dir.delete();
  }
}
