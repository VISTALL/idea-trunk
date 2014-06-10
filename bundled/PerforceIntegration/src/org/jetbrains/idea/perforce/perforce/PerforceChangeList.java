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
 * Time: 17:57:10
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeListImpl;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class PerforceChangeList implements CommittedChangeList {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.PerforceChangeList");

  private Date myDate;
  private long myNumber;
  private String myDescription;
  private String myUser;
  private String myClient;
  private final Project myProject;
  private P4Connection myConnection;
  private List<Change> myChanges;

  public PerforceChangeList(@NotNull final ChangeListData data, final Project project, final P4Connection connection) {
    myUser = data.USER;
    try {
      // Perforce before 2003.1 did not include date in 'p4 changes' output
      if (data.DATE.indexOf(':') >= 0) {
        myDate = ChangeListData.DATE_FORMAT.parse(data.DATE);
      }
      else {
        myDate = ChangeListData.DATE_ONLY_FORMAT.parse(data.DATE);
      }
    }
    catch (ParseException e) {
      LOG.error(e);
      myDate = new Date();
    }
    myNumber = data.NUMBER;
    myDescription = data.DESCRIPTION;
    myClient = data.CLIENT;

    myProject = project;
    myConnection = connection;
  }

  public PerforceChangeList(final Project project, final DataInput stream) throws IOException {
    myProject = project;
    readFromStream(stream);
  }

  public String getCommitterName() {
    return myUser;
  }

  public Date getCommitDate() {
    return myDate;
  }

  public Collection<Change> getChanges() {
    if (myChanges == null) {
      try {
        loadChanges();
      }
      catch (VcsException e) {
        myChanges = Collections.emptyList();
      }

    }
    return myChanges;
  }

  public void loadChanges() throws VcsException {
    if (myChanges != null) {
      return;
    }
    myChanges = new ArrayList<Change>();
    final List<PerforceChange> paths = PerforceRunner.getInstance(myProject).getChanges(myConnection, myNumber, null);
    for(PerforceChange path: paths) {
      final int type = path.getType();
      PerforceContentRevision beforeRevision = ((type == PerforceAbstractChange.ADD) || (type == PerforceAbstractChange.MOVE_ADD))
                                               ? null
                                               : createRevision(path.getDepotPath(), path.getRevision() - 1);

      PerforceContentRevision afterRevision = ((type == PerforceAbstractChange.DELETE) || (type == PerforceAbstractChange.MOVE_DELETE))
                                               ? null
                                               : createRevision(path.getDepotPath(), path.getRevision());

      myChanges.add(new Change(beforeRevision, afterRevision));
    }
  }

  private PerforceContentRevision createRevision(final String depotPath, final long revision) {
    return PerforceContentRevision.create(myProject, myConnection, depotPath, revision);
  }

  @NotNull
  public String getName() {
    return myDescription;
  }

  public String getComment() {
    return myDescription;
  }

  public long getNumber() {
    return myNumber;
  }

  public AbstractVcs getVcs() {
    return PerforceVcs.getInstance(myProject);
  }

  public Collection<Change> getChangesWithMovedTrees() {
    return CommittedChangeListImpl.getChangesWithMovedTreesImpl(this);
  }

  public String getClient() {
    return myClient;
  }

  public String toString() {
    return myDescription;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PerforceChangeList that = (PerforceChangeList)o;

    if (myNumber != that.myNumber) return false;
    if (myClient != null ? !myClient.equals(that.myClient) : that.myClient != null) return false;
    if (myDate != null ? !myDate.equals(that.myDate) : that.myDate != null) return false;
    if (myDescription != null ? !myDescription.equals(that.myDescription) : that.myDescription != null) return false;
    if (myUser != null ? !myUser.equals(that.myUser) : that.myUser != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myDate != null ? myDate.hashCode() : 0);
    result = 31 * result + (int)(myNumber ^ (myNumber >>> 32));
    result = 31 * result + (myDescription != null ? myDescription.hashCode() : 0);
    result = 31 * result + (myUser != null ? myUser.hashCode() : 0);
    result = 31 * result + (myClient != null ? myClient.hashCode() : 0);
    return result;
  }

  public void writeToStream(final DataOutput stream) throws IOException {
    stream.writeLong(myNumber);
    stream.writeLong(myDate.getTime());
    stream.writeUTF(myUser);
    stream.writeUTF(myClient);
    IOUtil.writeUTFTruncated(stream, myDescription);
    myConnection.getId().writeToStream(stream);
    Collection<Change> changes = getChanges();
    stream.writeInt(changes.size());
    for(Change change: changes) {
      PerforceContentRevision revision = (PerforceContentRevision) change.getAfterRevision();
      if (revision == null) {
        stream.writeByte(0);
        revision = (PerforceContentRevision) change.getBeforeRevision();
        assert revision != null;
      }
      else {
        stream.writeByte(change.getBeforeRevision() != null ? 1 : 2);
      }
      stream.writeLong(revision.getRevision());
      stream.writeUTF(revision.getDepotPath());
    }
  }

  private void readFromStream(final DataInput stream) throws IOException {
    myNumber = stream.readLong();
    myDate = new Date(stream.readLong());
    myUser = stream.readUTF();
    myClient = stream.readUTF();
    myDescription = stream.readUTF();
    ConnectionId id = ConnectionId.readFromStream(stream);
    myConnection = PerforceConnectionManager.getInstanceChecked(myProject).findConnectionById(id);
    int count = stream.readInt();
    myChanges = new ArrayList<Change>(count);
    for(int i=0; i<count; i++) {
      byte type = stream.readByte();
      long revision = stream.readLong();
      String path = stream.readUTF();
      Change change = null;
      switch(type) {
        case 0:
          change = new Change(createRevision(path, revision), null);
          break;
        case 1:
          change = new Change(createRevision(path, revision-1), createRevision(path, revision));
          break;
        case 2:
          change = new Change(null, createRevision(path, revision));
          break;
        default:
          assert false: "Unknown p4 change type " + type;
      }
      myChanges.add(change);
    }
  }
}
