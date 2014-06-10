package org.jetbrains.plugins.grails.perspectives.graph;

import com.intellij.openapi.graph.view.Graph2DSelectionEvent;
import com.intellij.openapi.graph.view.Graph2DSelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiManager;

/**
 * User: Dmitry.Krasilschikov
 * Date: 27.11.2008
 */
public class DataModelAndSelectionModificationTracker implements ModificationTracker, Graph2DSelectionListener {
  private long myCounter = 0;
  private final Project myProject;

  public DataModelAndSelectionModificationTracker(final Project project) {
    myProject = project;
  }

  public long getModificationCount() {
    myCounter = Math.max(PsiManager.getInstance(myProject).getModificationTracker().getModificationCount(), myCounter);
    return myCounter;
  }

  public void onGraph2DSelectionEvent(final Graph2DSelectionEvent event) {
    myCounter = Math.max(PsiManager.getInstance(myProject).getModificationTracker().getModificationCount(), myCounter);
    myCounter++;
  }
}

