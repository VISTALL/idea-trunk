package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.process.InterruptibleProcess;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.testFramework.AbstractVcsTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.FormParser;
import org.jetbrains.idea.perforce.perforce.PerforceChangeListHelper;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.PerforceProcessWaiter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author yole
 */
public abstract class PerforceTestCase extends AbstractVcsTestCase {
  private Process myP4dProcess;
  protected TempDirTestFixture myTempDirFixture;
  protected File myClientRoot;
  private int myP4dDelay;
  private LocalFileSystem.WatchRequest myWatchRequest;
  private PerforceSettings mySettings;

  @Before
  public void setUp() throws Exception {
    final String delayProp = System.getProperty("p4d.delay");
    if (delayProp != null) {
      myP4dDelay = Integer.parseInt(delayProp);
    }
    else {
      myP4dDelay = 500;
    }

    final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
    myTempDirFixture = fixtureFactory.createTempDirTestFixture();
    myTempDirFixture.setUp();

    final String p4dPath = PathManager.getHomePath() + "/svnPlugins/PerforceIntegration/testData/p4d/p4d.exe";
    myClientBinaryPath = new File(PathManager.getHomePath(), "/svnPlugins/PerforceIntegration/testData/p4d");

    final File p4dRoot = new File(myTempDirFixture.getTempDirPath(), "p4droot");
    p4dRoot.mkdir();
    myP4dProcess = new ProcessBuilder().command(p4dPath, "-p", "4666", "-r", p4dRoot.toString()).start();
    if (myP4dDelay > 0) {
      Thread.sleep(myP4dDelay);   // give p4d some time to initialize
    }

    try {
      myClientRoot = new File(myTempDirFixture.getTempDirPath(), "clientroot");
      myClientRoot.mkdir();

      setupClient();

      initProject(myClientRoot);
      myWatchRequest = LocalFileSystem.getInstance().addRootToWatch(new File(myTempDirFixture.getTempDirPath()).getCanonicalPath(), true);

      mySettings = PerforceSettings.getSettings(myProject);
      mySettings.useP4CONFIG = false;
      mySettings.pathToExec = new File(myClientBinaryPath, "p4.exe").getPath();
      mySettings.client = "test";
      mySettings.port = "4666";

      activateVCS("Perforce");
    }
    catch (Exception e) {
      stopPerforce(mySettings.pathToExec);
      myP4dProcess.destroy();
      throw e;
    }
  }

  private void stopPerforce(final String p4Path) throws IOException, InterruptedException {
    final Process process = new ProcessBuilder().command(p4Path, "-c", "test", "-p", "4666", "admin", "stop").start();
    try {
      final InterruptibleProcess ip = new InterruptibleProcess(process, myP4dDelay, TimeUnit.MILLISECONDS) {
        @Override
        protected int processTimeout() {
          return 0;
        }
      };
      final PerforceProcessWaiter waiter = new PerforceProcessWaiter();
      final int rc = waiter.execute(ip, myP4dDelay);
      final String out = waiter.getInStreamListener().toString();
      final String err = waiter.getErrStreamListener().toString();
      System.out.println("out = " + out + " err = " + err);
    }
    catch (ExecutionException e) {
      e.printStackTrace();
    }
    catch (TimeoutException e) {
      e.printStackTrace();
    } finally {
      process.destroy();
    }
  }

  @After
  public void tearDown() throws Exception {
    if (myP4dProcess != null) {
      stopPerforce(mySettings.pathToExec);
      myP4dProcess.destroy();
      if (myP4dDelay > 0) {
        Thread.sleep(myP4dDelay);   // give p4d some time to shutdown
      }
    }
    if (myWatchRequest != null) {
      LocalFileSystem.getInstance().removeWatchedRoot(myWatchRequest);
    }
    tearDownProject();
    if (myTempDirFixture != null) {
      myTempDirFixture.tearDown();
      myTempDirFixture = null;
    }
  }

  protected ProcessOutput runP4(String[] commandLine, @Nullable String stdin) throws IOException {
    final List<String> arguments = new ArrayList<String>();
    Collections.addAll(arguments, "-p", "4666");
    Collections.addAll(arguments, commandLine);
    return runClient("p4", stdin, null, arguments.toArray(new String[arguments.size()]));
  }

  protected ProcessOutput runP4WithClient(String... commandLine) throws IOException {
    List<String> arguments = new ArrayList<String>();
    arguments.add("-c");
    arguments.add("test");
    Collections.addAll(arguments, commandLine);
    return runP4(arguments.toArray(new String[arguments.size()]), null);
  }

  private String buildTestClientSpec() {
    StringBuilder builder = new StringBuilder();
    builder.append("Client:\ttest\r\n");
    builder.append("Root:\t").append(myClientRoot.toString()).append("\r\n");
    builder.append("View:\r\n\t//depot/... //test/...\r\n");
    return builder.toString();
  }

  private void setupClient() throws IOException {
    verify(runP4(new String[] { "client", "-i" }, buildTestClientSpec()));
  }

  protected void verifyOpened(final String path, final String changeType) throws IOException {
    ProcessOutput result = runP4WithClient("opened", new File(myClientRoot, path).toString());
    verify(result);
    final String stdout = result.getStdout();
    Assert.assertTrue("Unexpected 'p4 opened' result: " + stdout, StringUtil.startsWithConcatenationOf(stdout, "//depot/" + path + "#1 - ",
                                                                                                       changeType));
  }

  protected void submitFile(final String... depotPaths) throws IOException {
    StringBuilder submitSpec = new StringBuilder("Change:\tnew\r\n");
    submitSpec.append("Description:\r\n\ttest\r\n");
    submitSpec.append("Files:\r\n");
    for(String depotPath: depotPaths) {
      submitSpec.append("\t").append(depotPath).append("\r\n");
    }
    verify(runP4(new String[] { "-c", "test", "submit", "-i"}, submitSpec.toString()));
  }

  protected List<String> getFilesInDefaultChangelist() throws IOException {
    ProcessOutput result = runP4WithClient("change", "-o");
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.FILES);
    if (strings != null) {
      return strings;
    }
    return Collections.emptyList();
  }

  protected List<String> getFilesInList(final long number) throws IOException {
    ProcessOutput result = runP4WithClient("change", "-o", "" + number);
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.FILES);
    if (strings != null) {
      return strings;
    }
    return Collections.emptyList();
  }

  protected void editListDescription(final long number, final String description) throws IOException, VcsException {
    ProcessOutput result = runP4WithClient("change", "-o", "" + number);
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);

    final String specification =
      PerforceChangeListHelper.createSpecification(description, number, map.get(PerforceRunner.FILES), "test", "test", true);
    result = runP4(new String[] {"-c", "test", "change", "-i"}, specification);
    verify(result);
  }

  @Nullable
  protected String getListDescription(final long number) throws IOException {
    ProcessOutput result = runP4WithClient("change", "-o", "" + number);
    verify(result);
    final Map<String,List<String>> map = FormParser.execute(result.getStdout(), PerforceRunner.CHANGE_FORM_FIELDS);
    final List<String> strings = map.get(PerforceRunner.DESCRIPTION);
    if (strings != null && (! strings.isEmpty())) {
      final StringBuilder sb = new StringBuilder();
      for (String string : strings) {
        if (sb.length() != 0) {
          sb.append('\n');
        }
        sb.append(string);
      }
      return sb.toString();
    }
    return null;
  }

  protected void enableSilentOperation(final VcsConfiguration.StandardConfirmation op) {
    setStandardConfirmation("Perforce", op, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
  }
}
