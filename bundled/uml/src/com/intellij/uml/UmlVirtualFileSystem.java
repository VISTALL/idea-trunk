/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.uml;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlIcons;
import com.intellij.uml.utils.VcsUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author Konstantin Bulenkov
 */
public class UmlVirtualFileSystem extends DeprecatedVirtualFileSystem {
  @NonNls public static final String PROTOCOL = "uml";
  @NonNls public static final String PROTOCOL_PREFIX = "uml://";
  @NonNls public static final String CHANGES = PROTOCOL_PREFIX + "$SHOW_CHANGES$/";  

  @NotNull
  public String getProtocol() {
    return PROTOCOL;
  }

  @Nullable
  public VirtualFile findFileByPath(@NotNull String path) {
    return new UmlVirtualFile(path);
  }

  public void refresh(boolean asynchronous) {
  }

  public static boolean isInitialized(VirtualFile vf) {
    return vf instanceof UmlVirtualFile && ((UmlVirtualFile)vf).isInitialized();
  }

  public static void setInitialized(VirtualFile vf) {
    if (vf instanceof UmlVirtualFile) {
      ((UmlVirtualFile)vf).setInitialized(true);
    }
  }

  public static String getFileUrlByChangeList(@NotNull LocalChangeList changeList) {
    return CHANGES + changeList.getId() + "/" + changeList.getName();
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

  public static boolean isUmlVirtualFile(VirtualFile file) {
    return file instanceof UmlVirtualFile;
  }

  public class UmlVirtualFile extends DeprecatedVirtualFile {
    private final String myName;
    private boolean initialized = false;
    private String myPresentableName;

    public UmlVirtualFile(final @NotNull String name) {
      myName = name;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @Nullable
    public UmlProvider getUmlProvider() {
      final int ind = myName.indexOf('/');
      if (ind == -1) return null;
      return UmlProvider.findByID(myName.substring(0, ind));
    }

    @Nullable
    public String getFQN() {
      final int ind = myName.lastIndexOf('/');
      if (ind == -1) return null;
      return myName.substring(ind + 1);
    }

    public String getShortName() {
      if (myPresentableName != null) return myPresentableName;
      int ind;
      return ((ind = myName.lastIndexOf('.')) < 0) ? myName : myName.substring(++ind); 
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
      return UmlVirtualFileSystem.this;
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
      return EMPTY_ARRAY;
    }

    @NotNull
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
      throw new UnsupportedOperationException("getOutputStream is not implemented in : " + getClass());
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
      throw new UnsupportedOperationException("contentsToByteArray is not implemented in : " + getClass());
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
      return UmlIcons.UML_ICON;
    }

    public String getPresentableName() {
      if (getUmlProvider() == null) {
             return VcsUtils.isShowChangesFile(this)
             ? UmlBundle.message("uml.file.name.for.changes", getChangeListName())
             : UmlBundle.message("uml.class.diagramm.file.name", getShortName());
      } else {
        return VcsUtils.isShowChangesFile(this)
        ? UmlBundle.message("uml.file.name.for.changes", getChangeListName())
        : getShortName();
      }
    }

    private String getChangeListName() {
      return getPath().substring(CHANGES.length()).split("/")[1];
    }

    @Override
    public boolean equals(final Object obj) {
      return obj instanceof UmlVirtualFile && myName.equals(((UmlVirtualFile)obj).getName());
    }

    @Override
    public int hashCode() {
      return myName.hashCode();
    }

    @NotNull
    @Override
    public FileType getFileType() {
      return UmlFileType.TYPE;
    }

    public boolean isInitialized() {
      return initialized;
    }

    public void setInitialized(final boolean initialized) {
      this.initialized = initialized;
    }

    public void setPresentableName(String presentableName) {
      myPresentableName = presentableName;
    }
  }

  public static class UmlFileType implements FileType {
    public static UmlFileType TYPE = new UmlFileType();

    @NotNull
    public String getName() {
      return "Uml File Type";
    }

    @NotNull
    public String getDescription() {
      return "";
    }

    @NotNull
    public String getDefaultExtension() {
      return "there_is_no_default_extension";
    }

    public Icon getIcon() {
      return null;
    }

    public boolean isBinary() {
      return true;
    }

    public boolean isReadOnly() {
      return true;
    }

    public String getCharset(@NotNull final VirtualFile file, final byte[] content) {
      return Charset.defaultCharset().name();
    }
  }
}