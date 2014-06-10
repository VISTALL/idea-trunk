package com.intellij.webBeans;

import com.intellij.facet.FacetManager;
import com.intellij.j2ee.JavaeeTestUtil;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.javaee.ejb.facet.EjbFacetType;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.util.descriptors.ConfigFileMetaData;
import com.intellij.webBeans.facet.WebBeansFacet;
import com.intellij.webBeans.facet.WebBeansFacetType;

import java.io.File;

public abstract class AbstractWebBeansTestCase<T extends JavaModuleFixtureBuilder> extends BaseWebBeansTestCase {

  protected CodeInsightTestFixture myFixture;
  protected ModuleFixture myModuleTestFixture;
  protected Project myProject;
  protected Module myModule;

  protected Class<T> getModuleFixtureBuilderClass() {
    return (Class<T>)JavaModuleFixtureBuilder.class;
  }

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();

    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    final T moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());

    myFixture.setTestDataPath(getTestDataPath());

    myFixture.enableInspections(new WebBeansApplicationComponent().getInspectionClasses());

    configureModule(moduleBuilder);

    myFixture.setUp();

    myProject = myFixture.getProject();
    myModuleTestFixture = moduleBuilder.getFixture();
    myModule = myModuleTestFixture.getModule();

    createFacet();
    WebBeansProjectComponent.getInstance(myProject).projectOpened();
  }

  protected WebBeansFacet createFacet() {
    final RunResult<WebBeansFacet> runResult = new WriteCommandAction<WebBeansFacet>(myProject) {
      protected void run(final Result<WebBeansFacet> result) throws Throwable {
        FacetManager facetManager = FacetManager.getInstance(myModule);
        result.setResult(facetManager.addFacet(WebBeansFacetType.INSTANCE, WebBeansFacetType.INSTANCE.getPresentableName(), null));
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

  protected void configureModule(final T moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addSourceRoot("");
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  protected void addWebBeansJar(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("webbeans-api.jar", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 getLibsDirectory() + "webbeans-api.jar");
    moduleBuilder.addLibraryJars("webbeans-ri.jar", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 getLibsDirectory() + "webbeans-ri.jar");
    moduleBuilder.addLibraryJars("webbeans-ri-spi.jar", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 getLibsDirectory() + "webbeans-ri-spi.jar");
  }

  protected void configureJSF(final WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addSourceRoot("src");

    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");

    moduleBuilder.addLibraryJars("myfaces", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces.jar");
    moduleBuilder
      .addLibraryJars("myfaces-jsf-api", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces-jsf-api.jar");
  }

  private String getLibsDirectory() {
    return "libs/webbeans-1.0.0.ALPHA/";
  }

  protected void tearDown() throws Exception {
    WebBeansProjectComponent.getInstance(myProject).projectClosed();
    myFixture.tearDown();
    myFixture = null;
    myModuleTestFixture = null;
    myProject = null;
    myModule = null;

    super.tearDown();
  }

  protected XmlFile getXmlFile(final String path) {
    final String url = myFixture.getTempDirPath() + File.separatorChar + path;

    final VirtualFile[] virtualFile = new VirtualFile[1];
    new WriteCommandAction(myFixture.getProject()) {
      protected void run(final Result result) throws Throwable {
        virtualFile[0] = LocalFileSystem.getInstance().refreshAndFindFileByPath(url);
      }
    }.execute();

    assertNotNull("Cannot find file " + url, virtualFile[0]);
    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile[0]);

    assertNotNull(psiFile);
    assertTrue("'" + path + "' must be a xml file", psiFile instanceof XmlFile);

    return (XmlFile)psiFile;
  }

  protected void addJavaeeSupport() {
    new WriteCommandAction(myProject) {
      protected void run(final Result result) throws Throwable {
        JavaeeUtil.addFacet(myModule, EjbFacetType.INSTANCE);
      }
    }.execute();

    PsiTestUtil.addLibrary(myModule, "JavaEE", PathManager.getHomePath() + "/lib/", "javaee.jar", "javase-javax.jar");
  }

  protected void configeEjbDescriptor() {
    JavaeeFacet facet = JavaeeTestUtil.getJavaeeFacet(myModule);
    final ConfigFileMetaData metaData = JavaeeTestUtil.getMainMetaData(facet.getTypeId());

    facet.getDescriptorsContainer().getConfiguration()
      .addConfigFile(metaData, VfsUtil.pathToUrl(getTestDataPath() + "META-INF/ejb-jar.xml"));
  }
}

