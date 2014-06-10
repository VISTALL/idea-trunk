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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PerforceChangeGatherer<T extends PerforceChangesForConnection> {
  private final PerforceRunner myRunner;
  private final SplitListIntoConnections<T> mySplitter;

  public PerforceChangeGatherer(final Project project, final Convertor<P4Connection, T> factory) {
    myRunner = PerforceRunner.getInstance(project);
    mySplitter = new SplitListIntoConnections<T>(project, factory);
  }

  public void execute(final List<Change> incomingChanges) throws VcsException {
    mySplitter.execute(incomingChanges);
    final MultiMap<ConnectionKey, FilePath> filePaths = mySplitter.getPaths();
    final Map<ConnectionKey, T> byConnectionMap = mySplitter.getByConnectionMap();

    for (ConnectionKey key : filePaths.keySet()) {
      final Collection<FilePath> paths = filePaths.get(key);
      final PerforceChangesForConnection job = byConnectionMap.get(key);
      final List<PerforceChange> changes = myRunner.opened(job.getConnection(), paths);
      job.addChanges(changes);
    }
  }

  public Map<ConnectionKey, T> getByConnectionMap() {
    return mySplitter.getByConnectionMap();
  }
}
