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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.application.annotation.AnnotationInfo;
import org.jetbrains.idea.perforce.application.annotation.PerforceFileAnnotation;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

public class PerforceAnnotationProvider implements AnnotationProvider{
  private final Project myProject;
  private final PerforceRunner myRunner;

  public PerforceAnnotationProvider(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  public FileAnnotation annotate(VirtualFile file) throws VcsException {
    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
    long revNumber = PerforceRunner.getInstance(myProject).haveRevision(P4File.create(file));
    return doAnnotate(file, ChangesUtil.getCommittedPath(myProject, filePath), revNumber);
  }

  private FileAnnotation doAnnotate(final VirtualFile vFile, final FilePath file, final long revision) throws VcsException {
    final P4File p4File = P4File.create(file);
    final AnnotationInfo annotationInfo = myRunner.annotate(p4File, revision);
    final P4Revision[] fileLog = myRunner.filelog(p4File, true);
    return new PerforceFileAnnotation(annotationInfo, vFile, fileLog, myProject);
  }

  public FileAnnotation annotate(VirtualFile file, VcsFileRevision revision) throws VcsException {
    PerforceVcsRevisionNumber number = (PerforceVcsRevisionNumber) revision.getRevisionNumber();
    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
    return doAnnotate(file, filePath, number.getRevisionNumber());
  }

  public boolean isAnnotationValid( VcsFileRevision rev ){
    return true;
  }
}
