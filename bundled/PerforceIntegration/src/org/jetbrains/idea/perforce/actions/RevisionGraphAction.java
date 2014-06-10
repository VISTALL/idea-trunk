/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class RevisionGraphAction extends AnAction {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.actions.RevisionGraphAction");

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    assert project != null && virtualFile != null;
    final P4Connection connection = PerforceConnectionManager.getInstance(project).getConnectionForFile(virtualFile);
    final PerforceClient client = PerforceManager.getInstance(project).getClient(connection);
    final PerforceSettings settings = PerforceSettings.getSettings(project);
    @NonNls List<String> cmd = new ArrayList<String>();

    String port = client.getServerPort();
    String userName = client.getUserName();
    String name = client.getName();

    if (port == null || userName == null || name == null) {
      Messages.showErrorDialog(project, "Failed to retrieve Perforce client settings. Available information: port=" + port +
                               " user name=" + userName + " client name=" + name,
                               "Perforce");
      return;
    }

    //create the command
    cmd.add(settings.PATH_TO_P4V);
    cmd.add("-p");
    cmd.add(client.getServerPort());
    cmd.add("-u");
    cmd.add(client.getUserName());
    cmd.add("-c");
    cmd.add(client.getName());
    if (SystemInfo.isWindows) {
      cmd.add("-win");
      cmd.add("0");
    }
    cmd.add("-cmd");
    @NonNls StringBuilder command = new StringBuilder("\"").append(getCommandName()).append(" ");

    FStat fStat = null;
    if (settings.ENABLED) {
      final P4File p4File = P4File.create(virtualFile);
      try {
        fStat = p4File.getFstat(project, false);
      }
      catch (VcsException ex) {
        Messages.showErrorDialog(project, PerforceBundle.message("failed.to.retrieve.p4.status.information",
                                                                 FileUtil.toSystemDependentName(virtualFile.getPath()),
                                                                 ex.getMessage()),
                                 "Perforce");
        return;
      }
    }
    if (fStat != null && !StringUtil.isEmpty(fStat.depotFile)) {
      command.append(fStat.depotFile);
    }
    else {
      command.append(FileUtil.toSystemDependentName(virtualFile.getPath()));
    }

    command.append("\"");

    cmd.add(command.toString());

    try {
      Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]), EnvironmentUtil.getEnvironment());
    }
    catch (IOException ex) {
      Messages.showErrorDialog(project, PerforceBundle.message("p4v.run.failed", ex.getMessage()), "P4V");
    }
  }

  @NonNls
  protected String getCommandName() {
    return "tree";
  }

  @Override
  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    if (project == null || virtualFile == null || !virtualFile.isInLocalFileSystem()) {
      e.getPresentation().setEnabled(false);
    }
    else {
      FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(virtualFile);
      e.getPresentation().setEnabled(fileStatus != FileStatus.ADDED && fileStatus != FileStatus.UNKNOWN && fileStatus != FileStatus.IGNORED);
    }
  }
}