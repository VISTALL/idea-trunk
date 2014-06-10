package com.intellij.seam.dependencies;

import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SeamDependenciesVirtualFileSystem extends DeprecatedVirtualFileSystem {
  @NonNls static final String PROTOCOL = "SEAM_DEPENDENCIES";
  @NonNls static final String FILE_GRAPH = "SEAM_DEPENDENCIES_GRAPH";

  @NotNull
  public String getProtocol() {
    return PROTOCOL;
  }

  @Nullable
  public VirtualFile findFileByPath(@NotNull String path) {
   return new MyVirtualFile(path);
  }

  public void refresh(boolean asynchronous) {
  }

  @Nullable
  public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
    return findFileByPath(path);
  }

  public void deleteFile(Object requestor, @NotNull VirtualFile vFile) throws IOException {
    throw new UnsupportedOperationException("deleteFile is not implemented in : " + getClass());
  }

  public void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) throws IOException {
    throw new UnsupportedOperationException("moveFile is not implemented in : " + getClass());
  }

  public VirtualFile copyFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent, @NotNull final String copyName) throws IOException {
    throw new UnsupportedOperationException("copyFile is not implemented in : " + getClass());
  }

  public void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) throws IOException {
    throw new UnsupportedOperationException("renameFile is not implemented in : " + getClass());
  }

  public VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) throws IOException {
    throw new UnsupportedOperationException("createChildFile is not implemented in : " + getClass());
  }

  public VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) throws IOException {
    throw new UnsupportedOperationException("createChildDirectory is not implemented in : " + getClass());
  }

  private class MyVirtualFile extends DeprecatedVirtualFile {
    private final String myName;

    public MyVirtualFile(final String name) {

      myName = name;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
      return SeamDependenciesVirtualFileSystem.this;
    }

    public String getPath() {
      return getName();
    }

    public boolean isWritable() {
      return false;
    }

    public boolean isDirectory() {
      return false;
    }

    public boolean isValid() {
      return true;
    }

    @Nullable
    public VirtualFile getParent() {
      return null;
    }

    public VirtualFile[] getChildren() {
      return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
      throw new UnsupportedOperationException("getOutputStream is not implemented in : " + getClass());
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
      return ArrayUtil.EMPTY_BYTE_ARRAY;
    }

    public long getTimeStamp() {
      return 0;
    }

    public long getLength() {
      return 0;
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {

    }

    public InputStream getInputStream() throws IOException {
      throw new UnsupportedOperationException("getInputStream is not implemented in : " + getClass());
    }

    public long getModificationStamp() {
      return 0;
    }

    public Icon getIcon() {
      return SeamIcons.SEAM_ICON;
    }

    public String getPresentableName() {
      return SeamBundle.message("seam.dependencies.file.name", getName());
    }
  }
}
