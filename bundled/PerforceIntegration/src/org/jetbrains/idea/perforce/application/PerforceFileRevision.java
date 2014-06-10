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

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.IOException;
import java.util.Date;

public class PerforceFileRevision implements VcsFileRevision {
  private final P4Revision myP4Revision;
  private final PerforceVcsRevisionNumber myNumber;
  private final PerforceSettings mySettings;
  private byte[] myContent;
  private final P4Connection myConnection;

  public PerforceFileRevision(P4Revision p4Revision,
                              PerforceSettings settings,
                              final P4Connection connection) {
    mySettings = settings;
    myP4Revision = p4Revision;
    myNumber = new PerforceVcsRevisionNumber(myP4Revision.getRevisionNumber(), myP4Revision.getChangeNumber(), myP4Revision.isBranched());
    myConnection = connection;
  }

  public VcsRevisionNumber getRevisionNumber() {
    return myNumber;
  }

  public Date getRevisionDate() {
    return myP4Revision.getDate();
  }

  public String getAuthor() {
    return myP4Revision.getUser();
  }

  public String getCommitMessage() {
    return myP4Revision.getSubmitMessage();
  }

  public void loadContent() throws VcsException {
    myContent = PerforceRunner.getInstance(mySettings.getProject()).getByteContent(myP4Revision.getDepotPath(),
                                                                                   String.valueOf(myP4Revision.getRevisionNumber()),
                                                                                   myConnection);
  }

  public byte[] getContent() throws IOException {
    return myContent;
  }

  public long getVersionNumber() {
    return myP4Revision.getRevisionNumber();
  }

  public String getAction() {
    return myP4Revision.getAction();
  }

  public String getClient() {
    return myP4Revision.getClient();
  }

  public String getBranchName() {
    return null;
  }

  public P4Connection getConnection() {
    return myConnection;
  }
}
