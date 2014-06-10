/*
 * Copyright 2000-2005 JetBrains s.r.o.
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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.PasswordUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.*;

@State(
  name="PerforceDirect.Settings",
  storages= {
    @Storage(
      id="other",
      file = "$WORKSPACE_FILE$"
    )}
)
public final class PerforceSettings implements PersistentStateComponent<PerforceSettings> {

  @Property(surroundWithTag = false)
  @MapAnnotation(surroundWithTag=false, surroundKeyWithTag = false, surroundValueWithTag = false, entryTagName = "CURRENT_CHANGE_LIST")
  public Map<ConnectionId, ParticularConnectionSettings> myConnectionSettings = new HashMap<ConnectionId, ParticularConnectionSettings>();

  public static final String CHARSET_NONE = PerforceBundle.message("none.charset.presentation");

  public boolean useP4CONFIG = true;
  @NonNls public String port = "<perforce_server>:1666";
  public String client = "";
  public String user = "";
  public String passwd = "";
  public boolean showCmds = false;

  public String pathToExec = s_p4cmd;
  @NonNls public String PATH_TO_P4V = "p4v";
  public boolean useCustomPathToExec = false;

  private final Project myProject;


  @NonNls private static final String s_p4cmd = "p4";
  public boolean SYNC_FORCE = false;
  public boolean SYNC_RUN_RESOLVE = true;
  public boolean REVERT_UNCHANGED_FILES = true;
  public boolean REVERT_UNCHANGED_FILES_CHECKIN = false;
  public String CHARSET = CHARSET_NONE;
  public boolean SHOW_BRANCHES_HISTORY = true;
  public boolean ENABLED = true;
  public boolean USE_LOGIN = false;
  public boolean LOGIN_SILENTLY = false;
  public boolean INTEGRATE_RUN_RESOLVE = true;
  public boolean INTEGRATE_REVERT_UNCHANGED = true;
  public int SERVER_TIMEOUT = 20000;
  public boolean USE_PERFORCE_JOBS = false;
  public boolean SHOW_INTEGRATED_IN_COMMITTED_CHANGES = true;

  private final List<PerforceActivityListener> myActivityListeners = ContainerUtil.createEmptyCOWList();

  //
  // public PerforceSettings methods
  //

  public PerforceSettings() {
    myProject = null;
  }

  public PerforceSettings(Project project) {
    myProject = project;
  }

  public boolean showCmds() {
    return showCmds;
  }

  public static PerforceSettings getSettings(final Project project) {
    return ServiceManager.getService(project, PerforceSettings.class);
  }

  /**
   * Gets the instance of the component if the project wasn't disposed. If the project was
   * disposed, throws ProcessCanceledException. Should only be used for calling from background
   * threads (for example, committed changes refresh thread).
   *
   * @param project the project for which the component instance should be retrieved.
   * @return component instance
   */
  public static PerforceSettings getSettingsChecked(final Project project) {
    return ApplicationManager.getApplication().runReadAction(new Computable<PerforceSettings>() {
      public PerforceSettings compute() {
        if (project.isDisposed()) throw new ProcessCanceledException();
        return getSettings(project);
      }
    });
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String[] getConnectArgs() {
    if (useP4CONFIG) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    else {
      final ArrayList<String> args = new ArrayList<String>();
      if (port != null && port.length() > 0) {
        args.add("-p");
        args.add(port);
      }
      if (client != null && client.length() > 0) {
        args.add("-c");
        args.add(client);
      }
      if (user != null && user.length() > 0) {
        args.add("-u");
        args.add(user);
      }
      if (!USE_LOGIN) {
        final String pass = getPasswd();
        if (pass != null && pass.length() > 0) {
          args.add("-P");
          args.add(pass);
        }
      }

      if (!isNoneCharset()) {
        args.add("-C");
        args.add(CHARSET);
      }

      return args.toArray(new String[args.size()]);
    }
  }

  public String getPathToExec() {
    return pathToExec;
  }

  public Project getProject() {
    return myProject;
  }

  @Nullable @Transient
  public String getPasswd() {
    if (passwd == null) {
      return null;
    }
    else if (passwd.length() == 0) {
      return passwd;
    }
    else {
      try {
        return PasswordUtil.decodePassword(passwd);
      }
      catch (Exception e) {
        return "";
      }
    }
  }

  public void setPasswd(final String passwd) {
    this.passwd = PasswordUtil.encodePassword(passwd);
  }

  public long getServerVersion(final P4Connection connection) {
    return PerforceManager.getInstance(myProject).getServerVertionYear(connection);
  }

  @Nullable
  public ServerVersion getServerFullVersion(final P4Connection connection) {
    return PerforceManager.getInstance(myProject).getServerVersion(connection);
  }

  public void disable() {
    ENABLED = false;
    PerforceManager.getInstance(myProject).configurationChanged();
  }

  public void enable() {
    ENABLED = true;
    PerforceManager.getInstance(myProject).configurationChanged();
    VcsOperationLog.getInstance(myProject).replayLog();
  }

  public boolean canBeChanged() {
    return true;
  }

  public Map<P4Connection, List<File>> chooseConnectionForFile(final Collection<File> files) {
    final HashMap<P4Connection, List<File>> result = new LinkedHashMap<P4Connection, List<File>>();
    final PerforceConnectionManager connectionManager = PerforceConnectionManager.getInstance(getProject());
    for (File file : files) {
      final P4Connection connection = connectionManager.getConnectionForFile(file);
      if (!result.containsKey(connection)) {
        result.put(connection, new ArrayList<File>());
      }
      result.get(connection).add(file);
    }
    return result;
  }

  public P4Connection getConnectionForFile(final File file) {
    return PerforceConnectionManager.getInstance(getProject()).getConnectionForFile(file);
  }

  public P4Connection getConnectionForFile(final VirtualFile file) {
    return PerforceConnectionManager.getInstance(getProject()).getConnectionForFile(file);
  }

  public List<P4Connection> getAllConnections() {
    return PerforceConnectionManager.getInstance(getProject()).getAllConnections(this);
  }

  public ParticularConnectionSettings getSettings(final P4Connection connection) {
    if (!myConnectionSettings.containsKey(connection.getId())) {
      myConnectionSettings.put(connection.getId(), new ParticularConnectionSettings());
    }
    return myConnectionSettings.get(connection.getId());
  }

  public void addActivityListener(PerforceActivityListener listener) {
    myActivityListeners.add(listener);
  }

  public void removeActivityListener(PerforceActivityListener listener) {
    myActivityListeners.remove(listener);
  }

  public void notifyChangeListSubmitted(P4Connection connection, long changeListNumber, final long newNumber) {
    for(PerforceActivityListener listener: myActivityListeners) {
      listener.changeListSubmitted(connection, changeListNumber, newNumber);
    }
  }

  public boolean isNoneCharset() {
    return CHARSET == null || CHARSET_NONE.equals(CHARSET);
  }

  public PerforceSettings getState() {
    return this;
  }

  public void loadState(PerforceSettings object) {
    XmlSerializerUtil.copyBean(object, this);
  }
}
