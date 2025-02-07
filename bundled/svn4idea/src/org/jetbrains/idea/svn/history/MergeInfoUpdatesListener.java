package org.jetbrains.idea.svn.history;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsListener;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.integrate.Merger;
import org.jetbrains.idea.svn.dialogs.SvnMapDialog;
import org.jetbrains.idea.svn.mergeinfo.SvnMergeInfoCache;

import java.util.ArrayList;
import java.util.List;

public class MergeInfoUpdatesListener {
  private final Project myProject;
  private final MessageBusConnection myConnection;
  private List<RootsAndBranches> myMergeInfoRefreshActions;
  private final ZipperUpdater myUpdater;

  public MergeInfoUpdatesListener(final Project project, final MessageBusConnection connection) {
    myConnection = connection;
    myProject = project;
    myUpdater = new ZipperUpdater();
  }

  public void addPanel(final RootsAndBranches action) {
    if (myMergeInfoRefreshActions == null) {
      myMergeInfoRefreshActions = new ArrayList<RootsAndBranches>();
      myMergeInfoRefreshActions.add(action);

      myConnection.subscribe(VcsConfigurationChangeListener.BRANCHES_CHANGED, new VcsConfigurationChangeListener.Notification() {
        public void execute(final Project project, final VirtualFile vcsRoot) {
          callReloadMergeInfo();
        }
      });
      final Runnable reloadRunnable = new Runnable() {
        public void run() {
          callReloadMergeInfo();
        }
      };
      myConnection.subscribe(SvnMapDialog.WC_CONVERTED, reloadRunnable);
      myConnection.subscribe(RootsAndBranches.REFRESH_REQUEST, reloadRunnable);

      myConnection.subscribe(SvnVcs.ROOTS_RELOADED, reloadRunnable);

      ProjectLevelVcsManager.getInstance(myProject).addVcsListener(new VcsListener() {
        public void directoryMappingChanged() {
          callReloadMergeInfo();
        }
      });

      myConnection.subscribe(CommittedChangesTreeBrowser.ITEMS_RELOADED, reloadRunnable);

      myConnection.subscribe(SvnMergeInfoCache.SVN_MERGE_INFO_CACHE, new SvnMergeInfoCache.SvnMergeInfoCacheListener() {
        public void copyRevisionUpdated() {
          doForEachInitialized(new Consumer<RootsAndBranches>() {
            public void consume(final RootsAndBranches rootsAndBranches) {
              rootsAndBranches.fireRepaint();
            }
          });
        }
      });

      myConnection.subscribe(Merger.COMMITTED_CHANGES_MERGED_STATE, new Merger.CommittedChangesMergedStateChanged() {
        public void event(final List<CommittedChangeList> list) {
          doForEachInitialized(new Consumer<RootsAndBranches>() {
            public void consume(RootsAndBranches rootsAndBranches) {
              rootsAndBranches.refreshByLists(list);
            }
          });
        }
      });
    } else {
      myMergeInfoRefreshActions.add(action);
    }
  }

  private void doForEachInitialized(final Consumer<RootsAndBranches> consumer) {
    myUpdater.queue(new Runnable() {
      public void run() {
        for (final RootsAndBranches action : myMergeInfoRefreshActions) {
          if (action.strategyInitialized()) {
            if (ApplicationManager.getApplication().isDispatchThread()) {
              consumer.consume(action);
            } else {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                  consumer.consume(action);
                }
              });
            }
          }
        }
      }
    });
  }
  
  private void callReloadMergeInfo() {
    doForEachInitialized(new Consumer<RootsAndBranches>() {
      public void consume(final RootsAndBranches rootsAndBranches) {
        rootsAndBranches.reloadPanels();
        rootsAndBranches.refresh();
      }
    });
  }

  public void removePanel(final RootsAndBranches action) {
    if (myMergeInfoRefreshActions != null) {
      myMergeInfoRefreshActions.remove(action);
    }
  }
}
