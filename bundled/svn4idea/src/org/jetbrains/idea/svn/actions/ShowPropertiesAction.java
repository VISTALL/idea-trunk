package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowAnchor;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.dialogs.PropertiesComponent;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;

public class ShowPropertiesAction extends BasicAction {

  protected String getActionName(AbstractVcs vcs) {
    return "Show Properties";
  }

  protected boolean needsAllFiles() {
    return false;
  }

  protected boolean isEnabled(Project project, SvnVcs vcs, VirtualFile file) {
    if (!file.isDirectory()) {
      file = file.getParent();
    }
    return file != null && file.getPath() != null && SVNWCUtil.isVersionedDirectory(new File(file.getPath()));
  }

  protected boolean needsFiles() {
    return true;
  }

  protected void perform(Project project, SvnVcs activeVcs, VirtualFile file, DataContext context)
    throws VcsException {
    batchPerform(project, activeVcs, new VirtualFile[]{file}, context);
  }

  protected void batchPerform(Project project, final SvnVcs activeVcs, VirtualFile[] file, DataContext context) throws VcsException {
    final File[] ioFiles = new File[file.length];
    for (int i = 0; i < ioFiles.length; i++) {
      ioFiles[i] = new File(file[i].getPath());
    }
    if (ioFiles.length > 0) {
      ToolWindow w = ToolWindowManager.getInstance(project).getToolWindow(PropertiesComponent.ID);
      PropertiesComponent component = null;
      if (w == null) {
        component = new PropertiesComponent();
        w = ToolWindowManager.getInstance(project).registerToolWindow(PropertiesComponent.ID, component, ToolWindowAnchor.BOTTOM);
      } else {
        component = ((PropertiesComponent) w.getContentManager().getContents()[0].getComponent());
      }
      w.setTitle(ioFiles[0].getName());
      w.show(null);
      final PropertiesComponent comp = component;
      w.activate(new Runnable() {
        public void run() {
          comp.setFile(activeVcs, ioFiles[0]);
        }
      });
    }

  }

  protected boolean isBatchAction() {
    return false;
  }
}
