/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.perspectives;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.xml.ui.Committable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.perspectives.graph.*;

import javax.swing.*;
import java.awt.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class PerspectiveFileEditorComponent extends JPanel implements DataProvider, Committable {
  private PsiFile[] myFiles;

  private final GraphBuilder<DomainClassNode, DomainClassRelationsInfo> myBuilder;
  private final DomainClassesRelationsDataModel myDataModel;
  private final Project myProject;
  private final PsiTreeChangeAdapter myListener;
  @NonNls private static final String HELP_ID = "reference.persistencediagram";

  public PerspectiveFileEditorComponent(VirtualFile domainDirectory, Project project) {
    myProject = project;

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();

    myDataModel = new DomainClassesRelationsDataModel(domainDirectory, myProject);

    DomainClassDependencyPresentation presentationModel = new DomainClassDependencyPresentation(graph, myDataModel);

    myBuilder = GraphBuilderFactory.getInstance(myProject).createGraphBuilder(graph, view, myDataModel, presentationModel);

    setLayout(new BorderLayout());

    add(createToolbarPanel(), BorderLayout.NORTH);
    add(myBuilder.getView().getComponent(), BorderLayout.CENTER);

    myListener = new PsiTreeChangeAdapter() {
      public void childrenChanged(PsiTreeChangeEvent psiTreeChangeEvent) {
        update();
      }

      public void childRemoved(PsiTreeChangeEvent event) {
        update();
      }

      public void childAdded(PsiTreeChangeEvent event) {
        update();
      }

      public void childReplaced(PsiTreeChangeEvent event) {
        update();
      }
    };

    PsiManager.getInstance(myProject).addPsiTreeChangeListener(myListener, this);

    Disposer.register(this, myBuilder);
    myBuilder.initialize();
  }

  private void update() {
    if (isShowing()) {
      myBuilder.queueUpdate();
    }
  }

  private JComponent createToolbarPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();

    actions.add(GraphViewUtil.getBasicToolbar(myBuilder.getGraph()));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
    return actionToolbar.getComponent();
  }

  @Nullable
  public Object getData(@NonNls String s) {
    if (DataKeys.HELP_ID.getName().equals(s)) {
      return HELP_ID;
    }
    return null;
  }

  public void commit() {
  }

  public void reset() {
    myBuilder.updateGraph();
  }

  public void dispose() {
    PsiManager.getInstance(myProject).removePsiTreeChangeListener(myListener);
  }

  public Overview getOverview() {
    return GraphManager.getGraphManager().createOverview(myBuilder.getView());
  }

  public DomainClassesRelationsDataModel getDataModel() {
    return myDataModel;
  }

  public Project getProject() {
    return myProject;
  }

  public GraphBuilder<DomainClassNode, DomainClassRelationsInfo> getBuilder() {
    return myBuilder;
  }
}
