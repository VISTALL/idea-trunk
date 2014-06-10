/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package wstests;

import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.index.FileBasedWSIndex;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @by maxim
 * @author Konstantin Bulenkov
 */
public abstract class BaseWSTestCase extends TestCase {
  // -Didea.testingFramework.mockJDK
  protected CodeInsightTestFixture myFixture;
  protected PsiManager myManager;
  private FileBasedWSIndex wsIndex;
  protected WebServicesPluginSettings pluginSettings;
  protected WebServicesPlugin plugin;
  protected final @NonNls Set<String> ourTestsWithJdk = new HashSet<String>();
  private List<PsiElement> myHardRefsToPsiFiles;
  private boolean isSelenaOrBetter;
  private boolean isStandAlone;
  @NonNls private static final String SVN_PLUGINS_DIR_NAME = "svnPlugins";

  protected void setUp() throws Exception {
    super.setUp();
    
    final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = fixtureFactory.createFixtureBuilder();

    myFixture = fixtureFactory.createCodeInsightFixture(projectBuilder.getFixture());

    final WebModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(WebModuleFixtureBuilder.class);

    moduleBuilder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("/");
    try {
      isSelenaOrBetter = true;
      Method method = moduleBuilder.getClass().getMethod("setOutputPath", String.class);
      method.setAccessible(true);
      method.invoke(moduleBuilder, myFixture.getTempDirPath());
    } catch (Exception e) {
      isSelenaOrBetter = false;
    } // API do not exist in Demetra

    isStandAlone = getPluginBasePath().indexOf(SVN_PLUGINS_DIR_NAME) == -1;
    moduleBuilder.addWebRoot(myFixture.getTempDirPath(),"/zzz");
    
    if (ourTestsWithJdk.contains(getTestName())) {
      moduleBuilder.setMockJdkLevel(isStandAlone ? JavaModuleFixtureBuilder.MockJdkLevel.jdk14 :JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    }

    configureLibs(moduleBuilder);
    configureInspections();

    myFixture.setUp();

    String basePath = getPluginBasePath();
    myFixture.setTestDataPath(basePath + "testData/"+getTestDataPath());

    final Project project = myFixture.getProject();
    myManager = PsiManager.getInstance(project);
    EnvironmentFacade.getInstance().setEffectiveLanguageLevel(LanguageLevel.JDK_1_5, project);

    WebServicesPlugin.setTestTemplates(true);
    
    if (isStandAlone && !isSelenaOrBetter) {
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {

            public void run() {
              WebServicesPluginSettings.setInstance(pluginSettings = new WebServicesPluginSettings());
              pluginSettings.initComponent();

              plugin = new WebServicesPlugin(project);
              WebServicesPlugin.setInstance(plugin);
              plugin.initComponent();
              plugin.projectOpened();

              wsIndex = FileBasedWSIndex.getInstance();
              //WSIndex.setInstance(wsIndex);
              //wsIndex.projectOpened();
            }
          });
        }
      }, ModalityState.current());
    } else {
      WebServicesPlugin.getInstance(myFixture.getProject()).projectOpened();
      //WSIndex.getInstance(myFixture.getProject()).projectOpened();
    }
  }

  protected abstract void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder);
  protected abstract void configureInspections();
  protected abstract @NonNls String getTestDataPath();

  protected void tearDown() throws Exception {
    myHardRefsToPsiFiles = null;
    plugin.setTestTemplates(false);

    if (pluginSettings != null) {
      pluginSettings.disposeComponent();
      pluginSettings = null;
      WebServicesPluginSettings.setInstance(null);

      plugin.projectClosed();
      plugin = null;
      WebServicesPlugin.setInstance(null);

      //wsIndex.projectClosed();
      //WSIndex.setInstance(null);
      wsIndex = null;
    } else {
      WebServicesPlugin.getInstance(myFixture.getProject()).projectClosed();
      //WSIndex.getInstance(myFixture.getProject()).projectClosed();
    }

    myFixture.tearDown();
    myFixture = null;
    myManager = null;
    super.tearDown();
  }

  protected void doHighlightingTest(@NonNls String... _files) throws Throwable {
    doHighlightingTest(false, _files);
  }

  protected void doHighlightingTest(boolean withInfos,@NonNls String... _files) throws Throwable {
    doHighlightingTest(true, withInfos, _files);
  }

  protected void doHighlightingTest(boolean withWarnings,boolean withInfos,@NonNls String... _files) throws Throwable {
    final String[] files = prepareFiles(_files);
    myFixture.testHighlighting(withWarnings, false, withInfos, files);
  }

  protected Collection<IntentionAction> doHighlightingForIntentions(String... _files) throws Throwable {
    final String[] files = prepareFiles(_files);
    return myFixture.getAvailableIntentions(files);
  }

  protected IntentionAction findIntentionActionWithName(@NotNull String intentionActionName, Collection<IntentionAction> intentions) {
    for(IntentionAction a:intentions) {
      if (intentionActionName.equals(a.getText())) return a;
    }
    return null;
  }

  protected void doQuickFixTest_(String intentionActionName, String afterName, String extension,
                                  String ... fileNames) throws Throwable {
    final Collection<IntentionAction> actions = doHighlightingForIntentions(fileNames);
    final IntentionAction intentionAction = findIntentionActionWithName(intentionActionName, actions);
    assertNotNull(intentionAction);
    myFixture.launchAction(intentionAction);
    myFixture.checkResultByFile(afterName + "." + extension);
  }

  protected void doQuickFixTest(String intentionActionName, String ... files) throws Throwable {
    doQuickFixTest_(intentionActionName, getTestName() + "_after","java", files);
  }

  protected String[] prepareFiles(String... _files) {
    if (_files == null || _files.length == 0) _files = new String[] { getJavaTestName() };
    final String[] files = _files;

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          final List<VirtualFile> filesList = new ArrayList<VirtualFile>();
          VirtualFile tempDir = LocalFileSystem.getInstance().findFileByIoFile(new File(myFixture.getTempDirPath()));

          for(int i = 0; i < files.length; ++i) {
            final String fileName = files[i];
            String basePath = getPluginBasePath();
            final File file = new File(basePath + "testData/" + getTestDataPath() + "/" + fileName);
            final String simpleName = fileName.substring(fileName.lastIndexOf('/') + 1);
            files[i] = simpleName;

            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
            vf.refresh(false, false);
            filesList.add(VfsUtil.copyFile(this, vf, tempDir));
          }
          tempDir.refresh(false, false);

          if (myHardRefsToPsiFiles == null) myHardRefsToPsiFiles = new ArrayList<PsiElement>(filesList.size());
          for(VirtualFile file:filesList) {
            final PsiFile psiFile = myManager.findFile(file);
            ((PsiFileImpl) psiFile).calcTreeElement();
            myHardRefsToPsiFiles.add(psiFile);
          }
        } catch (IOException e) {
          throw  new RuntimeException(e);
        }
      }
    });
    return files;
  }

  protected static String getPluginBasePath() {
    String basePath = PathManager.getHomePath() + File.separatorChar + SVN_PLUGINS_DIR_NAME + File.separatorChar + "WebServices" + File.separatorChar;
    if (!new File(basePath).exists()) basePath = new File("").getAbsolutePath() + File.separatorChar;
    return basePath;
  }

  protected String getTestName() {
    return getName().substring(4); // skip 'test' prefix
  }

  protected String getJavaTestName() {
    return getTestName() + ".java";
  }

  protected String getXmlTestName() {
    return getTestName() + ".xml";
  }

  protected String getWsdlTestName() {
    return getTestName() + ".wsdl";
  }

  protected String getWsddTestName() {
    return getTestName() + ".wsdd";
  }

  protected String getLibPath() {
    String libPath = PathManager.getLibPath();
    if (isStandAlone && isSelenaOrBetter) {
      libPath = "C:/Program Files/JetBrains/IntelliJ IDEA 7.0/lib";
    }
    return libPath;
  }
}
