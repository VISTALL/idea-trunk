package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceBinaryContentRevision;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yole
 */
public class PerforceCachingContentRevision extends PerforceContentRevision {
  private final FilePath myCurrentPath;   // for renames - path after rename
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision");

  private static final FileAttribute PERFORCE_REVISION_ATTRIBUTE = new FileAttribute("p4.revision", 1);
  private static final FileAttribute PERFORCE_CONTENT_ATTRIBUTE = new FileAttribute("p4.content", 1);

  private PerforceCachingContentRevision(final Project project, final FilePath path, final FilePath currentPath, final long revision) {
    super(project, path, revision);
    myCurrentPath = currentPath;
  }

  @Override @Nullable
  protected String loadContent() throws VcsException {
    assert myFilePath != null;
    VirtualFile vFile = myCurrentPath.getVirtualFile();
    if (vFile == null) return super.loadContent();
    String content = null;
    try {
      content = loadCachedContent(vFile, myRevision);
    }
    catch (IOException e) {
      // ignore
    }
    if (content == null && PerforceSettings.getSettings(myProject).ENABLED) {
      content = super.loadContent();
      if (content != null) {
        try {
          saveCachedContent(vFile, myRevision, content);
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    }
    return content;
  }

  @Nullable
  public static String loadCachedContent(final VirtualFile vFile, final long revision) throws IOException {
    Long cachedRevision = readCachedRevision(vFile);
    if (cachedRevision == null || cachedRevision.longValue() != revision) return null;
    byte[] bytes = PERFORCE_CONTENT_ATTRIBUTE.readAttributeBytes(vFile);
    if (bytes == null) return null;
    return CharsetToolkit.UTF8_CHARSET.decode(ByteBuffer.wrap(bytes)).toString();
  }

  @Nullable
  private static Long readCachedRevision(final VirtualFile vFile) throws IOException {
    DataInputStream inputStream = PERFORCE_REVISION_ATTRIBUTE.readAttribute(vFile);
    if (inputStream == null) return null;
    try {
      return inputStream.readLong();
    }
    finally {
      inputStream.close();
    }
  }

  public static void saveCachedContent(final VirtualFile vFile, final long revision, final String content) throws IOException {
    writeRevision(vFile, revision);
    PERFORCE_CONTENT_ATTRIBUTE.writeAttributeBytes(vFile, CharsetToolkit.getUtf8Bytes(content));
  }

  public static void saveCurrentContent(final VirtualFile vFile) {
    try {
      writeRevision(vFile, -1);
      PERFORCE_CONTENT_ATTRIBUTE.writeAttributeBytes(vFile, vFile.contentsToByteArray());
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  private static void writeRevision(final VirtualFile vFile, final long revision) throws IOException {
    DataOutputStream stream = PERFORCE_REVISION_ATTRIBUTE.writeAttribute(vFile);
    try {
      stream.writeLong(revision);
    }
    finally {
      stream.close();
    }
  }

  public static ContentRevision create(final Project project, final FilePath path, final long haveRevision) {
    if (path.getFileType().isBinary()) {
      return new PerforceBinaryContentRevision(project, path, haveRevision);
    }
    return new PerforceCachingContentRevision(project, path, path, haveRevision);
  }

  public static ContentRevision create(final Project project, final FilePath path, final FilePath currentPath, final long haveRevision) {
    if (path.getFileType().isBinary()) {
      return new PerforceBinaryContentRevision(project, path, haveRevision);
    }
    return new PerforceCachingContentRevision(project, path, currentPath, haveRevision);
  }
}
