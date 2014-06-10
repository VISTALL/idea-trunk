package com.intellij.seam.dependencies;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jdom.Element;

public class SeamDependenciesEditorProvider implements FileEditorProvider {


  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    return file.getFileSystem() instanceof SeamDependenciesVirtualFileSystem;
  }

  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    final String moduleName = file.getName();
    final Module moduleByName = ModuleManager.getInstance(project).findModuleByName(moduleName);
    return new SeamDependenciesFileEditor(moduleByName);
  }

  public void disposeEditor(@NotNull FileEditor editor) {
    Disposer.dispose(editor);
  }

  @NotNull
  public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
    return FileEditorState.INSTANCE;
  }

  public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
  }

  @NotNull
  @NonNls
  public String getEditorTypeId() {
    return "SeamDependenciesFileEditor";
  }

  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.NONE;
  }
}
