package org.jetbrains.idea.maven.vfs;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.DeprecatedVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class MavenPropertiesVirtualFile extends DeprecatedVirtualFile {
  private final String myPath;
  private final VirtualFileSystem myFS;
  private final byte[] myContent;

  public MavenPropertiesVirtualFile(String path, Properties properties, VirtualFileSystem FS) {
    myPath = path;
    myFS = FS;

    myContent = createContent(properties);
  }

  private byte[] createContent(Properties properties) {
    StringBuilder builder = new StringBuilder();
    TreeSet<String> sortedKeys = new TreeSet<String>((Set)properties.keySet());
    for (String each : sortedKeys) {
      builder.append(StringUtil.escapeProperty(each, true));
      builder.append("=");
      builder.append(StringUtil.escapeProperty(properties.getProperty(each), false));
      builder.append("\n");
    }
    return builder.toString().getBytes();
  }

  @NotNull
  public String getName() {
    return myPath;
  }

  @NotNull
  public VirtualFileSystem getFileSystem() {
    return myFS;
  }

  public String getPath() {
    return myPath;
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

  public VirtualFile getParent() {
    return null;
  }

  public VirtualFile[] getChildren() {
    return null;
  }

  @NotNull
  public byte[] contentsToByteArray() throws IOException {
    if (myContent == null) throw new IOException();
    return myContent;
  }

  public long getTimeStamp() {
    return -1;
  }

  @Override
  public long getModificationStamp() {
    return myContent.hashCode();
  }

  public long getLength() {
    return myContent.length;
  }

  public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
  }

  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(myContent);
  }

  @NotNull
  public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
    throw new UnsupportedOperationException();
  }
}
