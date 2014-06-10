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
package org.jetbrains.idea.perforce.application.annotation;

import com.intellij.openapi.editor.EditorGutterAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.annotate.*;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.*;
import com.intellij.util.text.SyncDateFormat;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.application.PerforceVcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class PerforceFileAnnotation implements FileAnnotation {
  private final AnnotationInfo myAnnotationInfo;
  private final List<AnnotationListener> myListeners = new ArrayList<AnnotationListener>();
  private final P4Revision[] myRevisions;
  private final VirtualFile myFile;

  private final LineAnnotationAspect REVISION = new PerforceRevisionAnnotationAspect();

  private final LineAnnotationAspect CLIENT = new LineAnnotationAspectAdapter() {
    public String getValue(int lineNumber) {
      P4Revision p4Revision = findRevisionForLine(lineNumber);
      if (p4Revision != null) {
        return p4Revision.getUser();
      } else {
        return "";
      }
    }

  };

  private final LineAnnotationAspect DATE = new LineAnnotationAspectAdapter() {
    public String getValue(int lineNumber) {
      P4Revision p4Revision = findRevisionForLine(lineNumber);
      if (p4Revision != null) {
        return DATE_FORMAT.format(p4Revision.getDate());
      } else {
        return "";
      }
    }

  };

  private final VirtualFileAdapter myListener;
  private static final SyncDateFormat DATE_FORMAT = new SyncDateFormat(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT));
  private final Project myProject;
  private final List<VcsFileRevision> myPerforceRevisions;


  public PerforceFileAnnotation(final AnnotationInfo annotationInfo,
                                final VirtualFile file,
                                P4Revision[] revisions,
                                final Project project) {
    myAnnotationInfo = annotationInfo;
    myRevisions = revisions;
    myProject = project;
    myFile = file;

    myListener = new VFSForAnnotationListener(file, myListeners);
    VirtualFileManager.getInstance().addVirtualFileListener(myListener);

    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    final P4Connection connectionForFile = settings.getConnectionForFile(myFile);
    myPerforceRevisions = new ArrayList<VcsFileRevision>();
    for (P4Revision p4Revision : myRevisions) {
      myPerforceRevisions.add(new PerforceFileRevision(p4Revision, settings, connectionForFile));
    }
    Collections.sort(myPerforceRevisions, new Comparator<VcsFileRevision>() {
      public int compare(final VcsFileRevision o1, final VcsFileRevision o2) {
        return -1 * o1.getRevisionNumber().compareTo(o2.getRevisionNumber());
      }
    });
  }

  public void addListener(AnnotationListener listener) {
    myListeners.add(listener);
  }

  public void removeListener(AnnotationListener listener) {
    myListeners.add(listener);
  }

  public void dispose() {
    VirtualFileManager.getInstance().removeVirtualFileListener(myListener);
  }

  public LineAnnotationAspect[] getAspects() {
    return new LineAnnotationAspect[]{REVISION, DATE, CLIENT};
  }

  public String getToolTip(final int lineNumber) {
    P4Revision revision = findRevisionForLine(lineNumber);
    return revision != null ? "Revision " + revision.getChangeNumber() + ": " + revision.getSubmitMessage() : "";
  }

  public String getAnnotatedContent() {
    return myAnnotationInfo.getContent();
  }

  @Nullable
  private P4Revision findRevisionForLine(final int lineNumber) {
    final int revision = myAnnotationInfo.getRevision(lineNumber);
    for (P4Revision p4Revision : myRevisions) {
      if (myAnnotationInfo.isUseChangelistNumbers()) {
        if (p4Revision.getChangeNumber() == revision) return p4Revision;
      }
      else {
        if (p4Revision.getRevisionNumber() == revision) return p4Revision;
      }
    }
    return null;
  }

  public VcsRevisionNumber getLineRevisionNumber(final int lineNumber) {
    P4Revision p4Revision = findRevisionForLine(lineNumber);
    if (p4Revision != null) {
      return new PerforceVcsRevisionNumber(p4Revision.getRevisionNumber(), p4Revision.getChangeNumber(), p4Revision.isBranched());
    }
    return null;
  }

  /**
   * Get revision number for the line.
   */
  public VcsRevisionNumber originalRevision(int lineNumber) {
    return getLineRevisionNumber(lineNumber);
  }

  public List<VcsFileRevision> getRevisions() {
    return myPerforceRevisions;
  }

  public AnnotationSourceSwitcher getAnnotationSourceSwitcher() {
    return null;
  }

  private class PerforceRevisionAnnotationAspect extends LineAnnotationAspectAdapter implements EditorGutterAction {
    public String getValue(int lineNumber) {
      P4Revision p4Revision = findRevisionForLine(lineNumber);
      if (p4Revision != null) {
        return String.valueOf(p4Revision.getChangeNumber());
      } else {
        return "";
      }
    }

    public Cursor getCursor(final int lineNum) {
      return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    public void doAction(int lineNum) {
      P4Revision p4Revision = findRevisionForLine(lineNum);
      if (p4Revision != null) {
        final long changeNumber = p4Revision.getChangeNumber();
        P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myFile);
        ShowAllSubmittedFilesAction.showAllSubmittedFiles(myProject, changeNumber,
                                                          p4Revision.getSubmitMessage(),
                                                          p4Revision.getDate(),
                                                          p4Revision.getUser(),
                                                          connection);
      }
    }
  }
}
