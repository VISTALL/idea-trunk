/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.idea.perforce.perforce;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.SystemProperties;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.Convertor;
import com.intellij.util.text.SyncDateFormat;
import gnu.trove.TObjectLongHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.ClientVersion;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.actions.MessageManager;
import org.jetbrains.idea.perforce.application.ChangeListSynchronizer;
import org.jetbrains.idea.perforce.application.LocalPathsSet;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.annotation.AnnotationInfo;
import org.jetbrains.idea.perforce.changesBrowser.FileChange;
import org.jetbrains.idea.perforce.merge.BaseRevision;
import org.jetbrains.idea.perforce.perforce.commandWrappers.DeleteEmptyChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.jobs.JobsSearchSpecificator;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;
import org.jetbrains.idea.perforce.perforce.local.CollectionSplitter;
import org.jetbrains.idea.perforce.perforce.login.Notifier;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author yole
 */
public class PerforceRunner implements PerforceRunnerI {
  private final Project myProject;
  private final PerforceConnectionManager myConnectionManager;
  private final PerforceSettings mySettings;
  private final Notifier myNotifier;
  private final PerforceRunnerProxy myProxy;
  
  private static final int MAX_LOG_LENGTH = 1000000;
  private static final int OPENED_SIZE = 50;
  private static final int HAVE_SIZE = 30;

  @NonNls private static final String PASSWORD_INVALID_MESSAGE = "Perforce password (P4PASSWD) invalid or unset";
  @NonNls private static final String SESSION_EXPIRED_MESSAGE = "Your session has expired";
  @NonNls public static final String FILES_UP_TO_DATE = "file(s) up-to-date.";
  @NonNls private static final String PASSWORD_NOT_ALLOWED_MESSAGE = "Password not allowed at this server security level";
  @NonNls private static final String LOGGED_IN_MESSAGE = "logged in";
  @NonNls private static final String NO_SUCH_FILE_MESSAGE = "no such file(s)";
  @NonNls private static final String STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE = "file(s) not opened on this client";
  @NonNls private static final String YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE = "file(s) not opened for edit.";
  @NonNls private static final String NO_FILES_TO_RESOLVE_MESSAGE = "no file(s) to resolve";
  @NonNls private static final String MERGING_MESSAGE = "- merging";
  @NonNls private static final String USING_BASE_MESSAGE = "using base";
  @NonNls private static final String MERGING2_MESSAGE = " - merging //";
  @NonNls public static final String NOT_UNDER_CLIENT_ROOT_MESSAGE = "is not under client's root";
  @NonNls public static final String NOT_IN_CLIENT_VIEW_MESSAGE = "file(s) not in client view";
  @NonNls public static final String NOT_ON_CLIENT_MESSAGE = "file(s) not on client";
  @NonNls private static final String REVERTED_MESSAGE = "reverted";
  @NonNls private static final String NO_FILES_RESOLVED_MESSAGE = "no file(s) resolved";
  @NonNls private static final String INVALID_REVISION_NUMBER = "Invalid revision number";

  @NonNls public static final String CHANGE = "Change:";
  @NonNls public static final String DATE = "Date:";
  @NonNls public static final String CLIENT = "Client:";
  @NonNls public static final String USER = "User:";
  @NonNls public static final String STATUS = "Status:";
  @NonNls public static final String DESCRIPTION = "Description:";
  @NonNls public static final String JOB = "Job:";
  @NonNls public static final String JOBS = "Jobs:";
  @NonNls public static final String FILES = "Files:";
  @NonNls public static final String OWNER = "Owner:";
  @NonNls public static final String VIEW = "View:";
  @NonNls private static final String AFFECTED_FILES = "Affected files";
  @NonNls private static final String JOBS_FIXED = "Jobs fixed";

  @NonNls public static final String CLIENTSPEC_ROOT = "Root:";
  @NonNls public static final String CLIENTSPEC_ALTROOTS = "AltRoots:";

  @NonNls public static final String USER_NAME = "User name:";
  @NonNls public static final String CLIENT_NAME = "Client name:";
  @NonNls public static final String CLIENT_HOST = "Client host:";
  @NonNls public static final String CLIENT_ROOT = "Client root:";
  @NonNls public static final String CURRENT_DIRECTORY = "Current directory:";
  @NonNls public static final String CLIENT_ADDRESS = "Client address:";
  @NonNls public static final String SERVER_ADDRESS = "Server address:";
  @NonNls public static final String SERVER_ROOT = "Server root:";
  @NonNls public static final String SERVER_DATE = "Server date:";
  @NonNls public static final String SERVER_LICENSE = "Server license:";
  @NonNls public static final String SERVER_VERSION = "Server version:";
  @NonNls public static final String EMAIL = "Email:";
  @NonNls public static final String UPDATE = "Update:";
  @NonNls public static final String ACCESS = "Access:";
  @NonNls public static final String FULL_NAME = "FullName:";
  @NonNls public static final String JOB_VIEW = "JobView:";
  @NonNls public static final String PASSWORD = "Password:";
  @NonNls public static final String REVIEWS = "Reviews:";

  @NonNls private static final SyncDateFormat DATESPEC_DATE_FORMAT = new SyncDateFormat(new SimpleDateFormat("yyyy/MM/dd:HH:mm:ss", Locale.US));
  @NonNls private static final String NOW = "now";
  @NonNls private static final String DEFAULT_CHANGELIST_NUMBER = "default";

  @NonNls public static final String CLIENT_FILE_PREFIX = "... clientFile ";

  public static final String[] USER_FORM_FIELDS = new String[] {USER, EMAIL, UPDATE, ACCESS, FULL_NAME, JOB_VIEW, PASSWORD, REVIEWS};

  public static final String[] CHANGE_FORM_FIELDS = new String[]{CHANGE,
    DATE,
    CLIENT,
    USER,
    STATUS,
    DESCRIPTION,
    JOBS,
    FILES};

  private static final String[] AVAILABLE_INFO = new String[]{USER_NAME,
    CLIENT_NAME,
    CLIENT_HOST,
    CLIENT_ROOT,
    CURRENT_DIRECTORY,
    CLIENT_ADDRESS,
    SERVER_ADDRESS,
    SERVER_ROOT,
    SERVER_DATE,
    SERVER_LICENSE,
    SERVER_VERSION};

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.PerforceRunner");
  private static final Logger SPECIFICATION_LOG = Logger.getInstance("#PerforceJobSpecificationLogging");
  @NonNls private static final String DEFAULT_DESCRIPTION = "<none>";
  @NonNls private static final String DUMP_FILE_NAME = "p4.output";
  
  private static final String CLIENT_VERSION_REV = "Rev.";

  private PerforceManager myPerforceManager;
  private final RunnerForCommands myMeAsRunner;

  public static PerforceRunner getInstance(Project project) {
    return ServiceManager.getService(project, PerforceRunner.class);
  }

  public PerforceRunner(final Project project, final PerforceConnectionManager connectionManager, final PerforceSettings settings,
                        final PerforceManager perforceManager) {
    myProject = project;
    myConnectionManager = connectionManager;
    mySettings = settings;
    myPerforceManager = perforceManager;
    myNotifier = myPerforceManager.getLoginNotifier();
    myProxy = new PerforceRunnerProxy(project, this);
    myMeAsRunner = new RunnerForCommands() {
      public ExecResult executeP4Command(@NonNls String[] p4args,
                                         P4Connection connection,
                                         @Nullable StringBuffer inputStream,
                                         boolean justLogged) {
        return PerforceRunner.this.executeP4Command(p4args, connection, inputStream, justLogged);
      }

      public void checkError(ExecResult result) throws VcsException {
        PerforceRunner.this.checkErrors(result);
      }
    };
  }

  public PerforceRunnerI getProxy() {
    return myProxy.getProxy();
  }

  public Map<String, List<String>> getInfo(final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"info"};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult);
    return FormParser.execute(execResult.getStdout(), AVAILABLE_INFO);
  }

  @Nullable
  public String getClient(final P4Connection connection) throws VcsException {
    List<String> clientNames = getInfo(connection).get(CLIENT_NAME);
    if (clientNames == null) {
      return null;
    }
    else {
      return clientNames.get(0);
    }
  }


  public void edit(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    long changeListNumber = ChangeListSynchronizer.getInstance(myProject).getActiveChangeListNumber(connection);
    edit(file, changeListNumber);
  }

  public void edit(final P4File file, final long changeListNumber) throws VcsException {
    file.invalidateFstat();

    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    final CommandArguments arguments = CommandArguments.createOn(P4Command.edit);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append(file.getRecursivePath());

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
  }

  private static void appendChangeListNumber(long changeListNumber, CommandArguments arguments) {
    if (changeListNumber > 0) {
      arguments.append("-c").append(String.valueOf(changeListNumber));
    }
  }

  public FStat fstat(final P4File p4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);

    final FStat retVal = new FStat();
    @NonNls final String[] p4args = {"fstat",
      p4File.getEscapedPath()};
    ExecResult execResult = executeP4Command(p4args, connection);
    String stderr = execResult.getStderr();
    if (stderr.contains(NO_SUCH_FILE_MESSAGE)) {
      retVal.status = FStat.STATUS_NOT_ADDED;
      return retVal;
    }
    checkError(execResult);

    retVal.parseFStat(execResult.getStdout());

    return retVal;
  }

  public void revert(final P4File p4File, final boolean justTry) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.revert);
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);

    final P4WhereResult p4WhereResult = where(p4File, connection);
    if (p4WhereResult != null && !StringUtil.isEmpty(p4WhereResult.getDepot())) {
      arguments.append(p4WhereResult.getDepot());
    }
    else {
      arguments.append(p4File.getAnyPath());
    }

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    if (!justTry) {
      checkError(execResult);
    }
  }

  public boolean revertUnchanged(final P4File file) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    if (file.isDirectory()) {
      String path = file.getEscapedPath() + "/...";
      return doRevertUnchanged(connection, path);
    }
    return doRevertUnchanged(connection, file.getEscapedPath());
  }

  private boolean doRevertUnchanged(final P4Connection connection, final String pattern) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{"revert", "-a", pattern}, connection);
    if (!execResult.getStderr().contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) &&
        !execResult.getStderr().contains(YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      checkError(execResult);
    }
    return execResult.getStdout().contains(REVERTED_MESSAGE);
  }

  public void revertUnchanged(final P4Connection connection, final long changeListNumber) throws VcsException {
    String changeListNum = changeListNumber == -1L ? DEFAULT_CHANGELIST_NUMBER : String.valueOf(changeListNumber);
    final ExecResult execResult = executeP4Command(new String[] { "revert", "-a", "-c", changeListNum }, connection);
    if (!execResult.getStderr().contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) &&
        !execResult.getStderr().contains(YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      checkError(execResult);
    }
  }

  public void add(final P4File p4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    long changeListNumber = ChangeListSynchronizer.getInstance(myProject).getActiveChangeListNumber(connection);
    add(p4File, changeListNumber);
  }

  public void add(final P4File p4File, final long changeListNumber) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.add);
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append(p4File.getAnyPath());
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
  }

  public ExecResult sync(final P4File p4File, boolean forceSync) {
    return doSync(p4File, forceSync ? "-f" : null);
  }

  public ExecResult previewSync(final P4File p4File) {
    return doSync(p4File, "-n");
  }

  private ExecResult doSync(final P4File p4File, @Nullable @NonNls final String arg) {
    p4File.invalidateFstat();
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    final CommandArguments arguments = CommandArguments.createOn(P4Command.sync);
    if (arg != null) {
      arguments.append(arg);
    }
    final String path = p4File.getAnyPath();
    if (p4File.isDirectory()) {
      arguments.append(path + "/...");
    }
    else {
      arguments.append(path);
    }

    return executeP4Command(arguments.getArguments(), connection);
  }

  // todo ? not sure for move+add/delete cases
  public void assureDel(final P4File p4File, @Nullable final Long changeList) throws VcsException {
    // reverting the edit of a file at the old path in a renamed directory will recreate both the file
    // and its parent dir => need to delete both
    final List<File> filesToDelete = new ArrayList<File>();
    File f = new File(p4File.getLocalPath());
    while(f != null && !f.exists()) {
      filesToDelete.add(f);
      f = f.getParentFile();
    }

    // can't use cached fstat because we can just have performed some operation affecting the status of the file
    final FStat fstat = p4File.getFstat(myProject, true);

    if (fstat.status == FStat.STATUS_NOT_ADDED || fstat.status == FStat.STATUS_NOT_IN_CLIENTSPEC || fstat.local == FStat.LOCAL_DELETING ||
      fstat.local == FStat.LOCAL_MOVE_DELETING) {
      // this is OK, that's what we want
    }
    else {
      // we have to do something about it

      // first, if revert is enough
      if (fstat.local == FStat.LOCAL_ADDING || fstat.local == FStat.LOCAL_MOVE_ADDING) {
        revert(p4File, false);
      }
      else if (fstat.local == FStat.LOCAL_CHECKED_IN) {
        if (changeList == null) {
          delete(p4File);
        }
        else {
          delete(p4File, changeList.longValue());
        }
      }
      else {
        revert(p4File, false);
        try {
          if (changeList == null) {
            delete(p4File);
          }
          else {
            delete(p4File, changeList.longValue());
          }
        }
        catch (Exception ex) {
          // TODO: now we ignore, but we should really check the status after revert
        }
      }
    }

    // remove file
    for(File file: filesToDelete) {
      if (file.exists()) {
        final boolean res = file.delete();
        if (!res) {
          throw new VcsException(PerforceBundle.message("exception.text.cannot.delete.local.file", file));
        }
      }
    }
  }

  private void delete(final P4File p4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    long changeListNumber = ChangeListSynchronizer.getInstance(myProject).getActiveChangeListNumber(connection);
    delete(p4File, changeListNumber);
  }

  private void delete(final P4File p4File, final long changeListNumber) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.delete);
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append(p4File.getAnyPath());
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
  }

  public void integrate(final P4File oldP4File,final P4File newP4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(oldP4File);
    long changeListNumber = ChangeListSynchronizer.getInstance(myProject).getActiveChangeListNumber(connection);
    integrate(oldP4File, newP4File, changeListNumber);
  }

  public void integrate(final P4File oldP4File, final P4File newP4File, final long changeListNumber) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(oldP4File);
    oldP4File.invalidateFstat();
    newP4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.integrate);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append("-d");
    arguments.append(oldP4File.getAnyPath()).append(newP4File.getAnyPath());

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
  }

  public void reopen(final File[] selectedFiles, final long changeListNumber) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.reopen);
    arguments.append("-c");
    if (changeListNumber > 0) {
      arguments.append(String.valueOf(changeListNumber));
    }
    else {
      arguments.append("default");
    }

    Map<P4Connection, List<File>> connectionToFile = mySettings.chooseConnectionForFile(Arrays.asList(selectedFiles));

    executeFileCommands(arguments, connectionToFile);
  }

  private void executeFileCommands(final CommandArguments arguments,
                                   final Map<P4Connection, List<File>> connectionToFile) throws VcsException {
    for (P4Connection connection : connectionToFile.keySet()) {
      final List<File> files = connectionToFile.get(connection);
      CommandArguments connectionArguments = arguments.createCopy();
      for (File selectedFile : files) {
        connectionArguments.append(selectedFile.getPath());
      }
      final ExecResult execResult = executeP4Command(connectionArguments.getArguments(), connection);
      checkError(execResult);
    }
  }

  public void reopen(P4Connection connection, List<PerforceChange> paths, long targetChangeListNumber) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.reopen);
    arguments.append("-c");
    if (targetChangeListNumber > 0) {
      arguments.append(String.valueOf(targetChangeListNumber));
    }
    else {
      arguments.append("default");
    }

    for (PerforceChange change : paths) {
      arguments.append(P4File.escapeWildcards(change.getDepotPath()));
    }

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
  }

  public List<String> getClients(P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{P4Command.clients.getName()}, connection);
    checkError(execResult);
    try {
      return OutputMessageParser.processClientsOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> getUsers(P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{P4Command.users.getName()}, connection);
    checkError(execResult);
    try {
      return OutputMessageParser.processUsersOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  @Nullable
  public String getUserJobView(final P4Connection connection, final String userName) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{P4Command.user.getName(), "-o"}, connection);
    checkError(execResult);
    final Map<String, List<String>> map = FormParser.execute(execResult.getStdout(), USER_FORM_FIELDS);
    final List<String> list = map.get(JOB_VIEW);
    if (list == null || list.isEmpty()) return null;
    if (list.size() == 1) return list.get(0);
    
    final StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append(s);
    }
    return sb.toString();
  }

  /**
   * @param vcsRoot can be null only for committed list!!!
   */
  public List<PerforceChange> getChanges(P4Connection connection, final long changeListNumber, @Nullable final VirtualFile vcsRoot) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    return getChanges(connection, changeListNumber, client, vcsRoot);
  }

  public List<PerforceChange> getChanges(final P4Connection connection, final long changeListNumber, final PerforceClient client,
                                         final VirtualFile vcsRoot) throws VcsException {
    final Convertor<String, Boolean> rootChecker;
    if (vcsRoot == null) {
      rootChecker = new Convertor<String, Boolean>() {
        public Boolean convert(String o) {
          return true;
        }
      };
    } else {
      final String rootPath = FileUtil.toSystemDependentName(myPerforceManager.convertP4ParsedPath(null, vcsRoot.getPath()));

      rootChecker = new Convertor<String, Boolean>() {
        public Boolean convert(final String path) {
          return path.startsWith(rootPath);
        }
      };
    }

    final List<PerforceChange> result = new ArrayList<PerforceChange>();

    if (changeListNumber == -1) {
      // 'p4 describe' doesn't work for the default changelist
      Map<String, List<String>> form = getChangeSpec(connection, changeListNumber);
      List<String> strings = form.get(FILES);
      if (strings != null) {
        for (String s : strings) {
          final PerforceChange change = PerforceChange.createOn(s, client, connection, changeListNumber, getDescription(form));
          if ((change != null) && (Boolean.TRUE.equals(rootChecker.convert(change.getFile().getAbsolutePath())))) {
            result.add(change);
          }
        }
      }
    }
    else {
      CommandArguments args = CommandArguments.createOn(P4Command.describe);
      args.append("-s");
      args.append(changeListNumber);
      final ExecResult execResult = executeP4Command(args.getArguments(), connection);
      checkError(execResult);

      final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
      if (progressIndicator != null && progressIndicator.isCanceled()) {
        throw new ProcessCanceledException();
      }

      final String stdout = execResult.getStdout();
      String description = extractDescription(stdout);

      List<PerforceAbstractChange> changes;
      try {
        changes = OutputMessageParser.processDescriptionOutput(stdout);
      }
      catch (IOException e) {
        throw new VcsException(e);
      }
      for(PerforceAbstractChange change: changes) {
        FileChange fileChange = (FileChange) change;
        final File localFile = PerforceManager.getFileByDepotName(fileChange.getDepotPath(), client);
        if ((localFile != null) && (rootChecker.convert(localFile.getAbsolutePath()))) {
          PerforceChange p4Change = new PerforceChange(fileChange.getType(), localFile, fileChange.getDepotPath(),
                                                       fileChange.getRevisionAfter(), connection, changeListNumber, description);
          result.add(p4Change);
        }
      }
    }

    return result;
  }

  private static String extractDescription(final String stdout) {
    /*
Change 136389 by yole@unit-056 on 2006/09/17 14:04:32 *pending*

        multiple
        line
        description
[Jobs fixed ...]
Affected files ...
     */
    String[] lines = LineTokenizer.tokenize(stdout, true);
    StringBuilder result = new StringBuilder();
    for(int i=2; i<lines.length-1; i++) {
      // "Affected files" is preceded by an empty line
      if (lines [i+1].startsWith(AFFECTED_FILES) || lines [i+1].startsWith(JOBS_FIXED)) {
        break;
      }
      result.append(lines [i].trim()).append("\n");
    }
    return result.toString();
  }

  private Map<String, List<String>> getChangeSpec(P4Connection connection, long changeListNumber) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.change);
    arguments.append("-o");
    if (changeListNumber > 0) {
      arguments.append(String.valueOf(changeListNumber));
    }
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);

    return FormParser.execute(execResult.getStdout(), CHANGE_FORM_FIELDS);
  }

  public PerforceChangeList describe(final P4Connection connection, final long number) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.describe);
    args.append("-s").append(number);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult);
    final ChangeListData changeListData;
    try {
      changeListData = OutputMessageParser.loadChangeListDescription(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
    return new PerforceChangeList(changeListData, myProject, connection);
  }

  public List<PerforceChangeList> getPendingChangeLists(final P4Connection connection) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    return getPendingChangeLists(connection, client);
  }

  public List<PerforceChange> getChangesUnder(final P4Connection connection, @NotNull final VirtualFile root) throws VcsException {
      final List<PerforceChangeList> list = getPendingChangeListsUnderRoot(connection, root);
      final List<PerforceChange> result = new LinkedList<PerforceChange>();

    final List<PerforceChange> perforceChanges = new LinkedList<PerforceChange>();
    perforceChanges.addAll(getChanges(connection, -1, root));
    for (PerforceChangeList changeList : list) {
      final List<PerforceChange> changes = getChanges(connection, changeList.getNumber(), root);
      perforceChanges.addAll(changes);
    }

    final File ioRoot = new File(root.getPath());

    final PathsHelper pathsHelper = new PathsHelper(myPerforceManager);
    for (PerforceChange perforceChange : perforceChanges) {
      try {
        if ((perforceChange.getFile() != null) && FileUtil.isAncestor(ioRoot, perforceChange.getFile(), false)) {
          result.add(perforceChange);
          pathsHelper.add(new FilePathImpl(perforceChange.getFile(), false));
        }
      }
      catch (IOException e) {
        throw new VcsException(e);
      }
    }

    final TObjectLongHashMap<String> haveRevisions = new TObjectLongHashMap<String>();
    haveMultiple(pathsHelper, connection, new MyWithRevisionHaveParser(haveRevisions));

    for (PerforceChange change : perforceChanges) {
      final String path = change.getFile().getAbsolutePath();
      final long revision = haveRevisions.get(FileUtil.toSystemDependentName(path));
      if (revision != 0) {
        change.setRevision(revision);
      }
    }
      return result;
  }

  public List<PerforceChangeList> getPendingChangeListsUnderRoot(final P4Connection connection, final VirtualFile root) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    final CommandArguments args = CommandArguments.createOn(P4Command.changes);
    args.append("-i");
    appendTArg(args, connection);
    args.append("-l")
      .append("-s").append("pending");

    appendUserName(client, args);
    appendClientName(client, args);
    args.append(P4File.create(root).getRecursivePath());
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult);
    return parsePerforceChangeLists(execResult.getStdout(), connection);
  }

  private List<PerforceChangeList> getPendingChangeLists(final P4Connection connection, final PerforceClient client) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.changes);
    args.append("-i");
    appendTArg(args, connection);
    args.append("-l")
      .append("-s").append("pending");

    appendUserName(client, args);
    appendClientName(client, args);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult);
    return parsePerforceChangeLists(execResult.getStdout(), connection);
  }


  private void appendTArg(final CommandArguments arguments, final P4Connection connection) {
    if (mySettings.getServerVersion(connection) >= 2003) {
      arguments.append("-t");
    }
  }

  private static CommandArguments appendUserName(final PerforceClient client, CommandArguments args) {
    final String userName = client.getUserName();
    if (userName != null) {
      return args.append("-u").append(userName);
    }
    else {
      return args;
    }
  }

  private static CommandArguments appendClientName(final PerforceClient client, CommandArguments args) {
    final String userName = client.getName();
    if (userName != null) {
      return args.append("-c").append(userName);
    }
    else {
      return args;
    }
  }

  private List<PerforceChangeList> parsePerforceChangeLists(final String stdout, P4Connection connection) throws VcsException {
    try {
      final List<ChangeListData> datas = OutputMessageParser.processChangesOutput(stdout);
      final ArrayList<PerforceChangeList> result = new ArrayList<PerforceChangeList>();
      for (ChangeListData data : datas) {
        result.add(new PerforceChangeList(data, myProject, connection));
      }
      return result;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public DeleteEmptyChangeList deleteEmptyChangeList(final P4Connection connection, final long number) {
    return new DeleteEmptyChangeList(myMeAsRunner, connection, number);
  }

  private final static String HAS_OPEN_FILES_CANT_BE_DELETED = "open file(s) associated with it and can't be deleted";
  public void deleteEmptyChangeList(final P4Connection connection, long listNumber, final boolean allowNotEmptyFailure) throws VcsException {
    if (listNumber > 0) {
      final CommandArguments arguments = CommandArguments.createOn(P4Command.change);
      arguments.append("-d");
      arguments.append(String.valueOf(listNumber));
      final ExecResult result = executeP4Command(arguments.getArguments(), connection);
      if ((result.getExitCode() != 0) && allowNotEmptyFailure) {
        final String err = result.getStderr();
        // do not throw exception
        if (err.contains(HAS_OPEN_FILES_CANT_BE_DELETED)) return;
      }
      checkError(result);
    }
  }

  private void adjustJobs(final P4Connection connection, final Map<String, List<String>> changeForm, final List<PerforceJob> p4jobs) {
    changeForm.remove(JOBS);
    if (p4jobs != null && (! p4jobs.isEmpty())) {
      final List<String> values = new ArrayList<String>();
      for (PerforceJob p4job : p4jobs) {
        if (connection.getId().equals(p4job.getConnection().getId())) {
          values.add(p4job.getName());
        }
      }
      changeForm.put(JOBS, values);
    }
  }

  public void submitForConnection(final P4Connection connection,
                                  final List<PerforceChange> changesForConnection,
                                  final long changeListNumber,
                                  final String preparedComment, final List<PerforceJob> p4jobs) throws VcsException {
    List<String> excludedChanges = new ArrayList<String>();
    Map<String, List<String>> changeForm = getChangeSpec(connection, changeListNumber);
    String originalDescription = getDescription(changeForm);
    adjustJobs(connection, changeForm, p4jobs);
    final StringBuffer changeData = createChangeData(preparedComment, changesForConnection, changeForm, excludedChanges);
    long newNumber = -1;
    if (changeListNumber == -1) {
      final CommandArguments arguments = CommandArguments.createOn(P4Command.submit);
      appendChangeListNumber(changeListNumber, arguments);
      arguments.append("-i");
      final ExecResult execResult = executeP4Command(arguments.getArguments(), connection, changeData);
      checkError(execResult);
    }
    else {
      CommandArguments arguments = CommandArguments.createOn(P4Command.change);
      arguments.append("-i");
      checkError(connection.runP4CommandLine(mySettings, arguments.getArguments(), changeData));
      arguments = CommandArguments.createOn(P4Command.submit);
      appendChangeListNumber(changeListNumber, arguments);
      checkError(executeP4Command(arguments.getArguments(), connection));

      if (!excludedChanges.isEmpty()) {
        LOG.debug("Reopening excluded changes in new changelist");
        newNumber = createChangeList(originalDescription, connection, excludedChanges);
      }
    }
    PerforceSettings.getSettings(myProject).notifyChangeListSubmitted(connection, changeListNumber, newNumber);
  }

  public long createChangeList(String description, final P4Connection connection, @Nullable final List<String> files)
    throws VcsException {
    final String changeListForm = createChangeListForm(description, -1, connection, files, myPerforceManager.getClient(connection));
    if (mySettings.showCmds()) {
      logMessage(changeListForm);
    }
    final ExecResult execResult = executeP4Command(new String[]{P4Command.change.getName(), "-i"}, connection, new StringBuffer(changeListForm));
    checkError(execResult);
    return PerforceChangeListHelper.parseCreatedListNumber(execResult.getStdout());
  }

  public void renameChangeList(long number, String description, P4Connection connection) throws VcsException {
    final Map<String, List<String>> oldSpec = getChangeSpec(connection, number);
    oldSpec.put(DESCRIPTION, processDescription(description));
    final ExecResult execResult = executeP4Command(new String[]{P4Command.change.getName(), "-i"}, connection, createStringFormRepresentation(oldSpec));
    checkError(execResult);
  }

  public static List<String> processDescription(final String description) {
    final List<String> result = new ArrayList<String>();
    final String[] lines = StringUtil.convertLineSeparators(description).split("\n");
    if (lines.length == 0) {
      result.add(DEFAULT_DESCRIPTION);
    } else {
      for(String line: lines) {
        result.add(line);
      }
    }
    return result;
  }

  private String createChangeListForm(final String description,
                                      final long changeListNumber, final P4Connection connection,
                                      @Nullable final List<String> files, final PerforceClient client) throws VcsException {
    @NonNls final StringBuilder result = new StringBuilder();
    result.append("Change:\t");
    if (changeListNumber == -1) {
      result.append("new");
    }
    else {
      result.append(changeListNumber);
    }
    result.append("\n\nClient:\t");
    result.append(client.getName());
    result.append("\n\nUser:\t");
    result.append(client.getUserName());
    result.append("\n\nStatus:\t");
    if (changeListNumber == -1) {
      result.append("new");
    }
    else {
      result.append("pending");
    }
    result.append("\n\nDescription:");
    final List<String> descriptionLines = processDescription(description);
    for (String line : descriptionLines) {
      result.append("\n\t").append(line);
    }
    if (changeListNumber != -1 || files != null) {
      result.append("\n\nFiles:\n");
      if (files != null) {
        for(String file: files) {
          result.append("\t").append(file).append("\n");
        }
      }
      else {
        final List<PerforceChange> list = openedInList(connection, changeListNumber);
        for(PerforceChange openedFile: list) {
          result.append("\t").append(openedFile.getDepotPath()).append("\n");
        }
      }
    }

    return result.toString();
  }

  public List<PerforceChangeList> getSubmittedChangeLists(String client, String user, final P4File rootP4File,
                                                          Date after, Date before, Long afterChange, Long beforeChange,
                                                          final int maxCount) throws VcsException {
    return getSubmittedChangeLists(client, user, rootP4File, after, before, afterChange, beforeChange, maxCount, mySettings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES);
  }

  public List<PerforceChangeList> getSubmittedChangeLists(String client, String user, final P4File rootP4File,
                                                          Date after, Date before, Long afterChange, Long beforeChange,
                                                          final int maxCount, final boolean showIntegrated) throws VcsException {
    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(rootP4File);
    final CommandArguments arguments = CommandArguments.createOn(P4Command.changes);
    arguments.append("-s").append("submitted");
    if (showIntegrated) {
      arguments.append("-i");
    }
    appendTArg(arguments, connection);
    arguments.append("-l");

    if (client != null && client.length() > 0) {
      arguments.append("-c").append(client);
    }
    if (user != null && user.length() > 0) {
      arguments.append("-u").append(user);
    }
    if (maxCount > 0) {
      arguments.append("-m").append(maxCount);
    }

    if (rootP4File != null) {
      arguments.append(rootP4File.getRecursivePath() + dateSpec(after, before, afterChange, beforeChange));
    }
    else {
      arguments.append("//..." + dateSpec(after, before, afterChange, beforeChange));
    }

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);
    return parsePerforceChangeLists(execResult.getStdout(), connection);
  }

  private static String dateSpec(final Date after, final Date before, final Long afterChange, final Long beforeChange) {

    if (after == null && before == null && afterChange == null && beforeChange == null) {
      return "";
    }

    final StringBuilder result = new StringBuilder();
    result.append('@');
    if (after != null) {
      result.append(DATESPEC_DATE_FORMAT.format(after));
    }
    else if (afterChange != null) {
      result.append(afterChange.longValue());
    }
    else {
      result.append(DATESPEC_DATE_FORMAT.format(new Date(0)));
    }
    result.append(',');
    result.append('@');
    if (before != null) {
      result.append(DATESPEC_DATE_FORMAT.format(before));
    }
    else if (beforeChange != null) {
      result.append(beforeChange.longValue());
    }
    else {
      result.append(NOW);
    }

    return result.toString();
  }

  public List<PerforceChange> openedInList(final P4Connection connection, final long number) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    args.append("-c").append(number);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getStderr().toLowerCase().contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      // no files opened
      return new ArrayList<PerforceChange>();
    }
    checkError(execResult);
    try {
      return PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<PerforceChange> openedUnderRoot(final P4Connection connection, final VirtualFile root) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    args.append(P4File.create(root).getEscapedPath() + "/...");
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final String err = execResult.getStderr().toLowerCase();
    if (err.contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) ||
        err.contains(NOT_UNDER_CLIENT_ROOT_MESSAGE) || err.contains(NOT_IN_CLIENT_VIEW_MESSAGE)) {
      // no files opened
      return new ArrayList<PerforceChange>();
    }
    checkError(execResult);
    try {
      return PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<PerforceChange> opened(final P4Connection connection, final Collection<FilePath> paths) throws VcsException {
    if (paths.size() > OPENED_SIZE) {
      final CollectionSplitter<FilePath> splitter = new CollectionSplitter<FilePath>(OPENED_SIZE);
      final List<List<FilePath>> collOfColl = splitter.split(paths);

      final List<PerforceChange> result = new ArrayList<PerforceChange>();
      for (Collection<FilePath> filePaths : collOfColl) {
        result.addAll(openedImpl(connection, filePaths));
      }
      return result;
    }
    return openedImpl(connection, paths);
  }

  private List<PerforceChange> openedImpl(final P4Connection connection, final Collection<FilePath> paths) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    for (FilePath path : paths) {
      args.append(P4File.create(path).getEscapedPath());
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final String err = execResult.getStderr().toLowerCase();
    if (err.contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) ||
        err.contains(NOT_UNDER_CLIENT_ROOT_MESSAGE) || err.contains(NOT_IN_CLIENT_VIEW_MESSAGE)) {
      // no files opened
      return new ArrayList<PerforceChange>();
    }
    checkError(execResult);
    try {
      return PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  @Nullable
  public PerforceChange opened(P4File file) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    args.append(file.getEscapedPath());
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final String err = execResult.getStderr().toLowerCase();
    if (err.contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) ||
        err.contains(NOT_UNDER_CLIENT_ROOT_MESSAGE) || err.contains(NOT_IN_CLIENT_VIEW_MESSAGE)) {
      // no files opened
      return null;
    }
    checkError(execResult);
    try {
      final List<PerforceChange> list = PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
      if (list.isEmpty()) {
        return null;
      }
      return list.get(0);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }


  private static String getDescription(final Map<String, List<String>> changeForm) {
    final List<String> strings = changeForm.get(DESCRIPTION);
    if (strings == null) return "";
    return StringUtil.join(strings, "\n");
  }

  private static StringBuffer createChangeData(String preparedComment,
                                               List<PerforceChange> actualChanges,
                                               Map<String, List<String>> changeForm,
                                               List<String> excludedChanges) {
    setDescriptionToForm(changeForm, preparedComment);

    List<String> changes = changeForm.get(FILES);

    if (changes != null) {
      String[] changesArray = ArrayUtil.toStringArray(changes);

      for (String changeString : changesArray) {
        String depotPath = PerforceChange.getDepotPath(changeString);
        if (findChangeByDepotPath(actualChanges, depotPath) == null) {
          changes.remove(changeString);
          excludedChanges.add(changeString);
        }
      }
    }

    return createStringFormRepresentation(changeForm);

  }

  public static void setDescriptionToForm(Map<String, List<String>> changeForm, String preparedComment) {
    List<String> description = changeForm.get(DESCRIPTION);
    if (description != null) {
      description.clear();
      description.addAll(getAllLines(preparedComment));
    }
    else {
      changeForm.put(DESCRIPTION, getAllLines(preparedComment));
    }
  }

  private static List<String> getAllLines(String preparedComment) {
    List<String> result = new ArrayList<String>();
    String[] lines = LineTokenizer.tokenize(preparedComment, false);
    for (String line1 : lines) {
      String line = line1.trim();
      if (line.length() > 0) {
        result.add(line);
      }
    }

    return result;
  }

  @Nullable
  private static PerforceChange findChangeByDepotPath(List<PerforceChange> actualChanges, String depotPath) {
    for (PerforceChange change : actualChanges) {
      if (change.getDepotPath().equals(depotPath)) {
        return change;
      }
    }
    return null;
  }

  public static StringBuffer createStringFormRepresentation(Map<String, List<String>> changeForm) {
    StringBuffer result = new StringBuffer();
    for (String field : changeForm.keySet()) {
      result.append("\n");
      result.append(field);

      List<String> values = changeForm.get(field);
      result.append("\n");
      for (String value : values) {
        result.append("\t");
        result.append(value);
        result.append("\n");
      }
    }

    return result;
  }

  public List<String> getBranches(final P4Connection connection) throws VcsException {
    final ExecResult result = executeP4Command(new String[]{"branches"}, connection);
    checkError(result);
    try {
      return OutputMessageParser.processBranchesOutput(result.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public BranchSpec loadBranchSpec(final String branchName, final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"branch",
      "-o",
      branchName};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult);
    final Map<String, List<String>> branchSpecForm = FormParser.execute(execResult.getStdout(), new String[]{OWNER,
      DESCRIPTION,
      VIEW});
    return new BranchSpec(branchSpecForm);
  }

  public Map<String, List<String>> loadClient(final String clientName, final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"client",
      "-o",
      clientName};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult);

    return FormParser.execute(execResult.getStdout(), new String[]{"Client:",
      "Owner:",
      "Update:",
      "Access:",
      "Host:",
      "Description:",
      CLIENTSPEC_ROOT,
      CLIENTSPEC_ALTROOTS,
      "Options:",
      "LineEnd:",
      VIEW});
  }

  public byte[] getByteContent(final P4File file,
                               @Nullable final String revisionNumber) throws VcsException {
    return getByteContent(file.getAnyPath(), revisionNumber, myConnectionManager.getConnectionForFile(file));
  }

  public byte[] getByteContent(final String depotPath,
                               @Nullable final String revisionNumber,
                               P4Connection connection) throws VcsException {

    @NonNls final String[] p4args = revisionNumber != null
                                    ? new String[]{"print",
      "-q",
      depotPath + "#" + revisionNumber}
                                    : new String[]{"print",
                                      "-q",
                                      depotPath};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult);

    return execResult.getByteOut();
  }

  public String getContent(final P4File file,
                           final long revisionNumber) throws VcsException {
    return getContent(file.getAnyPath(), revisionNumber, myConnectionManager.getConnectionForFile(file));
  }

  public String getContent(final String depotPath,
                           final long revisionNumber,
                           final P4Connection connection) throws VcsException {

    @NonNls final String[] p4args = revisionNumber >= 0
                                    ? new String[]{"print",
      "-q",
      depotPath + "#" + revisionNumber}
                                    : new String[]{"print",
                                      "-q",
                                      depotPath};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult);

    byte[] byteOut = execResult.getByteOut();
    final String result = CharsetToolkit.bytesToString(byteOut);
    return StringUtil.convertLineSeparators(result, SystemProperties.getLineSeparator());
  }

  public List<VirtualFile> getResolvedWithConflicts(P4File file) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    final ExecResult execResult = executeP4Command(new String[]{"resolve", "-n", file.getRecursivePath() }, connection);
    if (execResult.getStderr().toLowerCase().contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return Collections.emptyList();
    }

    return getFilesFromOutput(execResult.getStdout(), MERGING2_MESSAGE);
  }

  public LocalPathsSet getResolvedWithConflictsMap(final P4Connection connection, @Nullable final VirtualFile root) throws VcsException {
    final LocalPathsSet result = new LocalPathsSet();
    final CommandArguments args = CommandArguments.createOn(P4Command.resolve);
    args.append("-n");
    if (root != null) {
      args.append(P4File.create(root).getRecursivePath());
    }

    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getStderr().toLowerCase().contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return result;
    }

    getSomethingFromOutput(execResult.getStdout(), MERGING2_MESSAGE, new Consumer<String>() {
      public void consume(String s) {
        result.add(s);
      }
    });
    return result;
  }

  public List<VirtualFile> getResolvedWithConflicts(final P4Connection connection, @Nullable final VirtualFile root) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.resolve);
    args.append("-n");
    if (root != null) {
      args.append(P4File.create(root).getRecursivePath());
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getStderr().toLowerCase().contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return Collections.emptyList();
    }

    return getFilesFromOutput(execResult.getStdout(), MERGING2_MESSAGE);
  }

  @Nullable
  public BaseRevision getBaseRevision(P4File file) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    final ExecResult execResult = executeP4Command(new String[]{"resolve",
      "-n",
      "-o",
      file.getEscapedPath()}, connection);
    final String stdout = execResult.getStdout();
    if (stdout.toLowerCase().contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return null;
    }
    checkError(execResult);
    try {
      Map<File, BaseRevision> result = processResolveOutput(stdout);
      if (result.isEmpty()) {
        return null;
      }
      final File[] files = result.keySet().toArray(new File[result.size()]);
      return result.get(files [0]);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public static Map<File, BaseRevision> processResolveOutput(final String output) throws IOException {
    final HashMap<File, BaseRevision> result = new HashMap<File, BaseRevision>();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(output));

      String line;
      while ((line = reader.readLine()) != null) {
        processResolveLine(line, result);
      }
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
    return result;
  }

  private static void processResolveLine(final String line, final HashMap<File, BaseRevision> result) {
    int mergingPosition = line.indexOf(MERGING_MESSAGE);
    if (mergingPosition < 0) return;
    File file = new File(line.substring(0, mergingPosition).trim());
    int usingBasePosition = line.indexOf(USING_BASE_MESSAGE);
    if (usingBasePosition < 0) return;
    String sourcePath = line.substring(mergingPosition + MERGING_MESSAGE.length(), usingBasePosition);
    long sourceRevision = -1;
    int sourceRevPosition = sourcePath.lastIndexOf('#');
    if (sourceRevPosition >= 0) {
      sourceRevision = Long.parseLong(sourcePath.substring(sourceRevPosition+1).trim());
    }
    int revPosition = line.indexOf("#", usingBasePosition);

    String basePath = line.substring(usingBasePosition + USING_BASE_MESSAGE.length(), revPosition).trim();

    if (revPosition < 0) return;
    final String revision = line.substring(revPosition + 1).trim();
    try {
      final long revisionNum = Long.parseLong(revision);
      result.put(file, new BaseRevision(revisionNum, sourceRevision, basePath));
    }
    catch (NumberFormatException e) {
      //igore
    }
  }

  public void resolveToYours(final P4File file) throws VcsException {
    resolve(file, "-ay");
  }

  public void resolveAutomatically(final P4File file) throws VcsException {
    resolve(file, "-am");
  }

  public void resolveForce(final P4File file) throws VcsException {
    resolve(file, "-af");
  }

  private void resolve(final P4File file, @NonNls final String resolveMode) throws VcsException {
    @NonNls String[] p4args = {"resolve", resolveMode, file.getRecursivePath()};
    final ExecResult execResult = executeP4Command(p4args, myConnectionManager.getConnectionForFile(file));
    final String stdErr = execResult.getStderr().toLowerCase();
    if (!stdErr.contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      checkError(execResult);
    }
  }

  public ExecResult integrate(final String branchName,
                              final P4File path,
                              final long changeListNum,
                              @Nullable final String integrateChangeListNum,
                              final boolean isReverse,
                              final P4Connection connection) {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.integrate);
    if (changeListNum >= 0) {
      arguments.append("-c").append(changeListNum);
    }
    arguments.append("-b").append(branchName);

    if (isReverse) {
      arguments.append("-r");
    }

    if (integrateChangeListNum == null) {
      arguments.append(path.getRecursivePath());
    }
    else {
      arguments.append(path.getRecursivePath() + "@" + integrateChangeListNum + ",@" + integrateChangeListNum);
    }

    return executeP4Command(arguments.getArguments(), connection);
  }

  private static void getSomethingFromOutput(final String out, final String separator, final Consumer<String> consumer) throws VcsException {
    final BufferedReader rdr = new BufferedReader(new StringReader(out));
    try {
      String s;
      while ((s = rdr.readLine()) != null) {

        final int endOfFileName = s.indexOf(separator);
        if (endOfFileName >= 0) {
          final String fileName = s.substring(0, endOfFileName).trim();
          consumer.consume(fileName);
        }
      }
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private static List<VirtualFile> getFilesFromOutput(final String out, final String separator) throws VcsException {
    final List<VirtualFile> result = new ArrayList<VirtualFile>();

    getSomethingFromOutput(out, separator, new Consumer<String>() {
      public void consume(final String s) {
        final VirtualFile virtualFile = ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
          @Nullable public VirtualFile compute() {
            return LocalFileSystem.getInstance().findFileByIoFile(new File(s));
        }});
        if (virtualFile != null) {
          result.add(virtualFile);
        }
      }
    });

    return result;
  }

  public void have(final P4File file, final P4Connection connection, final boolean recursively, final Consumer<String> consumer) throws VcsException {
    if (file != null && connection.handlesFile(new File(file.getLocalPath()))) {
      final MyDelegatingHaveParser haveParser = new MyDelegatingHaveParser(consumer);
      doHave(file, connection, recursively, haveParser);
    }
  }

  public void have(P4File file, P4Connection connection, boolean recursively, TObjectLongHashMap<String> haveRevisions) throws VcsException {
    if (file != null && connection.handlesFile(new File(file.getLocalPath()))) {
      final MyWithRevisionHaveParser haveParser = new MyWithRevisionHaveParser(haveRevisions);
      doHave(file, connection, recursively, haveParser);
    }
  }

  public boolean have(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    TObjectLongHashMap<String> haveRevisions = new TObjectLongHashMap<String>();
    final MyWithRevisionHaveParser haveParser = new MyWithRevisionHaveParser(haveRevisions);
    doHave(file, connection, file.isDirectory(), haveParser);
    return !haveRevisions.isEmpty();
  }

  public long haveRevision(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    TObjectLongHashMap<String> haveRevisions = new TObjectLongHashMap<String>();
    final MyWithRevisionHaveParser haveParser = new MyWithRevisionHaveParser(haveRevisions);
    doHave(file, connection, false, haveParser);
    if (!haveRevisions.isEmpty()) {
      final long[] values = haveRevisions.getValues();
      return values [0];
    }
    return -1;
  }

  public void haveMultiple(final PathsHelper helper, final P4Connection connection, final ThrowableConsumer<String, VcsException> consumer) throws VcsException {
    if (helper.isEmpty()) return;
    final List<String> args = helper.getRequestString();

    if (args.size() > HAVE_SIZE) {
      final CollectionSplitter<String> splitter = new CollectionSplitter<String>(HAVE_SIZE);
      final List<List<String>> collOfColl = splitter.split(args);
      for (List<String> collection : collOfColl) {
        doHave(collection, connection, consumer);
      }
    } else {
      doHave(args, connection, consumer);
    }
  }

  public static class PathsHelper {
    private final PerforceManager myPerforceManager;

    private final Collection<VirtualFile> myRecursiveFiles;
    private final Collection<VirtualFile> mySimpleFiles;
    private final Collection<FilePath> myRecursivePaths;
    private final Collection<FilePath> mySimplePaths;

    public PathsHelper(final PerforceManager perforceManager) {
      myPerforceManager = perforceManager;
      myRecursiveFiles = new LinkedList<VirtualFile>();
      mySimpleFiles = new LinkedList<VirtualFile>();
      myRecursivePaths = new LinkedList<FilePath>();
      mySimplePaths = new LinkedList<FilePath>();
    }

    public void add(final VirtualFile vf) {
      mySimpleFiles.add(vf);
    }

    public void addRecursively(final VirtualFile vf) {
      myRecursiveFiles.add(vf);
    }

    public void add(final FilePath path) {
      mySimplePaths.add(path);
    }

    public void addRecursively(final FilePath path) {
      myRecursivePaths.add(path);
    }

    public void addAll(final Collection<VirtualFile> files) {
      mySimpleFiles.addAll(files);
    }

    public void addAllRecursively(final Collection<VirtualFile> files) {
      myRecursiveFiles.addAll(files);
    }

    public void addAllPaths(final Collection<FilePath> files) {
      mySimplePaths.addAll(files);
    }

    public void addAllPathsRecursively(final Collection<FilePath> files) {
      myRecursivePaths.addAll(files);
    }

    public boolean isEmpty() {
      return myRecursiveFiles.isEmpty() && mySimpleFiles.isEmpty() && myRecursivePaths.isEmpty() && mySimplePaths.isEmpty();
    }

    public List<String> getRequestString() {
      final List<String> result = new LinkedList<String>();
      final StringBuilder sb = new StringBuilder();
      for (FilePath file : myRecursivePaths) {
        result.add(convert(getP4FilePath(P4File.create(file), true)));
      }
      for (VirtualFile file : myRecursiveFiles) {
        result.add(convert(getP4FilePath(P4File.create(file), true)));
      }
      for (FilePath file : mySimplePaths) {
        result.add(convert(getP4FilePath(P4File.create(file), false)));
      }
      for (VirtualFile file : mySimpleFiles) {
        result.add(convert(getP4FilePath(P4File.create(file), false)));
      }
      return result;
    }

    private String convert(final String s) {
      return myPerforceManager.convertP4ParsedPath(null, s);
    }
  }

  /*public void haveMultiple(final Collection<VirtualFile> recursiveFiles, final Collection<VirtualFile> notRecursiveFiles,
                            final P4Connection connection, final ThrowableConsumer<String, VcsException> consumer) throws VcsException {
    if (recursiveFiles.isEmpty() && notRecursiveFiles.isEmpty()) return;
    final StringBuilder sb = new StringBuilder();
    for (VirtualFile file : recursiveFiles) {
      sb.append(getP4FilePath(P4File.create(file), true));
      sb.append(' ');
    }
    for (VirtualFile file : notRecursiveFiles) {
      sb.append(getP4FilePath(P4File.create(file), false));
      sb.append(' ');
    }
    doHave(sb.toString(), connection, consumer);
  }*/

  private static String getP4FilePath(final P4File file, final boolean recursively) {
    final String result;
    if (file.isDirectory()) {
      result = file.getEscapedPath() + "/" + (recursively ? "..." : "*");
    }
    else {
      result = file.getEscapedPath();
    }
    return result;
    //return FileUtil.toSystemIndependentName(result);
  }

  // todo do not use overloading too much
  private void doHave(final P4File file, final P4Connection connection, final boolean recursively, final ThrowableConsumer<String, VcsException> consumer) throws VcsException {
    final String filePath = getP4FilePath(file, recursively);
    final List<String> list = new ArrayList<String>(2);
    list.add(P4File.escapeWildcards(filePath));
    doHave(list, connection, consumer);
  }

  private String filesSpecToString(final List<String> filesSpec) {
    final StringBuilder sb = new StringBuilder();
    for (String s : filesSpec) {
      sb.append(s).append("\n");
    }
    return sb.toString();
  }

  private void doHave(final List<String> filesSpec, final P4Connection connection, final ThrowableConsumer<String, VcsException> consumer) throws VcsException {
    // See http://www.perforce.com/perforce/doc.052/manuals/cmdref/have.html#1040665
    // According to Perforce docs output will be presented patterned like: depot-file#revision-number - local-path
    // One line per file

    filesSpec.add(0, "have");
    final ExecResult execResult = executeP4Command(filesSpec.toArray(new String[filesSpec.size()]), connection);
    final String stderr = execResult.getStderr();
    final boolean notUnderRoot = (stderr.indexOf(NOT_ON_CLIENT_MESSAGE) != -1) || (stderr.indexOf(NOT_UNDER_CLIENT_ROOT_MESSAGE) != -1);
    if (! notUnderRoot) {
      // Perforce bug: if ask "p4 have <local path>/*" and in <local path> directory it would be unversioned file with symbols
      // that should be escaped, Perforce reports "Invalid revision number" somewhy
      // since we do NOT pass revision number in have string, we can filter out this message and use other strings of output
      if (! stderr.contains(INVALID_REVISION_NUMBER)) {
        checkError(execResult);
      }
    } else {
      LOG.info("Problem while doing 'have': " + stderr);
    }
    final Ref<VcsException> vcsExceptionRef = new Ref<VcsException>();
    try {
      execResult.allowSafeStdoutUsage(new ThrowableConsumer<InputStream, IOException>() {
        public void consume(InputStream inputStream) throws IOException {
          final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
          do {
            String haveline = reader.readLine();
            if (haveline == null || haveline.length() == 0) break;
            try {
              consumer.consume(haveline);
            }
            catch (VcsException e) {
              vcsExceptionRef.set(e);
              return;
            }
          }
          while (true);
        }
      });
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
    if (! vcsExceptionRef.isNull()) {
      throw vcsExceptionRef.get();
    }
  }

  public class MyDelegatingHaveParser implements ThrowableConsumer<String, VcsException> {
    private final Consumer<String> myDelegate;

    public MyDelegatingHaveParser(final Consumer<String> delegate) {
      myDelegate = delegate;
    }

    public void consume(String haveline) throws VcsException {
      final int hashIndex = haveline.indexOf('#');
      if (hashIndex < 0) {
        throw new VcsException("Unexpected 'p4 have' output format: " + haveline);
      }
      final int idx = haveline.indexOf(HAVE_DELIM, hashIndex);
      if (idx < 0) {
        throw new VcsException("Unexpected 'p4 have' output format: " + haveline);
      }
      String localPath = haveline.substring(idx + HAVE_DELIM.length());
      localPath = myPerforceManager.convertP4ParsedPath(null, localPath);
      myDelegate.consume(localPath);
    }
  }

  private class MyWithRevisionHaveParser implements ThrowableConsumer<String, VcsException> {
    private final TObjectLongHashMap<String> myHaveRevisions;

    public MyWithRevisionHaveParser(final TObjectLongHashMap<String> haveRevisions) {
      myHaveRevisions = haveRevisions;
    }

    public void consume(final String haveline) throws VcsException {
      final int hashIndex = haveline.indexOf('#');
      if (hashIndex < 0) {
        throw new VcsException("Unexpected 'p4 have' output format: " + haveline);
      }
      final int idx = haveline.indexOf(HAVE_DELIM, hashIndex);
      if (idx < 0) {
        throw new VcsException("Unexpected 'p4 have' output format: " + haveline);
      }
      String localPath = haveline.substring(idx + HAVE_DELIM.length());
      localPath = myPerforceManager.convertP4ParsedPath(null, localPath);
      final long revision = Long.parseLong(haveline.substring(hashIndex+1, idx));
      myHaveRevisions.put(FileUtil.toSystemDependentName(localPath), revision);
    }
  }

  private static final String HAVE_DELIM = " - ";

  public P4Revision[] filelog(final P4File file, boolean showBranches) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    final CommandArguments arguments = createFilelogArgs(showBranches, connection);
    arguments.append(file.getEscapedPath());
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult);

    return parseLogOutput(execResult, isFilelogNewDateVersion(connection));
  }

  public List<String> dirs(final Collection<VirtualFile> dirs, final P4Connection connection) throws VcsException {
    if (dirs.isEmpty()) return Collections.emptyList();

    final List<String> params = new LinkedList<String>();
    // todo check for client root dir
    for (VirtualFile dir : dirs) {
      if (dir.isDirectory()) {
        final P4File p4File = P4File.create(dir);
        final P4WhereResult p4WhereResult = whereDir(p4File.getRecursivePath(), connection);
        params.add(p4WhereResult.getDepot() + "/*");
      }
    }
    if (params.isEmpty()) return Collections.emptyList();

    final CommandArguments args = CommandArguments.createOn(P4Command.dirs);
    for (String param : params) {
      args.append(param);
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getExitCode() != 0) {
      checkError(execResult);
    }
    try {
      return new OutputMessageParser(execResult.getStdout()).myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> files(final Collection<String> depotPaths, final P4Connection connection) throws VcsException {
    if (depotPaths.isEmpty()) return Collections.emptyList();

    final StringBuilder sb = new StringBuilder();
    for (String dir : depotPaths) {
      sb.append(dir).append("/* ");
    }
    if (sb.length() == 0) return Collections.emptyList();
    final CommandArguments args = CommandArguments.createOn(P4Command.files);
    args.append(sb.toString());
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getExitCode() != 0) {
      checkError(execResult);
    }
    try {
      return new OutputMessageParser(execResult.getStdout()).myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private CommandArguments createFilelogArgs(boolean showBranches, final P4Connection connection) {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.filelog);
    if (showBranches) {
      arguments.append("-i");
    }
    arguments.append("-l");
    if (isFilelogNewDateVersion(connection)) {
      arguments.append("-t");
    }
    return arguments;
  }

  private boolean isFilelogNewDateVersion(final P4Connection connection) {
    final ServerVersion serverVersion = mySettings.getServerFullVersion(connection);
    if (serverVersion == null) return false;
    return serverVersion.getVersionYear() >= 2003 || serverVersion.getVersionYear() == 2002 && serverVersion.getVersionNum() > 1;
  }

  private static P4Revision[] parseLogOutput(final ExecResult execResult, boolean newDateFormat) throws VcsException {
    try {
      final List<P4Revision> p4Revisions = OutputMessageParser.processLogOutput(execResult.getStdout(), newDateFormat);
      return p4Revisions.toArray(new P4Revision[p4Revisions.size()]);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public AnnotationInfo annotate(P4File file, long revision) throws VcsException {
    final P4Connection connection = myConnectionManager.getConnectionForFile(file);
    String annotatePath = file.getEscapedPath();
    if (revision != -1) {
      annotatePath = annotatePath + "#" + revision;
    }
    @NonNls String[] commands;
    boolean useChangelistNumbers = false;
    if (mySettings.SHOW_BRANCHES_HISTORY && isAnnotateBranchSupported(connection)) {
      commands = new String[]{"annotate", "-q", "-i", annotatePath};
      useChangelistNumbers = true;
    }
    else {
      commands = new String[]{"annotate", "-q", annotatePath};
    }
    final ExecResult execResult = executeP4Command(commands, connection);
    checkError(execResult);
    try {
      return new AnnotationInfo(execResult.getStdout(), useChangelistNumbers);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private boolean isAnnotateBranchSupported(final P4Connection connection) {
    ServerVersion version = myPerforceManager.getServerVersion(connection);
    if (version == null) return false;
    return version.getVersionYear() > 2005 || version.getVersionYear() == 2005 && version.getVersionNum() >= 2;
  }

  public List<ResolvedFile> getResolvedFiles(final P4Connection connection, @Nullable final VirtualFile root) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.resolved);
    if (root != null) {
      args.append(P4File.create(root).getRecursivePath());
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (execResult.getStderr().toLowerCase().contains(NO_FILES_RESOLVED_MESSAGE)) {
      return Collections.emptyList();
    }
    checkError(execResult);
    try {
      final String clientRoot = myPerforceManager.getClientRoot(connection);
      return PerforceOutputMessageParser.processResolvedOutput(execResult.getStdout(),
                                                               // this convertor will replace possibly not canonic
                                                               // client root in a path (client root returned by perforce manager is already canonicalized)
                                                               new Convertor<String, String>() {
                                                                 public String convert(String o) {
                                                                   return myPerforceManager.convertP4ParsedPath(clientRoot, o);
                                                                 }
                                                               });
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<ResolvedFile> getResolvedFiles(final P4Connection connection) throws VcsException {
    return getResolvedFiles(connection, null);
  }

  public List<String> getJobSpecification(final P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"jobspec", "-o"}, connection);
    checkError(execResult);
    final OutputMessageParser parser;
    try {
      final String stdout = execResult.getStdout();
      SPECIFICATION_LOG.debug(stdout);
      parser = new OutputMessageParser(stdout);
      return parser.myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> getJobs(final P4Connection connection, final JobsSearchSpecificator specificator) throws VcsException {
    final String[] strings = specificator.addParams(new String[]{"jobs", "-l"});
    final ExecResult execResult = executeP4Command(strings, connection);
    checkError(execResult);
    try {
      return new OutputMessageParser(execResult.getStdout()).myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> getJobDetails(final PerforceJob job) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"job", "-o", job.getName()}, job.getConnection());
    checkError(execResult);
    try {
      return new OutputMessageParser(execResult.getStdout()).myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> getJobsForChange(final P4Connection connection, final long number) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fixes", "-c", "" + number}, connection);
    checkError(execResult);
    try {
      return new OutputMessageParser(execResult.getStdout()).myLines;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public void addJobForList(final P4Connection connection, final long number, final String name) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fix", "-c", "" + number, name}, connection);
    checkError(execResult);
  }

  public void removeJobFromList(final P4Connection connection, final long number, final String name) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fix", "-d", "-c", "" + number, name}, connection);
    checkError(execResult);
  }

  private ExecResult executeP4Command(@NonNls final String[] p4args, final P4Connection connection) {
    return executeP4Command(p4args, connection, null);
  }

  private ExecResult executeP4Command(@NonNls final String[] p4args, final P4Connection connection,
                                      @Nullable final StringBuffer inputStream) {
    return executeP4Command(p4args, connection, inputStream, false);
  }

  private ExecResult executeP4Command(@NonNls final String[] p4args, final P4Connection connection,
                                      @Nullable final StringBuffer inputStream, final boolean justLogged) {
    // construct the command-line
    final ExecResult retVal = new ExecResult();
    if (!mySettings.ENABLED) {
      retVal.setException(new VcsException(PerforceBundle.message("exception.text.perforce.integration.is.disabled")));
      retVal.setStderr(PerforceBundle.message("exception.text.perforce.integration.is.disabled"));
      return retVal;
    }
    try {
      final StringBuffer status = new StringBuffer();
      for (String p4arg : p4args) {
        status.append(p4arg).append(' ');
      }

      final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
      if (progressIndicator != null) {
        progressIndicator.setText2(PerforceBundle.message("progress.text2.p4.status", status));
        progressIndicator.setText(PerforceBundle.message("progress.text.perforce.command"));
      }


      if (mySettings.showCmds()) {
        logMessage(commandsToString(p4args));
      }

      connection.runP4Command(mySettings, p4args, retVal, inputStream);

    }
    catch (PerforceTimeoutException e) {
      retVal.setException(e);
      retVal.setStderr(PerforceBundle.message("exception.text.perforce.integration.disconnected"));
    }
    catch (IOException e) {
      retVal.setException(e);
    }
    catch (InterruptedException e) {
      retVal.setException(e);
    }
    catch (VcsException e) {
      retVal.setException(e);
    }

    if (mySettings.showCmds()) {
      logMessage("\n" + retVal.toString());
    }

    if (mySettings.USE_LOGIN && (retVal.getStderr().contains(SESSION_EXPIRED_MESSAGE) || retVal.getStderr().contains(PASSWORD_INVALID_MESSAGE))) {
      myPerforceManager.getLoginManager().notLogged(connection);
      try {
        if ((! justLogged) && myPerforceManager.getLoginManager().silentLogin(connection)) {
          retVal.cleanup();
          return executeP4Command(p4args, connection, null, true);
        }
      }
      catch (VcsException e) {
        retVal.setException(e);
        return retVal;
      }
      myNotifier.ensureNotify(connection);

      return retVal;
    }

    if ((! mySettings.USE_LOGIN) && retVal.getStderr().contains(PASSWORD_INVALID_MESSAGE)) {
      myPerforceManager.getLoginManager().notLogged(connection);

      final WindowManager windowManager = WindowManager.getInstance();
      final StatusBar statusBar = windowManager.getStatusBar(mySettings.getProject());
      if (statusBar != null) {
        statusBar.setInfo(PerforceBundle.message("p4.login.error.status.text", retVal.getStderr()));
      }
      // do not notify if login is NOT used
      /*myPerforceManager.getLoginManager().notLogged(connection);
      myNotifier.ensureNotify(connection);*/
    }

    if ((! mySettings.USE_LOGIN) && retVal.getStderr().contains(PASSWORD_NOT_ALLOWED_MESSAGE)) {
      myPerforceManager.getLoginManager().notLogged(connection);
      
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          if (mySettings.USE_LOGIN || (! mySettings.ENABLED)) {
            return;
          }
          final int dialogResult = MessageManager
            .showDialog(mySettings.getProject(), PerforceBundle.message("confirmation.text.password.not.allowed.enable.login"),
                        PerforceBundle.message("dialog.title.perforce"),
                        new String[]{CommonBundle.getYesButtonText(), PerforceBundle.message("button.text.disable.integration")}, 0,
                        Messages.getQuestionIcon());
          if (dialogResult == 0) {
            mySettings.USE_LOGIN = true;
            myConnectionManager.refreshConnections(mySettings);
            myPerforceManager.configurationChanged();
          } else {
            mySettings.disable();
          }
        }
      });
      return retVal;
    }
    if (mySettings.USE_LOGIN && (retVal.getExitCode() == 0) && (retVal.getException() == null)) {
      myPerforceManager.getLoginManager().ensure(connection);
    }
    return retVal;
  }

  private static void logMessage(final String message) {
    logMessage(message, getDumpFile(), MAX_LOG_LENGTH);
  }

  public static File getDumpFile() {
    return new File(DUMP_FILE_NAME);
  }

  private static void logMessage(final String message, File file, long maxFileLength) {
    try {
      if (file.length() > maxFileLength) {
        file.delete();
      }
      final OutputStream output = new BufferedOutputStream(new FileOutputStream(file, true));
      try {

        output.write(message.getBytes());
      }
      catch (Exception e) {
        //ignore
      }
      finally {
        output.close();
      }
    }
    catch (IOException e) {
      //ignore
    }
  }

  public static String commandsToString(final String[] p4args) {
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < p4args.length; i++) {
      String p4arg = p4args[i];
      if (i != 0) result.append(" ");
      result.append(p4arg);
    }
    return result.toString();
  }

  @Nullable
  private String requestForPassword(final String message, String defaultValue, final String rootDir) {
    final String prompt;
    if (myConnectionManager.isSingletonConnectionUsed()) {
      prompt = PerforceBundle.message("message.text.perforce.command.failed.enter.password", message);
    } else {
      prompt = PerforceBundle.message("message.text.perforce.command.failed.withdir.enter.password", rootDir, message);
    }
    final PerforceLoginDialog pwdDialog = new PerforceLoginDialog(myProject, prompt, defaultValue);
    pwdDialog.show();
    if (pwdDialog.isOK()) {
      return pwdDialog.getPassword();
    }
    else {
      return null;
    }
  }

  public void performLogin(String password, final P4Connection connection) throws VcsException {
    final StringBuffer data = new StringBuffer();
    data.append(password);
    final ExecResult loginResult = connection.runP4CommandLine(mySettings, new String[]{"login"}, data);
    if (loginResult.getStderr().length() > 0 || !loginResult.getStdout().contains(LOGGED_IN_MESSAGE)) {
      throw new VcsException(loginResult.getStderr());
    }

    if (loginResult.getException() != null) {
      throw new VcsException(loginResult.getException());
    }
  }

  private static void checkError(final ExecResult result) throws VcsException {
    final Throwable exception = result.getException();
    final boolean error = exception != null || containsErrorOutput(result) || result.getExitCode() < 0;
    if (error) {
      final String errorOutput = result.getStderr();
      if (exception != null) {
        result.cleanup();
        throw new VcsException(exception);
      }
      else {
        if (errorOutput.trim().length() > 0) {
          throw new VcsException(errorOutput);
        }
        else {
          throw new VcsException("p4 returned error code " + result.getExitCode());
        }
      }
    }
  }

  public static VcsException[] checkErrors(final ExecResult result) {
    final Throwable exception = result.getException();
    final boolean error = result.getExitCode() < 0 || exception != null || containsErrorOutput(result);
    if (error) {
      final String errorOutput = result.getStderr();
      result.cleanup();
      if (exception != null) {
        return new VcsException[] { new VcsException(exception) };
      }
      else {
        String[] lines = errorOutput.split("\n");
        VcsException[] errors = new VcsException[lines.length];
        for(int i=0; i<lines.length; i++) {
          errors [i] = new VcsException(lines [i]);
        }
        return errors;
      }
    }
    return VcsException.EMPTY_ARRAY;
  }

  private static boolean containsErrorOutput(final ExecResult result) {
    String errorOutput = result.getStderr().trim();
    return !errorOutput.contains(FILES_UP_TO_DATE) && errorOutput.length() > 2;
  }

  public P4WhereResult where(final P4File file, final P4Connection connection) throws VcsException {
    return where(file.getLocalPath(), connection);
  }

  public P4WhereResult whereDir(final String anyPath, final P4Connection connection) throws VcsException {
    final String fake = "/...";
    final P4WhereResult result = where(anyPath, connection);
    return new P4WhereResult(removeTail(result.getLocal(), fake), removeTail(result.getLocalRootDependent(), fake),
                             removeTail(result.getDepot(), fake));
  }

  private String removeTail(final String s, final String tail) {
    if (s.endsWith(tail)) {
      return s.substring(0, s.length() - tail.length());
    }
    return s;
  }

  public P4WhereResult where(final String anyPath, final P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"where", anyPath}, connection);
    checkError(execResult);

    String local = null;
    String localRootRelative = null;
    String depot = null;
    final String result = execResult.getStdout();
    final String[] lines = result.trim().split("\n");
    String out = null;
    if (lines.length > 1) {
      for (String line : lines) {
        if (line.startsWith("-")) continue;
        out = line;
        break;
      }
      if (out == null) throw new VcsException("p4 where wrong result: " + result);
    } else {
      out = lines[0];
    }

    final int idxWhiteFirst = out.indexOf(' ');
    if (idxWhiteFirst > 0) {
      depot = out.substring(0, idxWhiteFirst);
      out = out.substring(idxWhiteFirst).trim();
      final int idxNext = out.indexOf(' ');
      if (idxNext > 0) {
        localRootRelative = out.substring(0, idxNext);
        local = out.substring(idxNext).trim();
      } else {
        localRootRelative = out;
      }
    } else {
      depot = out;
    }

    if (localRootRelative == null || local == null || depot == null) {
      throw new VcsException("p4 where wrong result: " + result);
    }
    local = myPerforceManager.convertP4ParsedPath(null, local);
    return new P4WhereResult(local, localRootRelative, depot);
  }

  public ClientVersion getClientVersion(final P4Connection connection) {
    // just "p4 -V"
    try {
      final ExecResult result = executeP4Command(new String[]{"-V"}, connection);
      checkError(result);
      final String out = result.getStdout();
      final Map<String, List<String>> map = FormParser.execute(out, new String[]{CLIENT_VERSION_REV});
      final List<String> versionString = map.get(CLIENT_VERSION_REV);
      if ((versionString != null) && (! versionString.isEmpty())) {
        return OutputMessageParser.parseClientVersion(versionString.get(0));
      }
    }
    catch (VcsException e) {
      //
    }
    return ClientVersion.UNKNOWN;
  }

  public void move(final P4File from, final P4File to, final P4Connection connection) throws VcsException {
    final CommandArguments command = CommandArguments.createOn(P4Command.move);
    command.append(from.getEscapedPath());
    command.append(to.getEscapedPath());

    final ExecResult result = executeP4Command(command.getArguments(), connection);
    checkError(result);
  }
}
