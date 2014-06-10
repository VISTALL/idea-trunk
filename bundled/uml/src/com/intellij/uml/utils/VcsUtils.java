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

package com.intellij.uml.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.uml.UmlVirtualFileSystem;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.presentation.UmlDiagramPresentationAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class VcsUtils {
  private static final PsiElementFilter<PsiClass> CLASS_FILTER = new PsiElementFilter<PsiClass>(PsiClass.class) {
    @Override
    public boolean accept(PsiClass element) {
      return ! PsiUtils.isAnonymousClass(element);
    }
  };

  private VcsUtils() {
  }

  public static List<PsiFile> getChangedFiles(Project project, LocalChangeList changeList) {
    List<PsiFile> result = new ArrayList<PsiFile>();
    for (Change change : changeList.getChanges()) {
      ContentRevision contentRevision = change.getBeforeRevision();
      if (contentRevision == null) contentRevision = change.getAfterRevision();
      if (contentRevision == null) continue;
      final VirtualFile affectedFile = contentRevision.getFile().getVirtualFile();

      if (affectedFile == null) continue; //TODO: make workaround for deleted classes

      final PsiFile file = PsiManager.getInstance(project).findFile(affectedFile);
      if (file != null) {
        result.add(file);
      }
    }
    return result;
  }

  public static List<PsiClass> getChangedClasses(Project project, LocalChangeList changeList) {
    final List<PsiClass> classes = new ArrayList<PsiClass>();

    for (PsiFile file : getChangedFiles(project, changeList)) {
      classes.addAll(PsiChangeTracker.getElementsChanged(file, CLASS_FILTER).keySet());
    }
    return classes;
  }

  public static boolean isShowChangesFile(VirtualFile file) {
    return UmlVirtualFileSystem.isUmlVirtualFile(file) && file.getUrl().startsWith(UmlVirtualFileSystem.CHANGES);  
  }

  @Nullable
  public static LocalChangeList getLocalChangeListFromFile(VirtualFile file, Project project) {
    final String path = file.getUrl().substring(UmlVirtualFileSystem.CHANGES.length());
    String id = path.contains("/") ? path.split("/")[0] : null;    
    final ChangeListManager listManager = ChangeListManager.getInstance(project);
    final LocalChangeList changeList = listManager.getChangeList(id);
    return changeList == null ? listManager.getDefaultChangeList() : changeList;
  }

  public static final UmlDiagramPresentation CHANGES_PRESENTATION = new UmlDiagramPresentationAdapter() {
    @Override public boolean isFieldsVisible() {return true;}
    @Override public boolean isMethodsVisible() {return true;}
    @Override public boolean isShowInnerClasses() {return true;}
    @Override public boolean isVcsFilterEnabled() {return true;}
    @Override public boolean isConstructorsVisible() {return true;}
  };
}
