package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnStatusUtil;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;

import java.util.Arrays;
import java.util.List;

public class ShareWholeProject extends AnAction {
  @Override
  public void update(final AnActionEvent e) {
    final MyChecker checker = new MyChecker();
    checker.execute(e);

    final Presentation presentation = e.getPresentation();
    presentation.setEnabled(checker.isEnabled());
    
    presentation.setVisible(checker.isVisible());
    if (checker.isEnabled()) {
      presentation.setText(SvnBundle.message("action.share.whole.project.text"));
    }
  }

  private static class MyChecker {
    private boolean myEnabled;
    private boolean myVisible;
    private Project myProject;
    private boolean myHadNoMappings;

    public void execute(final AnActionEvent e) {
      final DataContext dataContext = e.getDataContext();
      myProject = PlatformDataKeys.PROJECT.getData(dataContext);
      if (myProject == null || myProject.isDefault()) {
        // remain false
        return;
      }
      final VirtualFile baseDir = myProject.getBaseDir();
      if (baseDir == null) return;

      final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
      final MyCheckResult result = checkMappings(baseDir, vcsManager);

      if (MyCheckResult.disable.equals(result)) return;

      myHadNoMappings = MyCheckResult.notMapped.equals(result);
      if (MyCheckResult.notMapped.equals(result)) {
        // no change list manager working
        if(SvnUtil.seemsLikeVersionedDir(baseDir)) return;
      } else if (SvnStatusUtil.isUnderControl(myProject, baseDir)) {
        return;
      }

      if ((! myHadNoMappings) && (! SvnVcs.getInstance(myProject).getSvnFileUrlMapping().isEmpty())) {
        // there are some versioned dirs under project dir
        return;
      }

      // visible: already checked above
      myVisible = true;
      myEnabled = (! vcsManager.isBackgroundVcsOperationRunning());
    }

    private static enum MyCheckResult {
      disable,
      notMapped,
      rootToSvn;
    }

    private MyCheckResult checkMappings(final VirtualFile baseDir, final ProjectLevelVcsManager vcsManager) {
      final List<VcsDirectoryMapping> mappings = vcsManager.getDirectoryMappings();

      boolean notMapped = true;
      boolean svnMappedToBase = false;
      for (VcsDirectoryMapping mapping : mappings) {
        final String vcs = mapping.getVcs();
        if (vcs != null && vcs.length() > 0) {
          notMapped = false;
          if (SvnVcs.VCS_NAME.equals(vcs)) {
            if (mapping.isDefaultMapping() || baseDir.equals(mapping.getDirectory())) {
              svnMappedToBase = true;
              break;
            }
          }
        }
      }

      return svnMappedToBase ? MyCheckResult.rootToSvn :
             (notMapped ? MyCheckResult.notMapped : MyCheckResult.disable);
    }

    public boolean isEnabled() {
      return myEnabled;
    }

    public boolean isVisible() {
      return myVisible;
    }

    public Project getProject() {
      return myProject;
    }

    public boolean isHadNoMappings() {
      return myHadNoMappings;
    }
  }


  public void actionPerformed(AnActionEvent e) {
    final MyChecker checker = new MyChecker();
    checker.execute(e);
    if (! checker.isEnabled()) return;

    final Project project = checker.getProject();
    final VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null) return;
    boolean success = false;
    boolean excThrown = false;
    try {
      success = ShareProjectAction.share(project, baseDir);
    } catch (VcsException exc) {
      AbstractVcsHelper.getInstance(project).showError(exc, "Failed to Share Project");
      excThrown = true;
    } finally {
      // if success = false -> either action was cancelled or exception was thrown, so also check for exception
      if (success || excThrown) {
        baseDir.refresh(true, true, new Runnable() {
          public void run() {
            VcsDirtyScopeManager.getInstance(project).dirDirtyRecursively(project.getBaseDir());
            if (checker.isHadNoMappings() && SvnUtil.seemsLikeVersionedDir(baseDir)) {
              final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
              vcsManager.setDirectoryMappings(Arrays.asList(new VcsDirectoryMapping("", SvnVcs.VCS_NAME)));
            }
          }
        });
      }
    }
  }
}
