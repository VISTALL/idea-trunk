/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts;

import com.intellij.ProjectTopics;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.struts.dom.validator.FormValidation;
import com.intellij.struts.facet.StrutsFacet;
import com.intellij.struts.facet.StrutsFacetType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an IDEA project with Struts support.
 *
 * @author Dmitry Avdeev
 */
public class StrutsProjectComponent extends AbstractProjectComponent {

  private StrutsView myStrutsView;
  private ToolWindow myToolWindow;

  private final StrutsDomFactory myStrutsFactory;
  private final StrutsPluginDomFactory<TilesDefinitions, TilesModel> myTilesFactory;
  private final StrutsPluginDomFactory<FormValidation, ValidationModel> myValidatorFactory;


  @NotNull
  public static StrutsProjectComponent getInstance(@NotNull final Project project) {
    return project.getComponent(StrutsProjectComponent.class);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "StrutsProjectComponent";
  }

    /**
   * Init DOM-Factories.
   *
   * @param project    IDEA project.
     */
  public StrutsProjectComponent(final Project project) {
    super(project);

    myStrutsFactory = new StrutsDomFactory(project);

    myTilesFactory = new TilesDomFactory(myStrutsFactory, project);

    myValidatorFactory = new ValidatorDomFactory(myStrutsFactory, project);
  }

  /**
   * Registers a tool window, which should be defined at Project level.
   */
  public void projectOpened() {

    /*
     * Checks availability of Struts library to disable/enable Struts Toolwindow.
     */
    final ModuleRootListener rootListener = new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {

      }

      public void rootsChanged(ModuleRootEvent event) {
        DumbService.getInstance(myProject).smartInvokeLater(new Runnable() {
          public void run() {
            checkStruts();
          }
        });
      }
    };

    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        checkStruts();
        final MessageBusConnection connection = myProject.getMessageBus().connect();
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, rootListener);
      }
    });
  }

  private void checkStruts() {
    if (myProject.isDisposed()) {
      return;
    }
    final PsiManager psiManager = PsiManager.getInstance(myProject);
    if (psiManager.isDisposed()) {
      return;
    }

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      final PsiClass actionClass = JavaPsiFacade.getInstance(psiManager.getProject())
        .findClass("org.apache.struts.action.Action", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
      final StrutsFacet strutsFacet = FacetManager.getInstance(module).getFacetByType(StrutsFacetType.ID);
      if (actionClass != null && strutsFacet != null) {

        // lazy init
        if (myStrutsView == null) {
          myStrutsView = new StrutsView(myProject);
          Disposer.register(myProject, myStrutsView);
          myToolWindow = ToolWindowManager.getInstance(myProject)
            .registerToolWindow(StrutsConstants.TOOL_WINDOW_ID, false, ToolWindowAnchor.LEFT);

          final ContentFactory contentFactory = PeerFactory.getInstance().getContentFactory();
          final Content content = contentFactory.createContent(myStrutsView.getComponent(), null, false);
          myToolWindow.getContentManager().addContent(content);
          myToolWindow.setIcon(StrutsIcons.ACTION_ICON);
        }

        myToolWindow.setAvailable(true, null);
        myStrutsView.openDefault();
        return;
      }

    }

    // hide already shown toolwindow
    if (myToolWindow != null) {
      myToolWindow.hide(null);
      myToolWindow.setAvailable(false, null);
    }

  }

  @Nullable
  public StrutsView getStrutsView() {
    return myStrutsView;
  }

  public StrutsDomFactory getStrutsFactory() {
    return myStrutsFactory;
  }

  public StrutsPluginDomFactory<TilesDefinitions, TilesModel> getTilesFactory() {
    return myTilesFactory;
  }

  public StrutsPluginDomFactory<FormValidation, ValidationModel> getValidatorFactory() {
    return myValidatorFactory;
  }

}