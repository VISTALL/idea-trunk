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

package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.WebServicePlatformUtils;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.ExternalEngineThatBundlesJEEJars;
import com.intellij.CommonBundle;
import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.*;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @by maxim
 */
public class InvokeExternalCodeUtil {
  private static final Logger LOG = Logger.getInstance("webservicesplugin.invokeexternalcode");
  private static boolean hasProblemWithCopyingBefore;
  private static final Map<String, String> ourBasePath2TempEndorsedDirectory = new HashMap<String, String>();

  private static String buildDescriptionFromCommandsAndLaunchDir(File dir, String[] commands) {
    final StringBuilder b = new StringBuilder();
    if (dir != null) b.append(dir.getAbsolutePath()).append(">");
    final int initialLen = b.length();

    for(String s: commands) {
      if (initialLen != b.length()) b.append(" ");
      b.append(s);
    }
    return b.toString();
  }

  public static String toAntPath(@NotNull String outputPath) {
    return outputPath.replace(File.separatorChar,'/');
  }

  public static void navigateToPackage(@NotNull Module moduleForFile,@NotNull String packagePrefix,@NotNull String outputPath) {
    final PsiPackage aPackage = EnvironmentFacade.getInstance().findPackage(packagePrefix, moduleForFile.getProject());

    if (aPackage != null) {
      final PsiDirectory[] directories = aPackage.getDirectories(GlobalSearchScope.moduleScope(moduleForFile));

      if (directories.length > 0) {
        PsiDirectory chosenDirectory = directories[0];

        final VirtualFile relativeFile = EnvironmentFacade.getInstance().findRelativeFile(outputPath, null);

        if (relativeFile != null) {
          for(PsiDirectory dir:directories) {
            if (VfsUtil.isAncestor(relativeFile, dir.getVirtualFile(), false)) {
              chosenDirectory = dir;
              break;
            }
          }
        }

        chosenDirectory.navigate(true);
      }
    }
  }

  public static void addEndorsedJarDirectory(@NotNull JavaExternalProcessHandler processHandler,
                                             @NotNull ExternalEngine engine, @Nullable final Module targetModule) {
    if (!(engine instanceof ExternalEngineThatBundlesJEEJars)) return;
    String basePath = engine.getBasePath();
    boolean jdk1_6 = false;

    if (targetModule != null) {
      jdk1_6 = WebServicePlatformUtils.isJdk1_6SetUpForModule(targetModule);

      if (jdk1_6 && !hasProblemWithCopyingBefore) {
        boolean wasAbleToCopyEndorsedJarsToJdk = true;
        final ExternalEngine.LibraryDescriptorContext libraryDescriptorContext = new ExternalEngine.LibraryDescriptorContext() {
          public boolean isForRunningGeneratedCode() // false -> to run engine
          {
            return false;
          }

          public String getBindingType() {
            return null;
          }

          public Module getTargetModule() {
            return targetModule;
          }
        };

        final Sdk projectJdk = JavaExternalProcessHandler.evaluateJdkForModule(targetModule);
        String endorsedDirectory = ourBasePath2TempEndorsedDirectory.get(basePath);
        if (endorsedDirectory == null) {
          endorsedDirectory = EnvironmentFacade.getInstance().getSdkHome(projectJdk) + File.separatorChar + "jre" + File.separatorChar + "lib" + File.separatorChar + "endorsed";
        } else {
          wasAbleToCopyEndorsedJarsToJdk = false;
        }
        File file = new File(endorsedDirectory);

        if(!file.exists() && !file.mkdirs()) {
          try {
            endorsedDirectory = FileUtils.createTempDir("endorsed.temp").getPath();
            ourBasePath2TempEndorsedDirectory.put(basePath, endorsedDirectory);
            wasAbleToCopyEndorsedJarsToJdk = false;
            file = new File(endorsedDirectory);
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        }

        for (String jeeJarName : ((ExternalEngineThatBundlesJEEJars) engine).getJEEJarNames(libraryDescriptorContext)) {
          File sourceJar = new File(basePath, jeeJarName);
          int i = jeeJarName.lastIndexOf(File.separatorChar);
          if (i == -1) i = jeeJarName.lastIndexOf('/');
          File destJar = new File(file, jeeJarName.substring(i + 1));

          assert sourceJar.exists();
          if (!destJar.exists() || sourceJar.length() != destJar.length()) {
            try {
              final FileInputStream inputStream = new FileInputStream(sourceJar);
              FileUtils.saveStreamContentAsFile(destJar.getPath(), inputStream);
              inputStream.close();
              if (!wasAbleToCopyEndorsedJarsToJdk) destJar.deleteOnExit();
            } catch (IOException e) {
              wasAbleToCopyEndorsedJarsToJdk = false;
              hasProblemWithCopyingBefore = true;
              Messages.showErrorDialog(
                MessageFormat.format(
                  WSBundle.message("exception.when.copying.endorsed.jars.message"),
                  sourceJar.getPath(), destJar.getPath()
                ),
                WSBundle.message("exception.when.copying.endorsed.jars.title")
              );
              break;
            }
          }
        }

        if (wasAbleToCopyEndorsedJarsToJdk) return;
      }
    }

    if (basePath != null && jdk1_6) {
      final String tempPath = ourBasePath2TempEndorsedDirectory.get(basePath);
      if (new File(basePath,"lib").exists()) basePath += File.separatorChar + "lib";
      processHandler.addCommandLineProperty("java.endorsed.dirs", tempPath != null ? tempPath:basePath);
    }
  }

  public interface OutputConsumer {
    /**
     * true to continue execute default errOutput filtering
     */
    boolean handle(String output, String errOutput) throws ExternalCodeException;
  }

  static class StreamReaderThread implements Runnable {
    private final InputStream in;
    private StringBuilder builder;

    StreamReaderThread(InputStream stream) { in = stream; }

    public void run() {
      final Reader reader = new InputStreamReader(in);
      final char[] buf = new char[128];
      builder = new StringBuilder();

      try {
        while(true) {
          int read = reader.read(buf);
          if (read == -1) break;
          builder.append(buf, 0, read);
        }
      } catch (IOException e) {
        try { reader.close(); } catch(IOException ex ) {}
      }
    }
  }

  static abstract class ExternalProcessHandlerBase {
    protected final String myName;
    protected String[] myCommands;
    protected File myLaunchDir;
    protected OutputConsumer myOutputConsumer;

    protected ExternalProcessHandlerBase(@NotNull String name) {
      myName = name;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    protected abstract @NotNull List<String> buildCommands();

    @NotNull
    public String[] getCommands() {
      if (myCommands == null) {
        @NotNull List<String> commands = buildCommands();
        myCommands = commands.toArray(new String[commands.size()]);
      }
      return myCommands;
    }

    @Nullable
    public File getLaunchDir() {
      return myLaunchDir;
    }

    public void setLaunchDir(@Nullable File launchDir) {
      myLaunchDir = launchDir;
    }

    @Nullable
    public OutputConsumer getOutputConsumer() {
      return myOutputConsumer;
    }

    public void setOutputConsumer(@Nullable OutputConsumer outputConsumer) {
      myOutputConsumer = outputConsumer;
    }

    public String describeExecution() {
      return buildDescriptionFromCommandsAndLaunchDir(myLaunchDir, getCommands());
    }
  }

  public static class JarProcessHandler extends JavaExternalProcessHandler {
    public JarProcessHandler(@NotNull String name, @NotNull String _jarName, String[] _classPath, String[] _parameters, Module module, boolean includeToolsJar) {
      super(name, _jarName, _classPath, _parameters, module, includeToolsJar);
    }

    protected void addInvokedClass(List<String> parametersList) {
      parametersList.add("-jar");
      parametersList.add(className);
    }
  }
  
  public static class BatchExternalProcessHandler extends ExternalProcessHandlerBase implements ExternalProcessHandler {
    private final List<String> myParams;
    private String myBaseBatchFileName;

    public BatchExternalProcessHandler(@NotNull String name, @NotNull String baseBatchFileName, @NotNull List<String> params) {
      super(name);
      myBaseBatchFileName = baseBatchFileName;
      myParams = params;
    }

    protected List<String> buildCommands() {
      final List<String> commands = new ArrayList<String>(myParams.size() + 2);
      if (SystemInfo.isWindows) {
        commands.add("cmd");
        commands.add("/c");
      }

      myBaseBatchFileName += SystemInfo.isWindows ? ".bat":".sh";
      commands.add(myBaseBatchFileName);
      commands.addAll(myParams);
      return commands;
    }
  }

  public static class ANTExternalProcessHandler implements ExternalProcessHandler {
    private final ExternalProcessHandler myJavaExternalProcessHandler;
    private final String myName;
    private final String myAntFileText;

    public ANTExternalProcessHandler(@NotNull String name, String antFileText, String[] additionalClassPathForTasks, Module targetModule) {
      myName = name;
      myAntFileText = antFileText;
      File tempFile = null;
      try {
        tempFile = File.createTempFile("build", ".xml");
        tempFile.deleteOnExit();
        OutputStream outputStream = new FileOutputStream(tempFile.getAbsolutePath());
        outputStream.write(antFileText.getBytes());
        outputStream.close();

        List<String> parameters = new LinkedList<String>();

        String homeDir = EnvironmentFacade.getInstance().getAntHomeDir();
        homeDir = homeDir.replace('\\','/');
        parameters.add("-Dant.home=" + homeDir);

        if (additionalClassPathForTasks != null && additionalClassPathForTasks.length > 0) {
          parameters.add("-cp");
          StringBuilder additionalClassPath = new StringBuilder();

          for(String a:additionalClassPathForTasks) {
            if (additionalClassPath.length() > 0) additionalClassPath.append(CLASS_PATH_SEPARATOR);
            additionalClassPath.append(a);
          }
          parameters.add(additionalClassPath.toString());
        }

        parameters.add("-f");
        parameters.add(tempFile.getCanonicalPath());

        myJavaExternalProcessHandler = new JavaExternalProcessHandler(
          myName,
          "org.apache.tools.ant.launch.Launcher",
          new String[] { homeDir + "/lib/ant-launcher.jar" },
          parameters.toArray(new String[parameters.size()]),
          targetModule,
          true
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @NotNull
    public String getName() {
      return myName;
    }

    public String[] getCommands() {
      return myJavaExternalProcessHandler.getCommands();
    }

    @Nullable
    public File getLaunchDir() {
      return myJavaExternalProcessHandler.getLaunchDir();
    }

    public void setLaunchDir(@Nullable File launchDir) {
      myJavaExternalProcessHandler.setLaunchDir(launchDir);
    }

    public OutputConsumer getOutputConsumer() {
      return myJavaExternalProcessHandler.getOutputConsumer();
    }

    public void setOutputConsumer(OutputConsumer outputConsumer) {
      myJavaExternalProcessHandler.setOutputConsumer(outputConsumer);
    }

    public String describeExecution() {
      return myJavaExternalProcessHandler.describeExecution() + "\nAnt file:\n" + myAntFileText;
    }
  }

  public static class JavaExternalProcessHandler extends ExternalProcessHandlerBase implements ExternalProcessHandler {
    private @NotNull final Sdk jdk;
    private final String [] classPath;
    protected final String className;
    private final String[] parameters;
    private final boolean myIncludeToolsJar;

    private Map<String,String> commandLineProperties;

    public JavaExternalProcessHandler(@NotNull String name, @NotNull @NonNls String _className,String[] _classPath, String[] _parameters, Module module,
                                      boolean includeToolsJar) {
      super(name);

      if (LOG.isDebugEnabled()) {
        LOG.debug("About to execute " + _className);
        LOG.debug("Module " + module);
      }

      Sdk _jdk = evaluateJdkForModule(module);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Finally using jdk:" + EnvironmentFacade.getInstance().getVMExecutablePathForSdk(_jdk));
      }

      jdk = _jdk;
      className = _className;
      classPath = _classPath != null ? _classPath: ArrayUtil.EMPTY_STRING_ARRAY;
      parameters = _parameters;
      myIncludeToolsJar = includeToolsJar;

      final HttpConfigurable proxy = HttpConfigurable.getInstance();
      if (proxy.USE_HTTP_PROXY) {
        addCommandLineProperty("http.proxyHost", proxy.PROXY_HOST);
        addCommandLineProperty("http.proxyPort", "" + proxy.PROXY_PORT);
      }
    }

    public void addCommandLineProperty(@NonNls String property, String value) {
      if (commandLineProperties == null) commandLineProperties = new HashMap<String, String>(2);
      commandLineProperties.put(property, value);
    }

    public static Sdk evaluateJdkForModule(Module module) {
      Sdk _jdk = module != null ? EnvironmentFacade.getInstance().getProjectJdkFromModule(module):null;

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "Using module jdk:" +
            (EnvironmentFacade.getInstance().isAcceptableSdk(_jdk) ?
              EnvironmentFacade.getInstance().getVMExecutablePathForSdk(_jdk):
              null
            )
        );
      }

      if (_jdk == null && module != null) {
        _jdk = ProjectRootManager.getInstance(module.getProject()).getProjectJdk();

        if (LOG.isDebugEnabled()) {
          LOG.debug(
            "Using project jdk:" +
              (EnvironmentFacade.getInstance().isAcceptableSdk(_jdk) ?
                EnvironmentFacade.getInstance().getVMExecutablePathForSdk(_jdk):
                null
              )
          );
        }
      }
      if (!EnvironmentFacade.getInstance().isAcceptableSdk(_jdk)) _jdk = EnvironmentFacade.getInstance().getInternalJdk();
      return _jdk;
    }

    public String getName() {
      return myName;
    }

    protected List<String> buildCommands() {
      final boolean hasClassPath = classPath != null && classPath.length > 0 || myIncludeToolsJar || commandLineProperties != null;
      final List<String> parametersList = new ArrayList<String>(
        parameters.length + 3 + (hasClassPath ? 2:0) + (commandLineProperties != null ? commandLineProperties.size() : 0));

      parametersList.add(EnvironmentFacade.getInstance().getVMExecutablePathForSdk(jdk));
      if (hasClassPath) {
        parametersList.add("-classpath");
        StringBuilder builder = new StringBuilder();

        for(String u:classPath) {
          if (builder.length() > 0) builder.append(CLASS_PATH_SEPARATOR);
          u = FileUtils.removeFileProtocolPrefixIfPresent(u);

          builder.append(u);
        }
        if (myIncludeToolsJar) {
          if (builder.length() > 0) builder.append(CLASS_PATH_SEPARATOR);
          builder.append(EnvironmentFacade.getInstance().getToolsJarPathForSdk(jdk));
        }

        parametersList.add(builder.toString());
      }

      if (commandLineProperties != null) {
        for(String key:commandLineProperties.keySet()) {
          parametersList.add("-D" + key + "=" + commandLineProperties.get(key));
        }
      }

      parametersList.add("-Xmx" + WebServicesPluginSettings.getInstance().getMemorySizeToLaunchVM() + "M");

      addInvokedClass(parametersList);
      parametersList.addAll(Arrays.asList(parameters));
      return parametersList;
    }

    protected void addInvokedClass(List<String> parametersList) {
      parametersList.add(className);
    }
  }

  public static void invokeExternalProcess(final ExternalProcessHandler handler, Project project) throws ExternalCodeException {
    invokeExternalProcess(handler, project, null, null, null, null);
  }

  public static void invokeExternalProcess2(final ExternalProcessHandler handler, Project project,
                                           final Runnable actionAtSuccess, final Function<Exception, Void> actionAtFailure,
                                           Function<Void, Boolean> isRerunAvailable, Runnable editAndRunAgain
                                           ) {
    assert actionAtSuccess != null;
    runViaConsole(handler, project, actionAtSuccess, actionAtFailure, isRerunAvailable, editAndRunAgain);

  }

  public static void invokeExternalProcess(final ExternalProcessHandler handler, Project project,
                                           final Runnable actionAtSuccess, final Function<Exception, Void> actionAtFailure,
                                           Function<Void, Boolean> isRerunAvailable, Runnable editAndRunAgain
                                           ) throws ExternalCodeException {
    if (actionAtSuccess != null) {
      runViaConsole(handler, project, actionAtSuccess, actionAtFailure, isRerunAvailable, editAndRunAgain);
      return;
    }

    if (SwingUtilities.isEventDispatchThread()) {
      final ExternalCodeException[] result = new ExternalCodeException[1];

      EnvironmentFacade.getInstance().runProcessWithProgressSynchronously(
        new Runnable() {
          public void run() {
            try {
              doInvoke(handler);
            } catch (ExternalCodeException e) {
              result[0] = e;
            }
          }
        },
        "Launching " + handler.getName(),
        true,
        project
      );
      if (result[0] != null) throw result[0];
    } else {
      doInvoke(handler);
    }

  }

  public static void runViaConsole(final @NotNull ExternalProcessHandler handler, final @NotNull Project project,
                                    final @NotNull Runnable actionAtSuccess, final @NotNull Function<Exception, Void> actionAtFailure,
                                    final @Nullable Function<Void, Boolean> rerunAvailable, final Runnable editAndRunAgain) {
    try {
      final OSProcessHandler processHandler = new OSProcessHandler(startProcess(handler),handler.describeExecution());

      final StringBuilder output = new StringBuilder();
      final StringBuilder errorOutput = new StringBuilder();

      final ProcessListener processListener = new ProcessListener() {
        public void startNotified(ProcessEvent event) {
        }

        public void processTerminated(ProcessEvent event) {
          processHandler.notifyTextAvailable("Done\n", ProcessOutputTypes.SYSTEM);
          processHandler.removeProcessListener(this);
        }

        public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        }

        public void onTextAvailable(ProcessEvent event, Key outputType) {
          if (outputType == ProcessOutputTypes.STDOUT) output.append(event.getText());
          else if (outputType == ProcessOutputTypes.STDERR) errorOutput.append(event.getText());
        }
      };
      processHandler.addProcessListener(processListener);

// consoleview creating
      final ConsoleView myConsoleView = EnvironmentFacade.getInstance().getConsole(project);
      myConsoleView.setHelpId(handler.getName());
      myConsoleView.addMessageFilter(new Filter() {
        public Result applyFilter(String line, int entireLength) {
          return null;
        }
      });

      myConsoleView.attachToProcess(processHandler);

      final DefaultActionGroup toolbarActions = new DefaultActionGroup();
      
      JPanel content = new JPanel(new BorderLayout());
      content.add(myConsoleView.getComponent(), BorderLayout.CENTER);
      content.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).getComponent(), BorderLayout.WEST);
      
      final RunContentDescriptor myDescriptor = new RunContentDescriptor(myConsoleView, processHandler,
        content,handler.getName());
      
// adding actions

      final Function<Void, Boolean> isRerunAvailable = new Function<Void, Boolean>() {
        public Boolean fun(Void s) {
          return processHandler.isProcessTerminated() && rerunAvailable != null && rerunAvailable.fun(s) ? Boolean.TRUE:Boolean.FALSE;
        }
      };

      final CloseAction closeAction = EnvironmentFacade.getInstance().createRunnerAction(myDescriptor, project);

      final Runnable rerun = new Runnable() {
        public void run() {
          FileDocumentManager.getInstance().saveAllDocuments();
          closeAction.actionPerformed(null);
          runViaConsole(handler, project, actionAtSuccess, actionAtFailure, rerunAvailable, editAndRunAgain);
        }
      };
      toolbarActions.add(new RerunAction(myConsoleView, rerun, isRerunAvailable));

      toolbarActions.add(new RefreshAction(new Runnable() {
        public void run() {
          closeAction.actionPerformed(null);
          editAndRunAgain.run();
        }
      }, isRerunAvailable));

      //close
      toolbarActions.add(closeAction);

      final Runnable action = new Runnable() {
        public void run() {
          processHandler.startNotify();
          processHandler.waitFor();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              boolean showRunContent = true;
              boolean wasErrorOutput = false;

              try {
                doFilter(handler, output.toString(),errorOutput.toString());
                if (CompilerWorkspaceConfiguration.getInstance(project).CLOSE_MESSAGE_VIEW_IF_SUCCESS) showRunContent = false;
              } catch(ExternalCodeException  ex) {
                actionAtFailure.fun(ex);
                wasErrorOutput = true;
              }

              // showing run content
              if (showRunContent) {
                final RunContentManager contentManager = ExecutionManager.getInstance(project).getContentManager();
                EnvironmentFacade.getInstance().showRunContent(contentManager, myDescriptor);
                contentManager.addRunContentListener(new RunContentListener() {
                  public void contentSelected(RunContentDescriptor runContentDescriptor) {
                  }

                  public void contentRemoved(RunContentDescriptor runContentDescriptor) {
                    if (runContentDescriptor == myDescriptor) {
                      myConsoleView.dispose();
                      contentManager.removeRunContentListener(this);
                    }
                  }
                });
              }

              if (!wasErrorOutput) {
                myConsoleView.dispose();
                actionAtSuccess.run();
              }
            }
          });
        }
      };
      EnvironmentFacade.getInstance().runProcessInTheBackground(project, handler.getName(),action);
    } catch (IOException e) {
      actionAtFailure.fun(e);
    }
  }

  private static class BasicRerunAction extends AnAction {
    private final Runnable myRerunTask;
    protected final Function<Void, Boolean> myRerunAvailablePredicate;

    public BasicRerunAction(String text, String description, Icon icon, Runnable rerun, Function<Void, Boolean> isRerunAvailablePredicate) {
      super(text,description,icon);
      myRerunAvailablePredicate = isRerunAvailablePredicate;
      myRerunTask = rerun;
    }

    public void actionPerformed(AnActionEvent e) {
      myRerunTask.run();
    }
  }

  private static class RefreshAction extends BasicRerunAction {
    public RefreshAction(Runnable rerun, Function<Void, Boolean> isRerunAvailablePredicate) {
      super(WSBundle.message("edit.refresh.action.text"),
            WSBundle.message("edit.refresh.action.text"),
            IconLoader.getIcon("/actions/refresh.png"), rerun, isRerunAvailablePredicate);
    }
  }

  private static class RerunAction extends BasicRerunAction {
    public RerunAction(final ConsoleView consoleView, Runnable rerun, Function<Void, Boolean> isRerunAvailablePredicate) {
      super(CommonBundle.message("action.rerun"),
            CommonBundle.message("action.rerun"),
            IconLoader.getIcon("/actions/refreshUsages.png"), rerun, isRerunAvailablePredicate);
      registerCustomShortcutSet(CommonShortcuts.getRerun(),consoleView.getComponent());
    }

    public void update(AnActionEvent e) {
      e.getPresentation().setEnabled(myRerunAvailablePredicate.fun(null));
    }
  }

  private static Process startProcess(ExternalProcessHandler handler) throws IOException {
    final String[] commands = handler.getCommands();

    if (LOG.isDebugEnabled()) {
      for(String command:commands) LOG.debug(command);
    }

    return Runtime.getRuntime().exec(
      commands,
      null,
      handler.getLaunchDir()
    );
  }

  /**
   * @Blocking
   */
  public static void doInvoke(ExternalProcessHandler handler) throws ExternalCodeException {
    try {
      final Process process = startProcess(handler);
      
      StreamReaderThread streamReaderThreadOut = new StreamReaderThread(process.getInputStream());
      StreamReaderThread streamReaderThreadErr = new StreamReaderThread(process.getErrorStream());

      final Future<?> outThreadReadControl = EnvironmentFacade.getInstance().executeOnPooledThread(streamReaderThreadOut);
      final Future<?> errThreadReadControl = EnvironmentFacade.getInstance().executeOnPooledThread(streamReaderThreadErr);

      process.waitFor();
      try {
        outThreadReadControl.get();
        errThreadReadControl.get();
      } catch (Exception e) { LOG.error(e); }

      final String message = streamReaderThreadErr.builder.toString();
      final String outMessage = streamReaderThreadOut.builder.toString();

      doFilter(handler, outMessage, message);
    } catch (IOException e) {
      throw new ExternalCodeException(e.getMessage());
    } catch (InterruptedException e) {
      throw new ExternalCodeException(e.getMessage());
    }
  }

  private static void doFilter(ExternalProcessHandler handler, String outMessage, String message) throws ExternalCodeException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("err:"+message);
      LOG.debug("out:"+outMessage);
    }

    if (handler.getOutputConsumer() != null) {
      if(!handler.getOutputConsumer().handle(outMessage, message)) return;
    }

    StringTokenizer tokenizer = new StringTokenizer(message, "\r\n");

    while(tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken();
      if (line.startsWith("log4j:") ||
          line.startsWith("Note:") ||
          line.startsWith("BUILD FAILED") ||
          line.startsWith(": FINEST,")
         ) continue;
      throw new ExternalCodeException(line);
    }
  }

  private static void pickupClassesForModule(Module moduleForFile, List<URL> urls) {
    ModuleRootManager instance = ModuleRootManager.getInstance(moduleForFile);
    VirtualFile[] files = instance.getFiles(OrderRootType.CLASSES_AND_OUTPUT);

    String jdkPath = null;
    final Sdk projectJdk = EnvironmentFacade.getInstance().getProjectJdkFromModule(moduleForFile);
    if (projectJdk != null) jdkPath = EnvironmentFacade.getInstance().getSdkHome(projectJdk);

    for (VirtualFile file : files) {
      try {
        if (jdkPath != null && file.getPath().startsWith(jdkPath)) continue;
        String spec = LibUtils.FILE_URL_PREFIX + file.getPath();
        if (file.isDirectory() && !(file.getFileSystem() instanceof JarFileSystem)) spec += "/";
        if (spec.endsWith(JarFileSystem.JAR_SEPARATOR)) {
          spec = spec.substring(0,spec.length() - JarFileSystem.JAR_SEPARATOR.length());
        }
        urls.add(new URL(spec));
      } catch (MalformedURLException e) {
        LOG.error(e);
      }
    }
  }

  public static final String CLASS_PATH_SEPARATOR = System.getProperty("path.separator");

  public static String buildClasspathForModule(Module moduleForFile) {
    List<URL> urls = buildURLSforModule(moduleForFile);
    StringBuilder builder = new StringBuilder();

    for(URL u:urls) {
      final String s = u.toExternalForm();
      if (builder.length() > 0) builder.append(CLASS_PATH_SEPARATOR);
      builder.append(s.substring(LibUtils.FILE_URL_PREFIX.length()));
    }

    return builder.toString();
  }

  public static String[] buildClasspathStringsForModule(Module moduleForFile) {
    final List<URL> urls = buildURLSforModule(moduleForFile);
    final String[] result = new String[urls.size()];
    int i = 0;

    for(URL u:urls) {
      final String s = u.toExternalForm();
      result[i++] = s.substring(LibUtils.FILE_URL_PREFIX.length());
    }

    return result;
  }

  private static List<URL> buildURLSforModule(Module moduleForFile) {
    List<URL> urls = new LinkedList<URL>();

    pickupClassesForModule(moduleForFile, urls);
    Module[] dependencies = ModuleRootManager.getInstance(moduleForFile).getDependencies();
    for (Module dependency : dependencies) {
      pickupClassesForModule(dependency, urls);
    }
    return urls;
  }

  public static class ExternalCodeException extends Exception {
    public ExternalCodeException(String message) {
      super(message);
      LOG.info(this);
    }

    public ExternalCodeException(IOException ex) {
      super(ex);
      LOG.info(this);
    }
  }
}
