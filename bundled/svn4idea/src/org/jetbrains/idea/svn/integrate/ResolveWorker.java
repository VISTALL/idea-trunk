package org.jetbrains.idea.svn.integrate;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdateFilesHelper;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.actions.SvnMergeProvider;
import org.jetbrains.idea.svn.update.SvnUpdateGroups;

import java.util.ArrayList;
import java.util.List;

public class ResolveWorker {
  private final boolean myChangesUnderProjectRoot;
  private final Project myProject;
  private final VcsDirtyScopeManager myDirtyScopeManager;
  private List<VirtualFile> myConflictedVirtualFiles;

  public ResolveWorker(final boolean changesUnderProjectRoot, final Project project) {
    myChangesUnderProjectRoot = changesUnderProjectRoot;
    myProject = project;
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(project);
  }

  private void refreshChangeListsFindConflicts(final UpdatedFiles updatedFiles) {
    UpdateFilesHelper.iterateFileGroupFiles(updatedFiles,
                                            new UpdateFilesHelper.Callback() {
                                              public void onFile(final String filePath, final String groupId) {
                                                final VirtualFile vf = SvnUtil.getVirtualFile(filePath);
                                                if (vf != null) {
                                                  // refresh base directory so that conflict files should be detected
                                                  // file itself is already refreshed
                                                  vf.getParent().refresh(false, false);
                                                  myDirtyScopeManager.fileDirty(vf);
                                                }
                                                if (FileGroup.MERGED_WITH_CONFLICT_ID.equals(groupId)) {
                                                  myConflictedVirtualFiles.add(vf);
                                                }
                                              }
                                            });
  }

  public boolean needsInteraction(final UpdatedFiles updatedFiles) {
    myConflictedVirtualFiles = new ArrayList<VirtualFile>();

    if (myChangesUnderProjectRoot) {
      refreshChangeListsFindConflicts(updatedFiles);
    } else {
      final FileGroup conflictedGroup = updatedFiles.getGroupById(FileGroup.MERGED_WITH_CONFLICT_ID);
      for (String filename : conflictedGroup.getFiles()) {
        final VirtualFile vf = SvnUtil.getVirtualFile(filename);
        myConflictedVirtualFiles.add(vf);
      }
    }

    return ((! myConflictedVirtualFiles.isEmpty()) || (! haveUnresolvedConflicts(updatedFiles))) &&
           (! SvnConfiguration.getInstanceChecked(myProject).MERGE_DRY_RUN);
  }

  public static boolean haveUnresolvedConflicts(final UpdatedFiles updatedFiles) {
    final String[] ids = new String[] {FileGroup.MERGED_WITH_CONFLICT_ID, FileGroup.MERGED_WITH_PROPERTY_CONFLICT_ID,
      SvnUpdateGroups.MERGED_WITH_TREE_CONFLICT};
    for (String id : ids) {
      final FileGroup group = updatedFiles.getGroupById(id);
      if ((group != null) && (! group.isEmpty())) return true;
    }
    return false;
  }

  // on EDT, dispose checked
  public void execute(final UpdatedFiles updatedFiles) {
    if (myConflictedVirtualFiles.isEmpty()) {
      return;
    }
    final AbstractVcsHelper vcsHelper = AbstractVcsHelper.getInstance(myProject);

    List<VirtualFile> mergedFiles = vcsHelper.showMergeDialog(myConflictedVirtualFiles, new SvnMergeProvider(myProject));

    final FileGroup mergedGroup = updatedFiles.getGroupById(FileGroup.MERGED_ID);
    final FileGroup conflictedGroup = updatedFiles.getGroupById(FileGroup.MERGED_WITH_CONFLICT_ID);
    final VcsKey vcsKey = SvnVcs.getKey();

    for (final VirtualFile mergedFile : mergedFiles) {
      String path = FileUtil.toSystemDependentName(mergedFile.getPresentableUrl());
      conflictedGroup.remove(path);
      mergedGroup.add(path, vcsKey, null);

      mergedFile.refresh(false, false);
      // for additionally created files removal to be detected
      mergedFile.getParent().refresh(false, false);

      if (myChangesUnderProjectRoot) {
        myDirtyScopeManager.fileDirty(mergedFile);
      }
    }
  }
}
