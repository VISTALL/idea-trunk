package com.intellij.seam.dependencies;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.seam.constants.SeamDataKeys;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.SeamIcons;

public class ShowSeamDependenciesGraph extends AnAction {

  public void update(final AnActionEvent e) {
    final SeamFacet facet = SeamDataKeys.SEAM_FACET.getData(e.getDataContext());

    e.getPresentation().setVisible(facet != null);
    e.getPresentation().setEnabled(facet != null);
    e.getPresentation().setIcon(SeamIcons.SEAM_ICON);
  }

  public void actionPerformed(AnActionEvent e) {
    final SeamFacet facet = SeamDataKeys.SEAM_FACET.getData(e.getDataContext());
    assert facet != null;

    String moduleName = facet.getModule().getName();

    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(
      SeamDependenciesVirtualFileSystem.PROTOCOL + "://" + moduleName);
    FileEditorManager.getInstance(facet.getModule().getProject()).openFile(virtualFile, true);
  }
}
