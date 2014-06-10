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

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.util.ThrowableConvertor;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public class PerforceChangeListCalculator implements ThrowableConvertor<PerforceChange, ChangeList, VcsException> {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceChangeListCalculator");
  private final Project myProject;
  private final P4Connection myConnection;
  private final ChangeListManagerGate myAddGate;
  private final ChangeListSynchronizer myChangeListSynchronizer;

  public PerforceChangeListCalculator(final Project project, final P4Connection connection, final ChangeListManagerGate addGate) {
    myProject = project;
    myConnection = connection;
    myAddGate = addGate;
    myChangeListSynchronizer = ChangeListSynchronizer.getInstance(myProject);
  }
  
  public ChangeList convert(PerforceChange perforceChange) throws VcsException {
    final long changeListNumber = perforceChange.getChangeList();
    final String changeListDescription = perforceChange.getChangeListDescription();
    return findOrCreate(myConnection, changeListNumber, new ThrowableComputable<String, VcsException>() {
      public String compute() throws VcsException {
        if (changeListDescription == null) {
          return myChangeListSynchronizer.getListName(myConnection, changeListNumber);
        }
        return changeListDescription;
      }
    }, myAddGate);
  }

  // todo refactor
  private ChangeList findOrCreate(final P4Connection connection, long p4number, final ThrowableComputable<String, VcsException> p4descriptionProvider,
                            final ChangeListManagerGate addGate) throws VcsException {
    final ChangeListSynchronizer changeListSynchronizer = myChangeListSynchronizer;
    String nativeDescription = (p4number == -1) ? ChangeListSynchronizer.getDefaultChangeListName() : p4descriptionProvider.compute();
    if (nativeDescription == null) {
      LOG.info("Native changelist description getting problem, list number = " + p4number);
      nativeDescription = "";
    }
    final String name = changeListSynchronizer.getListNameAndCorrect(connection, p4number, PerforceNameCommentConvertor.fromNative(nativeDescription).getIdeaName());

    // to take new name from P4
    ChangeList changeList = (name == null) ? null : addGate.findChangeList(name);
    if (name == null || changeList == null) {
      final Pair<String,String> result =
        changeListSynchronizer.createChangeListByDescription(connection, nativeDescription, p4number);
      changeList = addGate.findOrCreateList(result.getFirst(), result.getSecond());
    }
    return changeList;
  }
}
