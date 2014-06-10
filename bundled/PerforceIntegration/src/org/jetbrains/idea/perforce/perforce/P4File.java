/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.io.File;

public final class P4File {

  //
  // constructor data
  //
  private VirtualFile myVFile = null;
  private VirtualFile myParentDirOfDeleted = null;
  private String myName = null;

  //
  // cached values
  //
  private FStat myFstat = null;
  private String myLocalPath = null;

  @NonNls private static final String VF_CACHE = "P4_VF_CACHE";
  public static final Key<P4File> KEY = new Key<P4File>(VF_CACHE);
  @NonNls private static final String FILE_SEPARATOR_PROPERTY = "file.separator";

  public static P4File createInefficientFromLocalPath(final String localPath) {
    final P4File p4File = new P4File();
    p4File.myLocalPath = localPath;
    return p4File;
  }

  public static P4File create(@NotNull final VirtualFile vFile) {
    final Object userData = vFile.getUserData(KEY);
    final P4File p4File;
    if (userData != null) {
      p4File = (P4File)userData;
    }
    else {
      p4File = new P4File();
      p4File.myVFile = vFile;
      p4File.myVFile.putUserData(KEY, p4File);
    }
    p4File.myParentDirOfDeleted = null;
    return p4File;
  }

  public static P4File create(final FilePath filePath) {
    VirtualFile virtualFile = filePath.getVirtualFile();
    if (virtualFile != null) {
      return create(virtualFile);
    }
    else {
      P4File result = P4File.createInefficientFromLocalPath(filePath.getPath());
      result.myParentDirOfDeleted = filePath.getVirtualFileParent();
      result.myName = filePath.getName();
      return result;
    }
  }

  public static P4File create(@NotNull final File file) {
    VirtualFile virtualFile = findVirtualFile(file);
    if (virtualFile != null) {
      return create(virtualFile);
    }
    else {
      P4File result = P4File.createInefficientFromLocalPath(file.getAbsolutePath());
      result.myParentDirOfDeleted = findVirtualFile(file.getParentFile());
      result.myName = file.getName();
      return result;
    }
  }

  private static VirtualFile findVirtualFile(@NotNull final File file) {
    return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
      @Nullable
      public VirtualFile compute() {
        return LocalFileSystem.getInstance().findFileByIoFile(file);
      }
    });
  }

  private P4File() {
  }

  public String getAnyPath() {
    return getLocalPath();
  }

  public String getEscapedPath() {
    return escapeWildcards(getAnyPath());
  }

  public String getRecursivePath() {
    String filePath = getEscapedPath();
    if (isDirectory()) {
      return filePath + "/...";
    }
    return filePath;
  }

  public static String escapeWildcards(String path) {
    path = path.replace("%", "%25");
    path = path.replace("@", "%40");
    path = path.replace("#", "%23");
    return path;
  }

  public static String unescapeWildcards(String path) {
    path = path.replace("%23", "#");
    path = path.replace("%40", "@");
    path = path.replace("%25", "%");
    return path;
  }

  public void invalidateFstat() {
    myFstat = null;

    if (myVFile != null && myVFile.isValid()) {
      invalidateFstat(myVFile);
    }
  }

  public static void invalidateFstat(final Project project) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        final PerforceVcs perforceVcs = PerforceVcs.getInstance(project);
        final VirtualFile[] contentRoots = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(perforceVcs);
        for (VirtualFile contentRoot : contentRoots) {
          invalidateFstat(contentRoot);
        }
      }
    });
  }

  public static void invalidateFstat(final VirtualFile file) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        invalidateFStatImpl(file);
      }
    });
  }

  private static void invalidateFStatImpl(final VirtualFile m_vFile) {
    LocalFileSystem.getInstance().processCachedFilesInSubtree(m_vFile, new Processor<VirtualFile>() {
      public boolean process(final VirtualFile file) {
        file.putUserData(KEY, null);
        return true;
      }
    });
  }

  public void clearCache() {
    myFstat = null;
    if (myVFile != null) {
      myLocalPath = null;
    }
  }

  public FStat getFstat(final Project project, final boolean forceNew) throws VcsException {
    final PerforceManager perforceManager = PerforceManager.getInstance(project);
    final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
    final PerforceRunner perforceRunner = PerforceRunner.getInstance(project);
    return getFstat(perforceManager, changeListManager, perforceRunner, forceNew);
  }

  public FStat getFstat(final PerforceManager perforceManager, final ChangeListManager changeListManager, final PerforceRunner perforceRunner,
                        final boolean forceNew) throws VcsException {
    if (myFstat == null || forceNew ||
        myFstat.statTime < perforceManager.getLastValidTime()
        || perforceManager.getLastValidTime() == -1) {
      if (myVFile != null && changeListManager.isUnversioned(myVFile) && !forceNew) {
        myFstat = new FStat();
        myFstat.status = FStat.STATUS_NOT_ADDED;
      }
      else {
        // 7-8
        myFstat = perforceRunner.getProxy().fstat(this);
      }
    }
    return myFstat;
  }

  @NotNull
  public String getLocalPath() {
    if (myLocalPath == null) {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          if (myVFile != null) {
            if (myVFile.getParent() != null) {
              myLocalPath = myVFile.getPath();
            }
            else {
              if (myParentDirOfDeleted != null) {
                myLocalPath = myParentDirOfDeleted.getPath() + System.getProperty(FILE_SEPARATOR_PROPERTY) + myName;
              }
              else {
                throw new RuntimeException(PerforceBundle.message("exception.text.cannot.figure.out.local.path"));
              }
            }
          }
          else {
            // implement for any new file spec
            throw new RuntimeException(PerforceBundle.message("exception.text.not.implemented"));
          }
        }
      });
    }
    return myLocalPath;
  }

  @NonNls
  public String toString() {
    return "org.jetbrains.idea.perforce.perforce.P4File{" +
           "'" + getLocalPath() + "'" +
           "}";
  }

  public boolean isDirectory() {
    if (myVFile != null) {
      return myVFile.isDirectory();
    }
    return new File(getLocalPath()).isDirectory();
  }
}
