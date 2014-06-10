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

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 27.11.2006
 * Time: 18:20:03
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceBinaryContentRevision;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;

public class PerforceContentRevision implements ContentRevision {
  private String myContent;
  protected final Project myProject;
  private final P4Connection myConnection;
  private final String myDepotPath;
  protected final long myRevision;
  protected FilePath myFilePath;

  protected PerforceContentRevision(final Project project, final P4Connection connection, final String depotPath, final long revision) {
    myProject = project;
    myConnection = connection;
    myDepotPath = depotPath;
    myRevision = revision;
  }

  public PerforceContentRevision(final Project project, final FilePath path, final long revision) {
    myProject = project;
    myDepotPath = null;
    myFilePath = path;
    myRevision = revision;
    myConnection = null;
  }

  @Nullable
  public String getContent() throws VcsException {
    if (myContent == null) {
      myContent = loadContent();
    }
    return myContent;
  }                                                                      

  protected String loadContent() throws VcsException {
    if (myDepotPath != null) {
      return PerforceRunner.getInstance(myProject).getContent(myDepotPath, myRevision, myConnection);
    }
    else {
      final P4File p4File = P4File.create(myFilePath);
      return PerforceRunner.getInstance(myProject).getContent(p4File, myRevision);
    }
  }

  @NotNull
  public FilePath getFile() {
    if (myFilePath == null) {
      if ((! myProject.isDisposed()) && PerforceSettings.getSettings(myProject).ENABLED) {
        PerforceClient client = PerforceManager.getInstance(myProject).getClient(myConnection);
        File file;
        try {
          file = PerforceManager.getFileByDepotName(myDepotPath, client);
        }
        catch (VcsException e) {
          file = null;
        }
        if (file != null) {
          myFilePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
        }
      }
      if (myFilePath == null) {
        myFilePath = VcsContextFactory.SERVICE.getInstance().createFilePathOnNonLocal(myDepotPath, false);
      }
    }
    return myFilePath;
  }

  @NotNull
  public VcsRevisionNumber getRevisionNumber() {
    return new VcsRevisionNumber.Long(myRevision);
  }

  public String getDepotPath() {
    return myDepotPath;
  }

  public long getRevision() {
    return myRevision;
  }

  public static PerforceContentRevision create(final Project project, P4Connection connection, String depotPath, final long revision) {
    int fileNamePos = depotPath.lastIndexOf('/');
    if (fileNamePos >= 0) {
      String fileName = depotPath.substring(fileNamePos);
      final FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
      if (fileType.isBinary()) {
        return new PerforceBinaryContentRevision(project, connection, depotPath, revision);
      }
    }
    
    return new PerforceContentRevision(project, connection, depotPath, revision);
  }
}