/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
package org.jetbrains.tfsIntegration.core.tfs.conflicts;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.tfs.WorkspaceInfo;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.Conflict;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.ConflictType;
import org.jetbrains.tfsIntegration.ui.MergeNameDialog;

public class DialogNameMerger implements NameMerger {

  @Nullable
  public String mergeName(final WorkspaceInfo workspace, Conflict conflict) {
    final String yoursName;
    final String theirsName;
    if (conflict.getCtype() == ConflictType.Merge) {
      yoursName = conflict.getYsitem();
      theirsName = conflict.getYsitemsrc();
    } else {
      yoursName = conflict.getYsitemsrc();
      theirsName = conflict.getTsitem();
    }
    MergeNameDialog d = new MergeNameDialog(workspace, yoursName, theirsName);
    d.show();
    if (d.isOK()) {
      return d.getSelectedPath();
    }
    return null;
  }
}
