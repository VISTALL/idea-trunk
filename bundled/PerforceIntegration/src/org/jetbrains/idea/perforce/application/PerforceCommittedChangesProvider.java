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

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.committed.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.changesBrowser.PerforceChangeBrowserSettings;
import org.jetbrains.idea.perforce.changesBrowser.PerforceVersionFilterComponent;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author yole
 */
public class PerforceCommittedChangesProvider implements CachingCommittedChangesProvider<PerforceChangeList, PerforceChangeBrowserSettings> {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceCommittedChangesProvider");
  
  private final Project myProject;
  private final PerforceRunner myRunner;
  private final MyZipper myZipper;
  private final ChangeListColumn[] myColumns = new ChangeListColumn[] {
    ChangeListColumn.NUMBER, ChangeListColumn.DATE, ChangeListColumn.NAME, new ClientColumn(), ChangeListColumn.DESCRIPTION };
  @NonNls private static final String IS_OPENED_SIGNATURE = "is opened and not being changed";

  public PerforceCommittedChangesProvider(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(myProject);
    myZipper = new MyZipper();
  }

  public PerforceChangeBrowserSettings createDefaultSettings() {
    return new PerforceChangeBrowserSettings();
  }

  public VcsCommittedListsZipper getZipper() {
    return myZipper;
  }

  private static class MyGroupCreator implements VcsCommittedListsZipperAdapter.GroupCreator {
    public Object createKey(final RepositoryLocation location) {
      final String url = ((DefaultRepositoryLocation) location).getLocation();
      final int idx = url.indexOf("://");
      return (idx == -1) ? url : url.substring(0, idx);
    }

    public RepositoryLocationGroup createGroup(final Object key, final Collection<RepositoryLocation> locations) {
      final RepositoryLocationGroup group = new RepositoryLocationGroup(key.toString());
      for (RepositoryLocation location : locations) {
        group.add(location);
      }
      return group;
    }
  }

  private static class MyZipper extends VcsCommittedListsZipperAdapter {
    private MyZipper() {
      super(new MyGroupCreator());
    }
  }

  public ChangesBrowserSettingsEditor<PerforceChangeBrowserSettings> createFilterUI(final boolean showDateFilter) {
    final List<P4Connection> allConnections =
      PerforceConnectionManager.getInstance(myProject).getAllConnections(PerforceSettings.getSettings(myProject));
    if (allConnections.size() == 1) {
      return new PerforceVersionFilterComponent(myProject, allConnections.get(0), showDateFilter);
    }
    // TODO: return composite editor
    return null;
  }

  public RepositoryLocation getLocationFor(final FilePath root) {
    return new DefaultRepositoryLocation(root.getPresentableUrl(), getRepositoryLocation(root));
  }

  public String getRepositoryLocation(final FilePath changedFile) {
    try {
      final P4Connection connection = PerforceSettings.getSettings(myProject).getConnectionForFile(changedFile.getIOFile());
      final P4WhereResult p4WhereResult = myRunner.where(P4File.create(changedFile), connection);
      final String serverAddress = PerforceManager.getInstance(myProject).getClient(connection).getServerPort();
      // todo why 'localhost:////depot/ ????'
      return serverAddress + "://" + p4WhereResult.getDepot();
    }
    catch (VcsException e) {
      return changedFile.getPresentableUrl();
    }
  }
  
  public String getRevisionNumber(final ContentRevision revision, final AbstractVcs vcs, final Project project) {
    return revision.getRevisionNumber().asString();
  }


  public RepositoryLocation getLocationFor(final FilePath root, final String repositoryPath) {
    return getLocationFor(root);
  }

  public List<PerforceChangeList> getCommittedChanges(PerforceChangeBrowserSettings settings, RepositoryLocation location, final int maxCount) throws VcsException {
    List<PerforceChangeList> changeLists = new ArrayList<PerforceChangeList>();
    if (!PerforceSettings.getSettingsChecked(myProject).ENABLED) {
      return changeLists;
    }
    String url = ((DefaultRepositoryLocation) location).getURL();

    final String client = settings.getClientFilter();
    final String user = settings.getUserFilter();
    changeLists.addAll(myRunner.getSubmittedChangeLists(client, user, P4File.create(new File(url)),
                                                        settings.getDateAfterFilter(), settings.getDateBeforeFilter(),
                                                        settings.getChangeAfterFilter(), settings.getChangeBeforeFilter(), maxCount));
    LOG.info("Changelists from Perforce: " + changeLists.size());
    settings.filterChanges(changeLists);
    LOG.info("Changelists after filtering: " + changeLists.size());
    return changeLists;
  }

  public ChangeListColumn[] getColumns() {
    return myColumns;
  }

  @Nullable
  public VcsCommittedViewAuxiliary createActions(final DecoratorManager manager, final RepositoryLocation location) {
    return null;
  }

  public int getUnlimitedCountValue() {
    return 0;
  }

  public int getFormatVersion() {
    return 2;
  }

  public void writeChangeList(final DataOutput stream, final PerforceChangeList list) throws IOException {
    list.writeToStream(stream);
  }

  public PerforceChangeList readChangeList(final RepositoryLocation location, final DataInput stream) throws IOException {
    return new PerforceChangeList(myProject, stream);
  }

  public boolean isMaxCountSupported() {
    return true;
  }

  public Collection<FilePath> getIncomingFiles(final RepositoryLocation location) throws VcsException {
    final DefaultRepositoryLocation repLocation = (DefaultRepositoryLocation) location;
    final P4File file = P4File.create(new File(repLocation.getURL()));
    ExecResult result = myRunner.previewSync(file);
    if (result.getExitCode() != 0 || result.getStderr().length() > 0) {
      if (result.getStderr().indexOf(PerforceRunner.FILES_UP_TO_DATE) >= 0) {
        return Collections.emptyList();
      }
      throw new VcsException("Error refreshing incoming changes: rc=" + result.getExitCode() + ", stderr=" + result.getStderr());
    }
    final P4Connection connection = PerforceConnectionManager.getInstanceChecked(myProject).getConnectionForFile(file);
    PerforceClient client = null;
    final String clientRoot = PerforceManager.getInstanceChecked(myProject).getClientRoot(connection);
    List<FilePath> files = new ArrayList<FilePath>();
    String[] lines = LineTokenizer.tokenize(result.getStdout(), false);
    for(String line: lines) {
      int pos = clientRoot == null ? -1 : line.indexOf(clientRoot);
      if (pos >= 0) {
        final File localFile = new File(line.substring(pos));
        LOG.info("Incoming file: " + line.substring(pos));
        files.add(VcsContextFactory.SERVICE.getInstance().createFilePathOn(localFile));
      }
      else if (line.indexOf(IS_OPENED_SIGNATURE) >= 0) {
        pos = line.indexOf(" - ");
        if (pos >= 0) {
          String depotPath = line.substring(0, pos);
          if (client == null) {
            client = PerforceManager.getInstanceChecked(myProject).getClient(connection);
          }
          final File localPath = PerforceManager.getFileByDepotName(depotPath, client);
          if (localPath != null) {
            files.add(VcsContextFactory.SERVICE.getInstance().createFilePathOn(localPath));
          }
        }
      }
      else {
        LOG.info("Unknown line in incoming files: " + line);
      }
    }
    return files;
  }

  public boolean refreshCacheByNumber() {
    return true;
  }

  public String getChangelistTitle() {
    return PerforceBundle.message("changes.browser.changelist.term");
  }

  public boolean isChangeLocallyAvailable(FilePath filePath, @Nullable VcsRevisionNumber localRevision, VcsRevisionNumber changeRevision,
                                          final PerforceChangeList changeList) {
    return localRevision != null && localRevision.compareTo(changeRevision) >= 0;
  }

  public boolean refreshIncomingWithCommitted() {
    return false;
  }

  private static class ClientColumn extends ChangeListColumn<PerforceChangeList> {
    public String getTitle() {
      return PerforceBundle.message("changes.browser.client.column.name");
    }

    public Object getValue(final PerforceChangeList changeList) {
      return changeList.getClient();
    }

    public Comparator<PerforceChangeList> getComparator() {
      return new Comparator<PerforceChangeList>() {
        public int compare(final PerforceChangeList o1, final PerforceChangeList o2) {
          return o1.getClient().compareTo(o2.getClient());
        }
      };
    }
  }
}
