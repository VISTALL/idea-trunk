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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.*;

public class PerforceIntegrateEnvironment extends AbstractUpdateEnvironment {
  @NonNls private static final Map<String, String> ourPatternToGroupId = new HashMap<String, String>();

  @NonNls private static final String INTEGRATED = "INTEGRATED";
  @NonNls private static final String CANT_INTEGRATE = "CANT_INTEGRATE";
  @NonNls private static final String BRANCHED = "BRANCHED";

  static {
    ourPatternToGroupId.put(" - integrate from ", INTEGRATED);
    ourPatternToGroupId.put(" - sync/integrate from ", INTEGRATED);
    ourPatternToGroupId.put(" - can't integrate (already opened on this client)", CANT_INTEGRATE);
    ourPatternToGroupId.put(" - branch/sync from ", BRANCHED);
  }

  public PerforceIntegrateEnvironment(final Project project) {
    super(project);
  }

  public void fillGroups(UpdatedFiles updatedFiles) {
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.integrated"),
                                             PerforceBundle.message("integrate.group.name.integrated"), false, INTEGRATED, false));
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.cant.integrate"),
                                             PerforceBundle.message("integrate.group.name.cant.integrate"), false, CANT_INTEGRATE, false));
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.branched"),
                                             PerforceBundle.message("integrate.group.name.branched"), false, BRANCHED, false));
  }

  protected boolean isTryToResolveAutomatically(PerforceSettings settings) {
    return settings.INTEGRATE_RUN_RESOLVE;
  }

  protected Map<String, String> getPatternToGroupId() {
    return ourPatternToGroupId;
  }

  protected boolean isRevertUnchanged(PerforceSettings settings) {
    return settings.INTEGRATE_REVERT_UNCHANGED;
  }

  protected ExecResult performUpdate(P4File p4Dir, PerforceSettings settings) throws VcsException {
    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(p4Dir);
    final ParticularConnectionSettings connectionSettings = settings.getSettings(connection);

    final String integrateChangeListNum = connectionSettings.INTEGRATE_CHANGE_LIST ? connectionSettings.INTEGRATED_CHANGE_LIST_NUMBER : null;

    return myRunner.integrate(connectionSettings.INTEGRATE_BRANCH_NAME,
                              p4Dir,
                              connectionSettings.INTEGRATE_TO_CHANGELIST_NUM,
                              integrateChangeListNum, connectionSettings.INTEGRATE_REVERSE,
                              connection);
  }

  public Configurable createConfigurable(Collection<FilePath> files) {
    final Map<P4Connection, List<File>> connectionToFiles = PerforceSettings.getSettings(myProject).chooseConnectionForFile(convertToFiles(files));
    return new PerforceUpdateConfigurable(PerforceSettings.getSettings(myProject)){
      protected PerforcePanel createPanel() {
        return new PerforceIntegratePanel(myProject, new ArrayList<P4Connection>(connectionToFiles.keySet()));
      }

      public String getHelpTopic() {
        return "reference.dialogs.versionControl.integrate.project.perforce";
      }
    };
  }

  private static Collection<File> convertToFiles(final Collection<FilePath> files) {
    final ArrayList<File> result = new ArrayList<File>();
    for (FilePath filePath : files) {
      result.add(filePath.getIOFile());
    }
    return result;
  }

  public boolean validateOptions(final Collection<FilePath> roots) {
    return true;
  }
}
